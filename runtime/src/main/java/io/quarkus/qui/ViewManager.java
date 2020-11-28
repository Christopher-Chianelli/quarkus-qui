package io.quarkus.qui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ViewManager {
    @Inject
    Instance<View<?>> viewInstance;

    @Inject
    Instance<Props> propsInstance;

    public static ViewManager INSTANCE = null;

    private Map<Object, Map<DomLocation, List<Props<?>>>> ownerToDomLocationToProps;
    private Map<Object, Map<DomLocation, Integer>> ownerToDomLocationToHitCount;
    private Map<View<?>, Props<?>> viewToPropsMap;

    @PostConstruct
    public void setInstance() {
        ownerToDomLocationToProps = new HashMap<>();
        ownerToDomLocationToHitCount = new HashMap<>();
        viewToPropsMap = new HashMap<>();
        INSTANCE = this;
    }

    /**
     * Must be called at the end of window render
     * to clear memory from removed elements
     */
    public void endRender(Object owner) {
        Map<DomLocation, Integer> domLocationToHitCount = ownerToDomLocationToHitCount.get(owner);
        Map<DomLocation, List<Props<?>>> domLocationToProps = ownerToDomLocationToProps.get(owner);
        domLocationToHitCount.clear();
        for (DomLocation domLocation : new HashSet<>(domLocationToProps.keySet())) {
            if (!domLocationToHitCount.containsKey(domLocation)) {
                domLocationToProps.remove(domLocation).forEach(props -> {
                    viewToPropsMap.remove(props._getView());
                    viewInstance.destroy(props._getView());
                    propsInstance.destroy(props);
                });
            }
            else {
                int count = domLocationToHitCount.get(domLocation);
                List<Props<?>> propsList = domLocationToProps.get(domLocation);
                if (count < propsList.size()) {
                    List<Props<?>> toDestroyList = propsList.subList(count, propsList.size());
                    toDestroyList.forEach(props -> {
                        viewToPropsMap.remove(props._getView());
                        viewInstance.destroy(props._getView());
                        propsInstance.destroy(props);
                    });
                    toDestroyList.clear();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <P extends Props<P>, V extends View<P>> P getProps(View<?> view) {
        return (P) viewToPropsMap.get(view);
    }

    public <P extends Props<P>, V extends View<P>> P createView(View<?> owner, final Class<? extends V> viewClass) {
        return createView(owner, viewClass, null);
    }

    @SuppressWarnings("unchecked")
    public <P extends Props<P>, V extends View<P>> P createView(View<?> parent, final Class<? extends V> viewClass, Object key) {
        Props parentProps = viewToPropsMap.get(parent);
        DomLocation domLocation = parentProps._getDomLocation();
        Window owner = parentProps._getWindow();
        return createView(parent.getClass(), domLocation, owner, viewClass, key);
    }

    public <P extends Props<P>, V extends View<P>> P createView(Class<?> parentClass, DomLocation parentDomLocation, Window owner, final Class<? extends V> viewClass, Object key) {
        Map<DomLocation, Integer> domLocationToHitCount = ownerToDomLocationToHitCount.computeIfAbsent(owner,
                                                                                                       (window) -> new HashMap<>());
        Map<DomLocation, List<Props<?>>> domLocationToProps = ownerToDomLocationToProps.computeIfAbsent(owner,
                                                                                                        (window) -> new HashMap<>());
        DomLocation domLocation = new DomLocation(parentDomLocation, key, parentClass);
        int count = domLocationToHitCount.getOrDefault(domLocation, 0);
        domLocationToHitCount.merge(domLocation, 1, Integer::sum);
        if (domLocationToProps.containsKey(domLocation) && domLocationToProps.get(domLocation).size() > count) {
            // Reset the prop, since it'll be reinitialized
            return (P) domLocationToProps.get(domLocation).get(count)._reset();
        }
        V view = viewInstance.select(viewClass).get();
        P props = (P) propsInstance.select(NamedLiteral.of(viewClass.getName())).get();
        props._setView(view);
        props._setWindow(owner);
        props._setDomLocation(domLocation);
        viewToPropsMap.put(view, props);
        List<Props<?>> propList = Collections.singletonList(props);
        domLocationToProps.merge(domLocation,
                                 propList,
                                 (oldList, newList) -> {
                                     if (!(oldList instanceof  ArrayList)) {
                                         ArrayList<Props<?>> out = new ArrayList<>(oldList);
                                         out.addAll(newList);
                                         return out;
                                     }
                                     else {
                                         oldList.addAll(newList);
                                         return oldList;
                                     }
                                 });
        return props;
    }

}
