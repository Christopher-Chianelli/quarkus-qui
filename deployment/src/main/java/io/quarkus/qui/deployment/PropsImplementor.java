package io.quarkus.qui.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

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

public class PropsImplementor {
    ClassCreator classCreator;
    Class<?> propsClass;
    Class<?> viewClass;
    List<String> fieldNames = new ArrayList<>();
    List<Class<?>> fieldTypes = new ArrayList<>();
    List<Class<?>> ownerTypes = new ArrayList<>();
    List<CollectionProp> fieldCollectionProp = new ArrayList<>();
    FieldDescriptor window;
    FieldDescriptor view;
    FieldDescriptor domLocation;
    FieldDescriptor value;
    List<FieldDescriptor> fieldDescriptors;

    public PropsImplementor(ClassOutput classOutput, String generatedClassName,
                            Class<?> viewClass, Class<?> propsClass) {
        classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(generatedClassName)
                .interfaces(Props.class, propsClass)
                .build();

        classCreator.addAnnotation(Dependent.class);
        classCreator.addAnnotation(Named.class)
                .addValue("value", viewClass.getName());

        this.propsClass = propsClass;
        this.viewClass = viewClass;

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
        window = classCreator.getFieldCreator("__window__", Window.class).getFieldDescriptor();

        // Generate special __view__ field to hold view this props is for
        view = classCreator.getFieldCreator("__view__", viewClass).getFieldDescriptor();

        // Generate special __domLocation__ field to hold where in the document this props is
        domLocation = classCreator.getFieldCreator("__domLocation__", DomLocation.class).getFieldDescriptor();

        // Generate special __value__ field to hold value of field to get
        value = classCreator.getFieldCreator("__value__", Object.class).getFieldDescriptor();

        // Generate fields that correspond with each property
        fieldDescriptors = new ArrayList<>(fieldNames.size());
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            Class<?> fieldType = fieldTypes.get(i);
            fieldDescriptors.add(classCreator.getFieldCreator(fieldName, fieldType).getFieldDescriptor());
        }
    }

    public void generateImplementation() {
        try {
            generateGetterSetterForImplField("Window", Window.class, window);
            generateGetterSetterForImplField("View", View.class, view);
            generateGetterSetterForImplField("DomLocation", DomLocation.class, domLocation);
            generateGetter("_get", Object.class, value);

            if (fieldCollectionProp.stream().anyMatch(Objects::nonNull)) {
                generatePostConstruct();
            }

            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                Class<?> ownerType = ownerTypes.get(i);
                Class<?> fieldType = fieldTypes.get(i);
                CollectionProp collectionProp = fieldCollectionProp.get(i);
                FieldDescriptor fieldDescriptor = fieldDescriptors.get(i);

                generateFieldSetter(fieldName, ownerType, fieldType, fieldDescriptor, collectionProp);
            }

            generateReset();
            generateDraw();
            generateGetAsMap();
            generateSetAsMap();

            classCreator.close();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private void generatePostConstruct() {
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

    private void generateFieldSetter(String fieldName, Class<?> ownerType, Class<?> fieldType, FieldDescriptor fieldDescriptor,
                                     CollectionProp collectionProp) throws NoSuchMethodException {
        MethodCreator mv = classCreator.getMethodCreator(fieldName, ownerType, fieldType);
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
        if (collectionProp == null) {
            valueIsNotNull.writeInstanceField(fieldDescriptor, thisObj, parameter);
        }
        else {
            String method = collectionProp.addAll().isEmpty()? getAddAllForType(fieldType) : collectionProp.addAll();
            Class<?> collectionClass = getCollectionClass(fieldType.getTypeName());

            MethodDescriptor addAll = MethodDescriptor.ofMethod(collectionClass.getMethod(method, fieldType));
            ResultHandle field = valueIsNotNull.readInstanceField(fieldDescriptor, thisObj);
            valueIsNotNull.invokeVirtualMethod(addAll, field, parameter);
        }
        valueIsNotNull.returnValue(thisObj);
    }

    private void generateGetterSetterForImplField(String fieldName, Class<?> fieldType, FieldDescriptor fieldDescriptor) {
        generateGetter("_get" + fieldName, fieldType, fieldDescriptor);
        generateSimpleSetter("_set" + fieldName, fieldType, fieldDescriptor);
    }

    private void generateGetter(String methodName, Class<?> fieldType, FieldDescriptor fieldDescriptor) {
        MethodCreator mv = classCreator.getMethodCreator(methodName, fieldType);
        ResultHandle thisObj = mv.getThis();
        ResultHandle returnValue = mv.readInstanceField(fieldDescriptor, thisObj);
        mv.returnValue(returnValue);
    }

    private void generateSimpleSetter(String methodName, Class<?> fieldType, FieldDescriptor fieldDescriptor) {
        MethodCreator mv = classCreator.getMethodCreator(methodName, Props.class, fieldType);
        ResultHandle thisObj = mv.getThis();
        ResultHandle parameter = mv.getMethodParam(0);
        mv.writeInstanceField(fieldDescriptor, thisObj, parameter);
        mv.returnValue(thisObj);
    }

    private void generateReset() throws NoSuchMethodException {
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
                    case "I": // int
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

    private void generateDraw() {
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

    private void generateGetAsMap() throws NoSuchMethodException {
        MethodCreator mv = classCreator.getMethodCreator("_getAsMap", Map.class);
        ResultHandle thisObj = mv.getThis();
        ResultHandle map = mv.newInstance(MethodDescriptor.ofConstructor(HashMap.class));

        for (int i = 0; i < fieldNames.size(); i++) {
            FieldDescriptor fieldDescriptor = fieldDescriptors.get(i);
            ResultHandle fieldValue = mv.readInstanceField(fieldDescriptor, thisObj);
            ResultHandle fieldName = mv.load(fieldNames.get(i));
            ResultHandle fieldMapValue;
            if (fieldTypes.get(i).isPrimitive()) {
                switch (fieldTypes.get(i).getName()) {
                    case "Z": // boolean
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Boolean.class, "toString",
                                                                                        "String",
                                                                                        "boolean"),
                                                              fieldValue);
                        break;
                    case "B": // byte
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Byte.class, "toString",
                                                                                        "String",
                                                                                        "byte"),
                                                              fieldValue);
                        break;
                    case "C": // char
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Character.class, "toString",
                                                                                        "String",
                                                                                        "char"),
                                                              fieldValue);
                        break;
                    case "D": // double
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Double.class, "toString",
                                                                                        "String",
                                                                                        "double"),
                                                              fieldValue);
                        break;
                    case "S": // short
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Short.class, "toString",
                                                                                        "String",
                                                                                        "short"),
                                                              fieldValue);
                        break;
                    case "F": // float
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Float.class, "toString",
                                                                                        "String",
                                                                                        "float"),
                                                              fieldValue);
                        break;
                    case "J": // long
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Long.class, "toString",
                                                                                        "String",
                                                                                        "long"),
                                                              fieldValue);
                        break;

                    case "I": // int
                        fieldMapValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "toString",
                                                                                        "String",
                                                                                        "int"),
                                                              fieldValue);
                        break;
                    default:
                        throw new IllegalStateException("Missing primitive case");
                }
            }
            else {
                // if (Serializable.class.isAssignableFrom(fieldTypes.get(i))) {
                ResultHandle byteOutputStream = mv
                        .newInstance(MethodDescriptor.ofConstructor(ByteArrayOutputStream.class));
                ResultHandle objectOutputStream = mv.newInstance(MethodDescriptor.ofConstructor(ObjectOutputStream.class, OutputStream.class),
                                                                 byteOutputStream);
                mv.invokeVirtualMethod(MethodDescriptor.ofMethod(ObjectOutputStream.class, "writeObject", "void", Object.class), objectOutputStream, fieldValue);
                ResultHandle byteArray = mv.invokeVirtualMethod(MethodDescriptor.ofMethod(ByteArrayOutputStream.class, "toByteArray", byte[].class), byteOutputStream);
                ResultHandle encoder = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Base64.class, "getEncoder", Base64.Encoder.class));
                fieldMapValue = mv.invokeVirtualMethod(MethodDescriptor.ofMethod(Base64.Encoder.class, "encodeToString", String.class, byte[].class),
                                                       encoder, byteArray);
                //  }
                // else {
                //     throw new IllegalStateException("Props has unserializable type " + fieldTypes.get(i) + " for field " + fieldNames.get(i));
                // }
            }
            mv.invokeInterfaceMethod(MethodDescriptor.ofMethod(Map.class.getMethod("put", Object.class, Object.class)),
                                     map, fieldName, fieldMapValue);
        }

        mv.returnValue(map);
    }

    private void generateSetAsMap() {
        MethodCreator mv = classCreator.getMethodCreator("_setAsMap", "void", Map.class);
        ResultHandle thisObj = mv.getThis();
        ResultHandle map = mv.getMethodParam(0);

        for (int i = 0; i < fieldNames.size(); i++) {
            FieldDescriptor fieldDescriptor = fieldDescriptors.get(i);
            ResultHandle fieldName = mv.load(fieldNames.get(i));
            ResultHandle fieldValue;
            ResultHandle fieldMapValue = mv.invokeInterfaceMethod(MethodDescriptor.ofMethod(Map.class, "get", Object.class, Object.class),
                                                                  map,
                                                                  fieldName);
            if (fieldTypes.get(i).isPrimitive()) {
                switch (fieldTypes.get(i).getName()) {
                    case "Z": // boolean
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Boolean.class, "parseBoolean",
                                                                                     "boolean",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;
                    case "B": // byte
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Byte.class, "parseByte",
                                                                                     "byte",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;
                    case "C": // char
                        ResultHandle zero = mv.load(0);
                        fieldValue = mv.invokeVirtualMethod(MethodDescriptor.ofMethod(String.class, "charAt",
                                                                                      "char",
                                                                                      "int"),
                                                            fieldMapValue, zero);
                        break;
                    case "D": // double
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Double.class, "parseDouble",
                                                                                     "double",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;
                    case "S": // short
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Short.class, "parseShort",
                                                                                     "short",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;
                    case "F": // float
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Float.class, "parseFloat",
                                                                                     "float",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;
                    case "J": // long
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Long.class, "parseLong",
                                                                                     "long",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;

                    case "I": // int
                        fieldValue = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "parseInt",
                                                                                     "int",
                                                                                     "String"),
                                                           fieldMapValue);
                        break;
                    default:
                        throw new IllegalStateException("Missing primitive case");
                }
                mv.invokeInterfaceMethod(MethodDescriptor.ofMethod(Map.class, "put", "void", Object.class, Object.class),
                                         map, fieldName, fieldMapValue);
            }
            else {
                //if (Serializable.class.isAssignableFrom(fieldTypes.get(i))) {
                ResultHandle decoder = mv.invokeStaticMethod(MethodDescriptor.ofMethod(Base64.class, "getDecoder", Base64.Decoder.class));
                ResultHandle byteArray = mv.invokeVirtualMethod(MethodDescriptor.ofMethod(Base64.Decoder.class, "decode", byte[].class, String.class), decoder, fieldMapValue);
                ResultHandle byteInputStream = mv
                        .newInstance(MethodDescriptor.ofConstructor(ByteArrayInputStream.class, byte[].class), byteArray);
                ResultHandle objectInputStream = mv.newInstance(MethodDescriptor.ofConstructor(ObjectInputStream.class, InputStream.class),
                                                                byteInputStream);
                fieldValue = mv.invokeVirtualMethod(MethodDescriptor.ofMethod(ObjectInputStream.class, "readObject", Object.class), objectInputStream);
                //}
                //else {
                //    throw new IllegalStateException("Props has unserializable type " + fieldTypes.get(i) + " for field " + fieldNames.get(i));
                // }
            }
            mv.writeInstanceField(fieldDescriptor, thisObj, fieldValue);
        }

        mv.returnValue(null);
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
        Class<?> classInfo;
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
