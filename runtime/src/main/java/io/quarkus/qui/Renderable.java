package io.quarkus.qui;

import java.util.Collections;
import java.util.Map;

/**
 * Do not show this; to render a Renderable is to
 * render itself. The purpose of this class is
 * to do the actual drawing to the canvas; the
 * rest of the methods in the interface have
 * been implemented with dummy methods.
 */
public interface Renderable extends Props, View {
    @Override
    default Props _reset() {
        return this;
    }

    @Override
    default Map<String, String> _getAsMap() {
        return Collections.emptyMap();
    }

    @Override
    default void _setAsMap(Map propValues) {
        // Do nothing
    }

    @Override
    default Props _setWindow(Window window) {
        return this;
    }

    @Override
    default Window _getWindow() {
        return null;
    }

    @Override
    default View _getView() {
        return this;
    }

    @Override
    default Props render(Props props) {
        return this;
    }

    @Override
    default Props _setView(View view) {
        return this;
    }

    @Override
    default Object _get() {
        return null;
    }

    @Override
    default DomLocation _getDomLocation() {
        return null;
    }

    @Override
    default Props _setDomLocation(DomLocation domLocation) {
        return this;
    }
}
