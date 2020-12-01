package io.quarkus.qui.skija;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.qui.ViewManager;
import io.quarkus.qui.Window;
import io.quarkus.qui.WindowManager;
import org.jetbrains.skija.IRect;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

@Singleton
public class SkijaWindowManager implements WindowManager {

    @Inject
    ViewManager viewManager;

    GLFWVidMode vidmode;

    @PostConstruct
    void initOpenGL() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
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
        return new SkijaWindow(bounds);
    }
}
