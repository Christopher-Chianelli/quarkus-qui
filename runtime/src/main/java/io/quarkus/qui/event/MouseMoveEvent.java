package io.quarkus.qui.event;

import io.quarkus.qui.Props;
import io.quarkus.qui.Window;

public class MouseMoveEvent {
    public static final String ADDRESS = "io.quarkus.qui.event.MouseMoveEvent";

    Window sourceWindow;
    Props component;
    int originalLocationInWindowX;
    int originalLocationInWindowY;
    int newLocationInWindowX;
    int newLocationInWindowY;

    public MouseMoveEvent(Window sourceWindow, int originalLocationInWindowX, int originalLocationInWindowY,
                          int newLocationInWindowX, int newLocationInWindowY, Props component) {
        this.sourceWindow = sourceWindow;
        this.originalLocationInWindowX = originalLocationInWindowX;
        this.originalLocationInWindowY = originalLocationInWindowY;
        this.newLocationInWindowX = newLocationInWindowX;
        this.newLocationInWindowY = newLocationInWindowY;
        this.component = component;
    }

    public MouseMoveEvent localTo(Props newComponent) {
        return new MouseMoveEvent(sourceWindow, originalLocationInWindowX, originalLocationInWindowY,
           newLocationInWindowX, newLocationInWindowY, newComponent);
    }

    public Window getSourceWindow() {
        return sourceWindow;
    }

    public int getOriginalLocationInViewX() {
        return originalLocationInWindowX - component.getBoundary().getBounds().toIRect().getLeft();
    }

    public int getOriginalLocationInViewY() {
        return originalLocationInWindowY - component.getBoundary().getBounds().toIRect().getTop();
    }

    public int getNewLocationInViewX() {
        return newLocationInWindowX - component.getBoundary().getBounds().toIRect().getLeft();
    }

    public int getNewLocationInViewY() {
        return newLocationInWindowY - component.getBoundary().getBounds().toIRect().getTop();
    }

    public int getOriginalLocationInWindowX() {
        return originalLocationInWindowX;
    }

    public int getOriginalLocationInWindowY() {
        return originalLocationInWindowY;
    }

    public int getNewLocationInWindowX() {
        return newLocationInWindowX;
    }

    public int getNewLocationInWindowY() {
        return newLocationInWindowY;
    }

    @Override
    public String toString() {
        return String.format("MouseMoveEvent(from: (%d, %d), to (%d, %d))", originalLocationInWindowX,
                             originalLocationInWindowY, newLocationInWindowX, newLocationInWindowY);
    }
}
