package io.quarkus.qui.ui;

import java.awt.Component;

import io.quarkus.qui.Props;
import io.quarkus.qui.Renderable;
import io.quarkus.qui.View;

public class ComponentView implements View<ComponentView.ComponentViewProps> {
    public interface ComponentViewProps extends Props<ComponentViewProps> {
        ComponentViewProps component(Component component);
    }

    @Override
    public Renderable render(ComponentViewProps props) {
        var component = get(props::component);
        return canvas -> {
            canvas.drawBoundary((boundary, graphics2D) -> {
                component.paint(graphics2D);
            });
        };
    }

}
