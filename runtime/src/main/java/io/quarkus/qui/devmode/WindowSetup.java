package io.quarkus.qui.devmode;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.quarkus.qui.Props;
import org.jetbrains.skija.IRect;

public class WindowSetup {
    private Map<Long, String> windowIdToViewClassName = new ConcurrentHashMap<>();
    private Map<Long, Map<String, String>> windowIdToPropValues = new ConcurrentHashMap<>();
    private Map<Long, IRect> windowIdToBounds = new ConcurrentHashMap<>();
    private static WindowSetup INSTANCE;

    public WindowSetup() {
        if (INSTANCE == null) {
            INSTANCE = this;
        }
        else {
            windowIdToViewClassName = INSTANCE.windowIdToViewClassName;
            windowIdToPropValues = INSTANCE.windowIdToPropValues;
            windowIdToBounds = INSTANCE.windowIdToBounds;
        }
    }

    public static void setInstance(WindowSetup instance) {
        INSTANCE = instance;
    }

    public boolean hasWindowsOpened() {
        return !windowIdToViewClassName.isEmpty();
    }

    public void removeWindow(long windowId) {
        windowIdToViewClassName.remove(windowId);
        windowIdToPropValues.remove(windowId);
        windowIdToBounds.remove(windowId);
    }

    public void reset() {
        windowIdToViewClassName.clear();
        windowIdToPropValues.clear();
        windowIdToBounds.clear();
    }

    public void setBounds(long windowId, IRect bounds) {
        windowIdToBounds.put(windowId, bounds);
    }

    public void setView(long windowId, Props props) {
        windowIdToViewClassName.put(windowId, props._getView().getClass().getName());
        windowIdToPropValues.put(windowId, props._getAsMap());
    }

    public Map<Long, String> getWindowIdToViewClassName() {
        return windowIdToViewClassName;
    }

    public Map<Long, Map<String, String>> getWindowIdToPropValues() {
        return windowIdToPropValues;
    }

    public Map<Long, IRect> getWindowIdToBounds() {
        return windowIdToBounds;
    }

    public static class WindowSetupSupplier implements Supplier<WindowSetup> {
        @Override
        public WindowSetup get() {
            return Optional.ofNullable(INSTANCE).orElse(new WindowSetup());
        }
    }
}
