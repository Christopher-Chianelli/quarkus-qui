package io.quarkus.qui;

import java.util.Arrays;
import java.util.Collection;

import io.quarkus.qui.annotations.CollectionProp;

public interface PropsWithChildren<T extends PropsWithChildren<T, C>, C extends Collection<Props>> extends Props<PropsWithChildren<T,C>> {
    @CollectionProp
    T children(C children);

    default T withChildren(Props... children) {
        ((C) children(null)._get()).addAll(Arrays.asList(children));
        return (T) this;
    }
    default T withChild(Props child) {
        return withChildren(child);
    }
}
