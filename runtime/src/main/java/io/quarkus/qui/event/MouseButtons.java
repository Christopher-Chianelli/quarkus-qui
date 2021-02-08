package io.quarkus.qui.event;

import org.lwjgl.glfw.GLFW;

public final class MouseButtons {
    static final int LEFT_BUTTON = (1 << GLFW.GLFW_MOUSE_BUTTON_1);
    static final int RIGHT_BUTTON = (1 << GLFW.GLFW_MOUSE_BUTTON_2);
    static final int MIDDLE_BUTTON = (1 << GLFW.GLFW_MOUSE_BUTTON_3);

    final int eventButton;
    final int buttonDownBitfield;

    public MouseButtons() {
        buttonDownBitfield = 0;
        eventButton = 0;
    }

    public MouseButtons(int buttonDownBitfield, int eventButton) {
        this.buttonDownBitfield = buttonDownBitfield;
        this.eventButton = eventButton;
    }

    public MouseButtons afterEvent(int button, int event) {
        if (event == GLFW.GLFW_RELEASE) {
            return new MouseButtons(buttonDownBitfield & ~(1 << button), button);
        } else {
            return new MouseButtons(buttonDownBitfield | (1 << button), button);
        }
    }

    public boolean isLeftButtonDown() {
        return (buttonDownBitfield & LEFT_BUTTON) > 0;
    }

    public boolean isRightButtonDown() {
        return (buttonDownBitfield & RIGHT_BUTTON) > 0;
    }

    public boolean isMiddleButtonDown() {
        return (buttonDownBitfield & MIDDLE_BUTTON) > 0;
    }

    public int getButtonDownBitfield() {
        return buttonDownBitfield;
    }

    public int getEventButton() {
        return eventButton;
    }
}
