package io.quarkus.qui.ui;

import java.util.Arrays;
import java.util.List;

import io.quarkus.qui.Drawable;
import io.quarkus.qui.Props;
import io.quarkus.qui.PropsWithChildren;
import io.quarkus.qui.View;

public class VLayout implements View<VLayout.VLayoutProps> {
    public interface VLayoutProps extends PropsWithChildren<VLayoutProps, List<Props>> {
    }

    @Override
    public Drawable render(VLayoutProps props) {
        var children = get(props::children);
        return canvas -> {
            children.forEach(child -> {
                child._getView().render(child).draw(canvas);
            });
        };
    }
}
