package io.quarkus.qui.ui;

import java.util.List;

import io.quarkus.qui.Renderable;
import io.quarkus.qui.Props;
import io.quarkus.qui.PropsWithChildren;
import io.quarkus.qui.View;

public class VLayout implements View<VLayout.VLayoutProps> {
    public interface VLayoutProps extends PropsWithChildren<VLayoutProps, List<Props>> {
    }

    @Override
    public Renderable render(VLayoutProps props) {
        var children = get(props::children);
        return canvas -> {
            float childHeight = canvas.getBoundary().getBounds().getHeight() / children.size();
            float childWidth = canvas.getBoundary().getBounds().getWidth();
            for (int i = 0; i < children.size(); i++) {
                Props child = children.get(i);
                child.draw(canvas.getSubcanvas(0, childHeight * i, childWidth, childHeight));
            }
        };
    }
}
