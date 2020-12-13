package io.quarkus.qui.example;

import java.util.Arrays;

import javax.inject.Inject;

import io.quarkus.qui.ViewManager;
import io.quarkus.qui.Window;
import io.quarkus.qui.WindowManager;
import io.quarkus.qui.skija.SkijaWindowManager;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class MyMain implements QuarkusApplication {

    @Inject
    ViewManager viewManager;

    @Inject
    WindowManager windowManager;

    @Override
    public int run(String... args) throws Exception {
        Window window = windowManager.createWindow("My Window");
        window.defaultView(TodoList.class)
              .todo(Arrays.asList("Task 1", "A very long task", "A task that \n span multiple lines"));
        windowManager.waitUntilAllWindowsAreClosed();
        return 0;
    }
}
