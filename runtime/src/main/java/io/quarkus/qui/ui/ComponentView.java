package io.quarkus.qui.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.SwingUtilities;

import io.quarkus.qui.Props;
import io.quarkus.qui.Renderable;
import io.quarkus.qui.View;
import io.quarkus.qui.event.MouseClickEvent;
import io.quarkus.qui.event.MouseListener;
import io.quarkus.qui.event.MouseMoveEvent;
import io.quarkus.vertx.ConsumeEvent;
import org.jetbrains.skija.IRect;

public class ComponentView implements View<ComponentView.ComponentViewProps>, MouseListener {

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

    @Override
    @ConsumeEvent(MouseClickEvent.ADDRESS)
    public void onMouseClick(MouseClickEvent event) {
        System.out.println("Got event Click");
        var props = getProps();
        Component component = get(props::component);

        if (component != null) {
            var awtEvent = new java.awt.event.MouseEvent(component, 0, System.currentTimeMillis(),
                                                         0, event.getClickLocationInWindowX(),
                                                         event.getClickLocationInWindowY(),
                                                         1, false,
                                                         event.getMouseButtons().getEventButton());
            SwingUtilities.invokeLater(() -> {
                component.dispatchEvent(awtEvent);
            });
        }
    }

    @Override
    @ConsumeEvent(MouseMoveEvent.ADDRESS)
    public void onMouseMove(MouseMoveEvent event) {
        var props = getProps();
        Component component = get(props::component);

        if (component != null) {
            // component.getComponentAt()
            var awtEvent = new java.awt.event.MouseEvent(component, 0, System.currentTimeMillis(),
                                                         0, event.getNewLocationInViewX(),
                                                         event.getNewLocationInViewY(),
                                                         0, false, 0);
            SwingUtilities.invokeLater(() -> {
                component.dispatchEvent(awtEvent);
            });
        }
    }

}
