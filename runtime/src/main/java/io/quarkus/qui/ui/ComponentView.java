package io.quarkus.qui.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.SwingUtilities;

import io.quarkus.qui.Props;
import io.quarkus.qui.Renderable;
import io.quarkus.qui.View;
import org.jetbrains.skija.IRect;

public class ComponentView implements View<ComponentView.ComponentViewProps> {
    public interface ComponentViewProps extends Props<ComponentViewProps> {
        ComponentViewProps component(Component component);
    }

    @Override
    public Renderable render(ComponentViewProps props) {
        var component = get(props::component);
        return canvas -> {
            canvas.drawBoundary((boundary, graphics2D) -> {
                IRect bounds = boundary.getBounds().toIRect();
                Dimension givenSpace = new Dimension((int) boundary.getBounds().getWidth(),
                                                     (int) boundary.getBounds().getHeight());
                Container container = new Container();
                container.setSize(givenSpace);
                SwingUtilities.paintComponent(graphics2D, component, container, bounds.getLeft(), bounds.getTop(),
                                              bounds.getWidth(), bounds.getHeight());
            });
        };
    }

}
