package io.quarkus.qui.event;

public class InputEvent implements Comparable<InputEvent> {
    final String ID;

    public InputEvent(String id) {
        this.ID = id;
    }

    @Override
    public int compareTo(InputEvent o) {
        return ID.compareTo(o.ID);
    }
}
