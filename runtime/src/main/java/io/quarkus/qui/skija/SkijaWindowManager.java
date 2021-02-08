package io.quarkus.qui.skija;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.bootstrap.runner.DevModeMediator;
import io.quarkus.dev.spi.HotReplacementSetup;
import io.quarkus.launcher.QuarkusLauncher;
import io.quarkus.qui.Props;
import io.quarkus.qui.View;
import io.quarkus.qui.ViewManager;
import io.quarkus.qui.Window;
import io.quarkus.qui.WindowManager;
import io.quarkus.qui.devmode.HotReplacementManager;
import io.quarkus.qui.devmode.WindowSetup;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.vertx.core.eventbus.EventBus;
import org.jetbrains.skija.IRect;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

@Singleton
public class SkijaWindowManager implements WindowManager {

    @Inject
    ViewManager viewManager;

    HotReplacementManager hotReplacementManager = new HotReplacementManager(this);

    @Inject
    WindowSetup windowSetup;

    @Inject
    EventBus eventBus;

    GLFWVidMode vidmode;

    ExecutorService managedExecutor = Executors.newCachedThreadPool();

    ConcurrentLinkedQueue<SkijaWindow> windowListToCreate;
    ConcurrentLinkedQueue<Future<?>> windowFutureList;

    ConcurrentHashMap<Long, SkijaWindow> windowIdToWindow;

    @PostConstruct
    void initOpenGL() {
        windowListToCreate = new ConcurrentLinkedQueue<>();
        windowFutureList = new ConcurrentLinkedQueue<>();
        windowIdToWindow = new ConcurrentHashMap<>();
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
    }

    protected void addWindow(long id, SkijaWindow window) {
        windowIdToWindow.put(id, window);
    }

    protected void removeWindow(long id) {
        windowIdToWindow.remove(id);
    }

    @Override
    public Window createWindow(String title) {
        int width = (int) (vidmode.width() * 0.75);
        int height = (int) (vidmode.height() * 0.75);
        IRect bounds = IRect.makeXYWH(
                Math.max(0, (vidmode.width() - width) / 2),
                Math.max(0, (vidmode.height() - height) / 2),
                width,
                height);
        SkijaWindow window = new SkijaWindow(this, windowSetup, bounds, eventBus);
        if (windowFutureList.isEmpty()) {
            windowListToCreate.add(window);
        } else {
            windowFutureList.add(managedExecutor.submit(window::waitUntilClosed));
        }
        return window;
    }

    @Override
    public void waitUntilAllWindowsAreClosed() {
        if (windowSetup.hasWindowsOpened()) {
            // WindowsOpened iff Devmode restart has occurred
            System.out.println("Trying to reload from previous run...");
            List<Class> viewClassList = new ArrayList<>();
            List<Map<String,String>> propValuesList = new ArrayList<>();
            List<IRect> boundsList = new ArrayList<>();
            for (Long windowId : windowSetup.getWindowIdToViewClassName().keySet()) {
                try {
                    // Try to reload each window's top level view class
                    viewClassList.add(Thread.currentThread().getContextClassLoader().loadClass(windowSetup.getWindowIdToViewClassName().get(windowId)));
                    propValuesList.add(windowSetup.getWindowIdToPropValues().get(windowId));
                    boundsList.add(windowSetup.getWindowIdToBounds().get(windowId));
                } catch (ClassNotFoundException e) {
                    // Class cannot be found (move, deleted), so need to do the normal startup
                    System.out.println("Can't find view class " + windowSetup.getWindowIdToViewClassName().get(windowId) + ";  doing normal startup");
                    windowSetup.reset();
                    normalStartup();
                    return;
                }
            }
            windowSetup.reset();
            windowListToCreate.clear();
            System.out.println("Success! Restoring previous windows");
            for (int i = 0; i < viewClassList.size(); i++) {
                IRect bounds = boundsList.get(i);
                SkijaWindow window = new SkijaWindow(this, windowSetup, bounds, eventBus);
                window.defaultView(viewClassList.get(i))._setAsMap(propValuesList.get(i));
                windowFutureList.add(managedExecutor.submit(window::waitUntilClosed));
            }

            awaitTermination();
        }
        else {
            normalStartup();
        }
    }

    @Override
    public void _onFileChange() {
        Set<Long> windowIdSet = new HashSet<>(windowIdToWindow.keySet());

        // Close all the windows, but make sure to save
        // window state
        for (Long windowId : windowIdSet) {
            SkijaWindow window = windowIdToWindow.get(windowId);
            window.close(true);
        }

        // HACK:
        // Fake System.in to automatically send the "Enter"
        // to automatically restart Quarkus
        InputStream fakeIn = new ByteArrayInputStream("\n".getBytes());
        System.setIn(fakeIn);

        // Exit the Quarkus App (will be restarted)
        Quarkus.asyncExit(1);
    }

    private void normalStartup() {
        for (SkijaWindow window : windowListToCreate) {
            windowFutureList.add(managedExecutor.submit(window::waitUntilClosed));
        }
        windowListToCreate.clear();

        awaitTermination();
    }

    private void awaitTermination() {
        while (!windowFutureList.isEmpty()) {
            try {
                windowFutureList.poll().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
