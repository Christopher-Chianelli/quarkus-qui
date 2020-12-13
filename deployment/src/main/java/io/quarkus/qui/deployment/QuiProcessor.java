package io.quarkus.qui.deployment;

import java.util.Optional;
import javax.inject.Singleton;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LiveReloadBuildItem;
import io.quarkus.deployment.dev.RuntimeUpdatesProcessor;
import io.quarkus.qui.View;
import io.quarkus.qui.devmode.HotReplacementManager;
import io.quarkus.qui.devmode.QuiHotReplacementService;
import io.quarkus.qui.devmode.WindowSetup;
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
    SyntheticBeanBuildItem restorePreviousWindows(LiveReloadBuildItem liveReload) {
        WindowSetup windowSetup;
        if (liveReload.isLiveReload()) {
            windowSetup = liveReload.getContextObject(WindowSetup.class);
            WindowSetup.setInstance(windowSetup);
        }
        else {
            windowSetup = new WindowSetup();
            liveReload.setContextObject(WindowSetup.class, windowSetup);
        }

        return SyntheticBeanBuildItem
                .configure(WindowSetup.class).scope(Singleton.class)
                .supplier(new WindowSetup.WindowSetupSupplier())
                .done();
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
                    PropsImplementor propsImplementor = new PropsImplementor(classOutput, generatedClassName,
                                                                             viewClass, propsClass);
                    propsImplementor.generateImplementation();
                    additionalBeanConsumer.produce(new AdditionalBeanBuildItem(viewClass));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }
}
