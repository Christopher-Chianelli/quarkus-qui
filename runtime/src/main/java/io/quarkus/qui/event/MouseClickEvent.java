package io.quarkus.qui.event;

import io.quarkus.qui.Window;

public class MouseClickEvent {
    public static final String ADDRESS = "io.quarkus.qui.event.MouseClickEvent";

    Window sourceWindow;
    int clickLocationInWindowX;
    int clickLocationInWindowY;
    MouseButtons mouseButtons;

    public MouseClickEvent(Window sourceWindow, int clickLocationInWindowX, int clickLocationInWindowY,
                           MouseButtons mouseButtons) {
        this.sourceWindow = sourceWindow;
        this.clickLocationInWindowX = clickLocationInWindowX;
        this.clickLocationInWindowY = clickLocationInWindowY;
        this.mouseButtons = mouseButtons;
    }

    public Window getSourceWindow() {
        return sourceWindow;
    }

    public int getClickLocationInWindowX() {
        return clickLocationInWindowX;
    }

    public int getClickLocationInWindowY() {
        return clickLocationInWindowY;
    }

    public MouseButtons getMouseButtons() {
        return mouseButtons;
    }
}
