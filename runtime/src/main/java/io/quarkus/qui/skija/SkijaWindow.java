package io.quarkus.qui.skija;

import java.time.Duration;

import io.quarkus.qui.Props;
import io.quarkus.qui.View;
import io.quarkus.qui.ViewManager;
import io.quarkus.qui.Window;
import io.quarkus.qui.devmode.WindowSetup;
import org.jetbrains.skija.BackendRenderTarget;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.ColorSpace;
import org.jetbrains.skija.DirectContext;
import org.jetbrains.skija.FramebufferFormat;
import org.jetbrains.skija.IRect;
import org.jetbrains.skija.Path;
import org.jetbrains.skija.Rect;
import org.jetbrains.skija.Surface;
import org.jetbrains.skija.SurfaceColorFormat;
import org.jetbrains.skija.SurfaceOrigin;
import org.jetbrains.skija.impl.Library;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class SkijaWindow implements Window {
    public long window;
    public int width;
    public int height;
    public float dpi = 1f;
    public int mouseXPos = 0;
    public int mouseYPos = 0;
    public int windowXPos = 0;
    public int windowYPos = 0;
    public boolean vsync = true;
    private String os = System.getProperty("os.name").toLowerCase();
    Props<?> props;
    SkijaWindowManager windowManager;
    WindowSetup windowSetup;
    boolean isPropsDirty = true;
    boolean shouldClose = false;
    boolean saveWindowState = false;

    public SkijaWindow(SkijaWindowManager windowManager, WindowSetup windowSetup, IRect bounds) {
        mouseXPos = bounds.getLeft();
        mouseYPos = bounds.getTop();
        width = bounds.getWidth();
        height = bounds.getHeight();
        this.windowSetup = windowSetup;
        this.windowManager = windowManager;
    }

    public void run(IRect bounds) {
        createWindow(bounds);
        loop();

        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();

        if (!saveWindowState) {
            windowSetup.removeWindow(window);
        }
        windowManager.removeWindow(window);
    }

    private void updateDimensions() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(window, width, height);

        float[] xscale = new float[1];
        float[] yscale = new float[1];
        GLFW.glfwGetWindowContentScale(window, xscale, yscale);
        assert xscale[0] == yscale[0] : "Horizontal dpi=" + xscale[0] + ", vertical dpi=" + yscale[0];

        this.width = (int) (width[0] / xscale[0]);
        this.height = (int) (height[0] / yscale[0]);
        this.dpi = xscale[0];

        System.out.println("FramebufferSize " + width[0] + "x" + height[0] + ", scale " + this.dpi + ", window " + this.width + "x" + this.height);
    }

    private void createWindow(IRect bounds) {
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE); // the window will be resizable

        window = GLFW.glfwCreateWindow(bounds.getWidth(), bounds.getHeight(), "Skija LWJGL Demo",
                                       MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
                GLFW.glfwSetWindowShouldClose(window, true);
        });

        GLFW.glfwSetWindowPos(window, bounds.getLeft(), bounds.getTop());
        updateDimensions();
        mouseXPos = width / 2;
        mouseYPos = height / 2;
        windowXPos = bounds.getLeft();
        windowYPos = bounds.getTop();

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(vsync ? 1 : 0); // Enable v-sync
        GLFW.glfwShowWindow(window);

        windowSetup.setBounds(window, bounds);
        windowSetup.setView(window, view());
        windowManager.addWindow(window, this);
    }

    private DirectContext context;
    private BackendRenderTarget renderTarget;
    private Surface surface;
    private Canvas canvas;

    private void initSkia() {
        canvas = null;
        if (surface != null) { surface.close(); surface = null; }
        if (renderTarget != null) { renderTarget.close(); renderTarget = null; }

        int fbId = GL11.glGetInteger(0x8CA6); // GL_FRAMEBUFFER_BINDING
        renderTarget = BackendRenderTarget.makeGL((int) (width * dpi),
                                                  (int) (height * dpi),
                                                  /*samples*/0,
                                                  /*stencil*/8,
                                                  fbId,
                                                  FramebufferFormat.GR_GL_RGBA8);
        // TODO load monitor profile
        surface = Surface.makeFromBackendRenderTarget(context,
                                                      renderTarget,
                                                      SurfaceOrigin.BOTTOM_LEFT,
                                                      SurfaceColorFormat.RGBA_8888,
                                                      ColorSpace.getDisplayP3());

        canvas = surface.getCanvas();
        canvas.scale(dpi, dpi);
        // canvas.translate(0.5f, 0.5f);
    }

    private long t0;
    private long[] times = new long[155];
    private int timesIdx = 0;

    private void loop() {
        GL.createCapabilities();
        if ("false".equals(System.getProperty("skija.staticLoad")))
            Library.load();
        context = DirectContext.makeGL();

        GLFW.glfwSetWindowSizeCallback(window, (window, width, height) -> {
            updateDimensions();
            initSkia();
            draw(props);
            windowSetup.setBounds(window, new IRect(windowXPos, windowYPos,
                                                    windowXPos + this.width, windowYPos + this.height));
        });

        GLFW.glfwSetWindowPosCallback(window, (window, x, y) -> {
            this.windowXPos = x;
            this.windowYPos = y;
            windowSetup.setBounds(window, new IRect(windowXPos, windowYPos,
                                                    windowXPos + width, windowYPos + height));
        });

        GLFW.glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if(os.contains("mac") || os.contains("darwin")) {
                this.mouseXPos = (int) xpos;
                this.mouseYPos = (int) ypos;
            } else {
                this.mouseXPos = (int) (xpos / dpi);
                this.mouseYPos = (int) (ypos / dpi);
            }
        });

        initSkia();
        while (!GLFW.glfwWindowShouldClose(window) && !shouldClose) {
            draw(props);
            GLFW.glfwPollEvents();
        }
    }

    public void close() {
        close(false);
    }

    public void close(boolean saveWindowState) {
        shouldClose = true;
        this.saveWindowState = saveWindowState;
    }

    @Override
    public Props<?> view() {
        return props;
    }

    @Override
    public <T extends Props<T>> T defaultView(Class<? extends View<T>> viewClass) {
        props = ViewManager.INSTANCE.createView(this.getClass(),
                                                null,
                                                this,
                                                viewClass,
                                                this);
        isPropsDirty = true;
        return (T) props;
    }

    @Override
    public Window draw(Props<?> props) {
        long t1 = System.nanoTime();
        times[timesIdx] = (t1 - t0);
        t0 = t1;
        canvas.clear(0xFFFFFFFF);
        int count = canvas.save();
        props.draw(new SkijaQuiCanvas(canvas, new Path().addRect(new Rect(0,0, width, height))));
        canvas.restoreToCount(count);
        timesIdx = (timesIdx + 1) % times.length;
        context.flush();
        GLFW.glfwSwapBuffers(window);

        if (isPropsDirty) {
            windowSetup.setView(window, props);
            isPropsDirty = false;
        }

        return this;
    }

    public long getFPS() {
        int lastIdx = (times.length + timesIdx - 1) % times.length;
        Duration nanoDuration = Duration.ofNanos(times[timesIdx] - times[lastIdx]);
        return Duration.ofSeconds(1).dividedBy(nanoDuration);
    }

    @Override
    public void waitUntilClosed() {
        run(new IRect(mouseXPos, mouseYPos, width, height));
    }
}
