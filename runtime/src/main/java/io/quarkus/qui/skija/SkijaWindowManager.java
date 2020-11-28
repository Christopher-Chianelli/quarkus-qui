package io.quarkus.qui.skija;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.qui.ViewManager;
import io.quarkus.qui.Window;
import io.quarkus.qui.WindowManager;

@Singleton
public class SkijaWindowManager implements WindowManager {

    @Inject
    ViewManager viewManager;

    @Override
    public Window createWindow(String title) {
        return new SkijaWindow();
    }
}
