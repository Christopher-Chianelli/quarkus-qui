package io.quarkus.qui.skija;

public class SkijaGraphicsConfig extends java.awt.GraphicsConfiguration {
    public static final SkijaGraphicsConfig INSTANCE = new SkijaGraphicsConfig();

    public void log(String format, String... args) {
//        System.out.println("GC " + String.format(format, args));
    }

    @Override
    public java.awt.GraphicsDevice getDevice() {
        log("getDevice");
        return null;
    }

    @Override
    public java.awt.image.ColorModel getColorModel() {
        log("getColorModel");
        return null;
    }

    @Override
    public java.awt.image.ColorModel getColorModel(int transparency) {
        log("getColorModel(int)");
        return null;
    }

    @Override
    public java.awt.geom.AffineTransform getDefaultTransform() {
        log("getDefaultTransform");
        return null;
    }

    @Override
    public java.awt.geom.AffineTransform getNormalizingTransform() {
        log("getNormalizingTransform");
        return null;
    }

    @Override
    public java.awt.Rectangle getBounds() {
        log("getBounds");
        return null;
    }

    @Override
    public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height, java.awt.ImageCapabilities caps, int transparency) throws java.awt.AWTException {
        log("[+] createCompatibleVolatileImage " + width + "x" + height + " " + caps + " " + transparency);
        return new SkijaVolatileImage(width, height, caps, transparency);
    }
}
