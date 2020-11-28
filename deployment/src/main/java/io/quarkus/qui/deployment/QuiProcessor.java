package io.quarkus.qui.deployment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BranchResult;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.qui.DomLocation;
import io.quarkus.qui.Props;
import io.quarkus.qui.QuiCanvas;
import io.quarkus.qui.View;
import io.quarkus.qui.Window;
import io.quarkus.qui.annotations.CollectionProp;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

class QuiProcessor {

    private static final String FEATURE = "qui";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void createPropsImplementations(
            CombinedIndexBuildItem combinedIndex,
            BuildProducer<AdditionalBeanBuildItem> additionalBeanConsumer,
            BuildProducer<GeneratedBeanBuildItem> generatedBeanConsumer) {
        DotName VIEW_DOTNAME = DotName.createSimple(View.class.getName());
        IndexView indexView = combinedIndex.getIndex();
        GeneratedBeanGizmoAdaptor classOutput = new GeneratedBeanGizmoAdaptor(generatedBeanConsumer);
        for (ClassInfo classInfo : indexView
                .getKnownDirectImplementors(VIEW_DOTNAME)) {
            if (!classInfo.hasNoArgsConstructor()) {
                continue;
            }
            Optional<org.jboss.jandex.Type> maybePropsType = classInfo.interfaceTypes().stream()
                    .filter(interfaceType -> interfaceType.name().equals(VIEW_DOTNAME))
                    .filter(interfaceType -> interfaceType.kind().equals(org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE))
                    .map(interfaceType ->
                            interfaceType.asParameterizedType().arguments().get(0))
                    .findAny();

            maybePropsType.ifPresent(propsType -> {
                try {
                    Class<?> viewClass = Class.forName(classInfo.name().toString(),
                                                       false, Thread.currentThread().getContextClassLoader());
                    Class<?> propsClass = Class.forName(propsType.name().toString(),
                                                    false, Thread.currentThread().getContextClassLoader());
                    String generatedClassName = viewClass.getPackage().getName() + "." + viewClass.getSimpleName() + "__" + propsClass.getSimpleName();
                    generatePropsImplementation(classOutput, generatedClassName,
                                                viewClass, propsClass);
                    additionalBeanConsumer.produce(new AdditionalBeanBuildItem(viewClass));
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    private void generatePropsImplementation(ClassOutput classOutput, String generatedClassName,
                                                     Class<?> viewClass, Class<?> propsClass) throws NoSuchMethodException {
        ClassCreator classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(generatedClassName)
                .interfaces(Props.class, propsClass)
                .build();

        classCreator.addAnnotation(Dependent.class);
        classCreator.addAnnotation(Named.class)
                .addValue("value", viewClass.getName());

        List<String> fieldNames = new ArrayList<>();
        List<Class<?>> fieldTypes = new ArrayList<>();
        List<Class<?>> ownerTypes = new ArrayList<>();
        List<CollectionProp> fieldCollectionProp = new ArrayList<>();

        for (Method method : propsClass.getMethods()) {
            if (!method.isDefault() && !method.getDeclaringClass().equals(Props.class)
                && !method.getDeclaringClass().equals(Object.class)
                && method.getParameterCount() == 1) {
                fieldNames.add(method.getName());
                fieldTypes.add(method.getParameterTypes()[0]);
                ownerTypes.add(method.getDeclaringClass());

                if (method.isAnnotationPresent(CollectionProp.class)) {
                    fieldCollectionProp.add(method.getAnnotation(CollectionProp.class));
                }
                else {
                    fieldCollectionProp.add(null);
                }
            }
        }

        // Generate special __window__ field to hold props Window
        FieldDescriptor window = classCreator.getFieldCreator("__window__", Window.class).getFieldDescriptor();

        // Generate special __view__ field to hold view this props is for
        FieldDescriptor view = classCreator.getFieldCreator("__view__", viewClass).getFieldDescriptor();

        // Generate special __domLocation__ field to hold where in the document this props is
        FieldDescriptor domLocation = classCreator.getFieldCreator("__domLocation__", DomLocation.class).getFieldDescriptor();

        // Generate special __value__ field to hold value of field to get
        FieldDescriptor value = classCreator.getFieldCreator("__value__", Object.class).getFieldDescriptor();

        // Generate fields that correspond with each property
        List<FieldDescriptor> fieldDescriptors = new ArrayList<>(fieldNames.size());
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            Class<?> fieldType = fieldTypes.get(i);
            fieldDescriptors.add(classCreator.getFieldCreator(fieldName, fieldType).getFieldDescriptor());
        }

        if (fieldCollectionProp.stream().anyMatch(Objects::nonNull)) {
            MethodCreator methodCreator = classCreator.getMethodCreator("__postConstruct__", "void");
            methodCreator.addAnnotation(PostConstruct.class);
            ResultHandle thisObject = methodCreator.getThis();
            for (int i = 0; i < fieldNames.size(); i++) {
                if (fieldCollectionProp.get(i) != null) {
                    Class<?> collectionClass = getCollectionClass(fieldTypes.get(i).getTypeName());
                    MethodDescriptor constructor = MethodDescriptor.ofConstructor(collectionClass);
                    ResultHandle collectionInstance = methodCreator
                            .newInstance(constructor);
                    methodCreator.writeInstanceField(fieldDescriptors.get(i), thisObject, collectionInstance);
                }
            }
            methodCreator.returnValue(null);
        }

        // Generate the setters for each property
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            Class<?> fieldType = fieldTypes.get(i);
            FieldDescriptor fieldDescriptor = fieldDescriptors.get(i);

            MethodCreator mv = classCreator.getMethodCreator(fieldName, ownerTypes.get(i), fieldType);
            // parameter name

            // Check if set value is special __GET_VALUE__
            ResultHandle thisObj = mv.getThis();
            ResultHandle parameter = mv.getMethodParam(0);
            BranchResult isValueNullBranch = mv.ifNull(parameter);

            // It null, so set __value__ to the current value of the field
            BytecodeCreator valueIsNull = isValueNullBranch.trueBranch();
            ResultHandle fieldValue = valueIsNull.readInstanceField(fieldDescriptor, thisObj);
            valueIsNull.writeInstanceField(value, thisObj, fieldValue);
            valueIsNull.returnValue(thisObj);

            // It is not null, so set the field
            BytecodeCreator valueIsNotNull = isValueNullBranch.falseBranch();
            if (fieldCollectionProp.get(i) == null) {
                valueIsNotNull.writeInstanceField(fieldDescriptor, thisObj, parameter);
            }
            else {
                CollectionProp collectionProp = fieldCollectionProp.get(i);
                String method = collectionProp.addAll().isEmpty()? getAddAllForType(fieldType) : collectionProp.addAll();
                Class<?> collectionClass = getCollectionClass(fieldType.getTypeName());

                MethodDescriptor addAll = MethodDescriptor.ofMethod(collectionClass.getMethod(method, fieldType));
                ResultHandle field = valueIsNotNull.readInstanceField(fieldDescriptor, thisObj);
                valueIsNotNull.invokeVirtualMethod(addAll, field, parameter);
            }
            valueIsNotNull.returnValue(thisObj);
        }

        // Special get method
        {
            MethodCreator mv = classCreator.getMethodCreator("_get", Object.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle returnValue = mv.readInstanceField(value, thisObj);
            mv.returnValue(returnValue);
        }

        // __window__ get method
        {
            MethodCreator mv = classCreator.getMethodCreator("_getWindow", Window.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle returnValue = mv.readInstanceField(window, thisObj);
            mv.returnValue(returnValue);
        }

        // __window__ set method
        {
            MethodCreator mv = classCreator.getMethodCreator("_setWindow", Props.class, Window.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle parameter = mv.getMethodParam(0);
            mv.writeInstanceField(window, thisObj, parameter);
            mv.returnValue(thisObj);
        }

        // __view__ get method
        {
            MethodCreator mv = classCreator.getMethodCreator("_getView", View.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle returnValue = mv.readInstanceField(view, thisObj);
            mv.returnValue(returnValue);
        }

        // __view__ set method
        {
            MethodCreator mv = classCreator.getMethodCreator("_setView", Props.class, View.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle parameter = mv.getMethodParam(0);
            mv.writeInstanceField(view, thisObj, parameter);
            mv.returnValue(thisObj);
        }

        // __domLocation__ get method
        {
            MethodCreator mv = classCreator.getMethodCreator("_getDomLocation", DomLocation.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle returnValue = mv.readInstanceField(domLocation, thisObj);
            mv.returnValue(returnValue);
        }

        // __domLocation__ set method
        {
            MethodCreator mv = classCreator.getMethodCreator("_setDomLocation", Props.class, DomLocation.class);
            ResultHandle thisObj = mv.getThis();
            ResultHandle parameter = mv.getMethodParam(0);
            mv.writeInstanceField(domLocation, thisObj, parameter);
            mv.returnValue(thisObj);
        }

        // Reset method
        {
            MethodCreator mv = classCreator.getMethodCreator("_reset", propsClass);
            ResultHandle thisObj = mv.getThis();
            ResultHandle nullValue = mv.loadNull();
            ResultHandle zeroValue = mv.load((byte) 0);
            ResultHandle falseValue = mv.load(false);
            for (int i = 0; i < fieldNames.size(); i++) {
                FieldDescriptor fieldDescriptor = fieldDescriptors.get(i);
                if (fieldTypes.get(i).isPrimitive()) {
                    switch (fieldTypes.get(i).getName()) {
                        case "Z": // boolean
                            mv.writeInstanceField(fieldDescriptor, thisObj, falseValue);
                            break;
                        case "B": // byte
                        case "C": // char
                        case "D": // double
                        case "S": // short
                        case "F": // float
                        case "J": // long
                            mv.writeInstanceField(fieldDescriptor, thisObj, zeroValue);
                            break;
                    }
                }
                else {
                    if (fieldCollectionProp.get(i) == null) {
                        mv.writeInstanceField(fieldDescriptor, thisObj, nullValue);
                    }
                    else {
                        CollectionProp collectionProp = fieldCollectionProp.get(i);
                        ResultHandle fieldHandle = mv.readInstanceField(fieldDescriptor, thisObj);
                        Class<?> collectionClass = getCollectionClass(fieldTypes.get(i).getTypeName());
                        MethodDescriptor clearMethod = MethodDescriptor.ofMethod(collectionClass.getMethod(collectionProp.reset()));
                        mv.invokeVirtualMethod(clearMethod, fieldHandle);
                    }
                }
            }
            mv.returnValue(thisObj);
        }

        // Draw method
        // This is the props passed to a view, which is not
        // the same as the props that will be drawn from
        // the view's render.
        {
            MethodCreator mv = classCreator.getMethodCreator("draw", "void", QuiCanvas.class);
            ResultHandle thisObj = mv.getThis();
            AssignableResultHandle currentProps = mv.createVariable(Props.class);
            mv.assign(currentProps, thisObj);
            BytecodeCreator whileLoop = mv.whileLoop(bytecodeCreator -> {
                MethodDescriptor getWindow = MethodDescriptor.ofMethod(Props.class, "_getWindow", Window.class);
                ResultHandle theWindow = bytecodeCreator.invokeInterfaceMethod(getWindow, currentProps);
                return bytecodeCreator.ifNotNull(theWindow);
            }).block();
            MethodDescriptor draw = MethodDescriptor.ofMethod(Props.class, "draw", "void", QuiCanvas.class);
            MethodDescriptor render = MethodDescriptor.ofMethod(View.class, "render", Props.class, Props.class);
            MethodDescriptor getView= MethodDescriptor.ofMethod(Props.class, "_getView", View.class);
            ResultHandle propsView = whileLoop.invokeInterfaceMethod(getView, currentProps);
            ResultHandle renderedProps = whileLoop.invokeInterfaceMethod(render, propsView, currentProps);
            whileLoop.assign(currentProps, renderedProps);
            ResultHandle parameter = mv.getMethodParam(0);
            mv.invokeInterfaceMethod(draw, currentProps, parameter);
            mv.returnValue(null);
        }

        classCreator.close();
    }

    private String getAddAllForType(Class<?> classType) {
        if (Map.class.isAssignableFrom(classType)) {
            return "putAll";
        }
        else if (Collection.class.isAssignableFrom(classType)) {
            return "addAll";
        }
        else {
            throw new IllegalArgumentException(
                    classType + " is not a Collection or Map and no addAll method was specified.");
        }
    }

    private Class<?> getCollectionClass(String childrenCollectionType) {
        Class<?> classInfo = null;
        try {
            classInfo = Class.forName(childrenCollectionType, false,
                                               Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        try {
            classInfo.getConstructor();
            return classInfo;
        }
        catch (NoSuchMethodException e) {
            if (classInfo.isAssignableFrom(List.class)) {
                return ArrayList.class;
            }
            else if (classInfo.isAssignableFrom(Set.class)) {
                return HashSet.class;
            }
            else {
                throw new IllegalArgumentException("Unrecognized collection with no no-args constructor: " + childrenCollectionType);
            }
        }
    }
}
