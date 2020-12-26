package io.quarkus.qui.ui;

import java.awt.Component;
import java.awt.Dimension;

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
                Dimension preferredSize = component.getPreferredSize();
                Dimension maximumSize = component.getMaximumSize();
                Dimension minimumSize = component.getMinimumSize();
                Dimension givenSpace = new Dimension((int) boundary.getBounds().getWidth(),
                                                     (int) boundary.getBounds().getHeight());
                if (preferredSize == null && minimumSize == null && maximumSize == null) {
                    component.setSize(givenSpace);
                } else if (preferredSize != null && minimumSize == null && maximumSize == null) {
                    double width = Math.min(givenSpace.getWidth(), preferredSize.getWidth());
                    double height = Math.min(givenSpace.getHeight(), preferredSize.getHeight());
                    component.setSize(new Dimension((int) width, (int) height));
                } else if (preferredSize != null && minimumSize != null && maximumSize == null) {
                    double width = Math.max(minimumSize.getWidth(), Math.min(givenSpace.getWidth(), preferredSize.getWidth()));
                    double height = Math.max(minimumSize.getHeight(), Math.min(givenSpace.getHeight(), preferredSize.getHeight()));
                    component.setSize(new Dimension((int) width, (int) height));
                } else if (preferredSize != null && minimumSize == null && maximumSize != null) {
                    double width = Math.min(maximumSize.getWidth(), Math.min(givenSpace.getWidth(), preferredSize.getWidth()));
                    double height = Math.max(maximumSize.getHeight(), Math.min(givenSpace.getHeight(), preferredSize.getHeight()));
                    component.setSize(new Dimension((int) width, (int) height));
                } else if (preferredSize != null && minimumSize != null && maximumSize != null) {
                    double width = Math.min(maximumSize.getWidth(), Math.max(minimumSize.getWidth(), Math.min(givenSpace.getWidth(), preferredSize.getWidth())));
                    double height = Math.min(maximumSize.getHeight(), Math.max(minimumSize.getHeight(), Math.min(givenSpace.getHeight(), preferredSize.getHeight())));
                    component.setSize(new Dimension((int) width, (int) height));
                } else if (preferredSize == null && minimumSize == null && maximumSize != null) {
                    double width = Math.min(maximumSize.getWidth(), givenSpace.getWidth());
                    double height = Math.min(maximumSize.getHeight(), givenSpace.getHeight());
                    component.setSize(new Dimension((int) width, (int) height));
                }
                else if (preferredSize == null && minimumSize != null && maximumSize == null) {
                    double width = Math.max(minimumSize.getWidth(), givenSpace.getWidth());
                    double height = Math.max(minimumSize.getHeight(), givenSpace.getHeight());
                    component.setSize(new Dimension((int) width, (int) height));
                }
                else {
                    double width = Math.min(maximumSize.getWidth(), Math.max(minimumSize.getWidth(), givenSpace.getWidth()));
                    double height = Math.min(maximumSize.getHeight(), Math.max(minimumSize.getHeight(), givenSpace.getHeight()));
                    component.setSize(new Dimension((int) width, (int) height));
                }
                component.paint(graphics2D);
            });
        };
    }

}
