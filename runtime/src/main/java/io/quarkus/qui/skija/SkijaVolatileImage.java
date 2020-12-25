package io.quarkus.qui.skija;

import org.jetbrains.skija.Surface;

public class SkijaVolatileImage extends java.awt.image.VolatileImage {
    public final int width;
    public final int height;
    public final java.awt.ImageCapabilities caps;
    public final Surface surface;

    public void log(String format, Object... args) {
//        System.out.println("VI " + String.format(format, args));
    }

    public SkijaVolatileImage(int width, int height, java.awt.ImageCapabilities caps, int transparency) {
        log("new SkiaVolatileImage(%d, %d, %s, %d)", width, height, caps, transparency);
        this.width = width;
        this.height = height;
        this.caps = caps;
        this.surface = Surface.makeRasterN32Premul(width, height);
    }

    @Override
    public java.awt.image.BufferedImage getSnapshot() {
        log("getSnapshot");
        return null;
    }

    @Override
    public int getWidth() {
        log("[+] getWidth => " + width);
        return width;
    }

    @Override
    public int getHeight() {
        log("[+] getHeight => " + height);
        return height;
    }

    @Override
    public java.awt.Graphics2D createGraphics() {
        log("createGraphics");
        return new SkijaGraphics2D(surface.getCanvas());
    }

    @Override
    public int validate(java.awt.GraphicsConfiguration gc) {
        log("validate");
        return 0;
    }

    @Override
    public boolean contentsLost() {
        log("contentLost");
        return false;
    }

    @Override
    public java.awt.ImageCapabilities getCapabilities() {
        log("[+] getCapabilities");
        return caps;
    }

    @Override
    public int getWidth(java.awt.image.ImageObserver observer) {
        log("[+] getWidth(ImageObserver) => " + width);
        return width;
    }

    @Override
    public int getHeight(java.awt.image.ImageObserver observer) {
        log("[+] getHeight(ImageObserver) => " + height);
        return height;
    }

    @Override
    public Object getProperty(String name, java.awt.image.ImageObserver observer) {
        log("getProperty(" + name + ", ImageObserver)");
        return null;
    }
}
