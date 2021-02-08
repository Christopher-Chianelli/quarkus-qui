package io.quarkus.qui.event;

public interface MouseListener {
    void onMouseClick(MouseClickEvent e);
    void onMouseMove(MouseMoveEvent e);
}
