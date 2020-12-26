package io.quarkus.qui.skija.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.PlainView;

import org.jetbrains.skija.Bitmap;
import org.jetbrains.skija.BlendMode;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Font;
import org.jetbrains.skija.FontEdging;
import org.jetbrains.skija.FontMgr;
import org.jetbrains.skija.FontStyle;
import org.jetbrains.skija.IRect;
import org.jetbrains.skija.Matrix33;
import org.jetbrains.skija.Paint;
import org.jetbrains.skija.PaintMode;
import org.jetbrains.skija.PaintStrokeCap;
import org.jetbrains.skija.Path;
import org.jetbrains.skija.PathOp;
import org.jetbrains.skija.Rect;
import org.jetbrains.skija.Typeface;

public class SkijaGraphics2D extends Graphics2D {
    private static final java.awt.Font defaultFont = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, 12);
    private static final Font defaultSkiaFont = skiaFont(defaultFont);
    public Matrix33 prevMatrix = Matrix33.IDENTITY;
    public Path prevClip = null;

    public java.awt.Color color;
    public java.awt.Color backgroundColor;
    public java.awt.Font font = defaultFont;
    public Path clip = null;
    public Font skiaFont = defaultSkiaFont;

    public final Canvas canvas;
    public final Paint paint = new Paint().setColor(0xFFFFFFFF).setStrokeWidth(1).setStrokeCap(PaintStrokeCap.SQUARE);
    private final RenderingHints renderingHints = new RenderingHints(null);
    private java.awt.Paint awtPaint = Color.WHITE;
    private Stroke stroke = new BasicStroke();
    public final Paint backgroundPaint = new Paint().setColor(0xFF000000);
    public Matrix33 matrix;

    public SkijaGraphics2D(Canvas canvas) { this(canvas, Matrix33.IDENTITY, null, null, null); }

    public SkijaGraphics2D(Canvas canvas, Matrix33 matrix, Path clip, java.awt.Font font, Font skiaFont) {
        this.canvas = canvas;
        this.matrix = matrix;
        this.clip = clip;
        this.font = font;
        this.skiaFont = skiaFont;
    }

    public SkijaGraphics2D(SkijaGraphics2D orig) {
        this.prevMatrix = orig.prevMatrix;
        this.prevClip = orig.prevClip;

        this.color = orig.color;
        this.backgroundColor = orig.backgroundColor;
        this.font = orig.font;
        this.skiaFont = orig.skiaFont;

        this.canvas = orig.canvas;
        this.renderingHints.putAll(orig.renderingHints);

        this.awtPaint = orig.awtPaint;
        this.stroke = orig.stroke;
        this.matrix = orig.matrix;

        PaintUtils.clonePaint(orig.paint, paint);
        PaintUtils.clonePaint(orig.backgroundPaint, backgroundPaint);

        // Do NOT clone the clip; it causes rendering issues
        this.clip = null;
    }

    private static Path getPathFromShape(Shape shape) {
        if (shape instanceof SkijaShape) {
            return ((SkijaShape) shape).getPath();
        } else {
            return new SkijaShape(shape).getPath();
        }
    }

    public void beforeDraw() {
        if (prevMatrix != matrix || prevClip != clip) {
            canvas.restore();
            canvas.save();
            canvas.concat(matrix);
            if (clip != null)
                canvas.clipPath(clip);
            prevMatrix = matrix;
            prevClip = clip;
        }
    }

    private void stroke(Runnable stroker) {
        beforeDraw();
        paint.setMode(PaintMode.STROKE);
        stroker.run();
    }

    private void fill(Runnable filler) {
        beforeDraw();
        paint.setMode(PaintMode.FILL);
        filler.run();
    }

    @Override
    public void draw(java.awt.Shape s) {
        stroke(() -> canvas.drawPath(getPathFromShape(s), paint));
    }

    @Override
    public boolean drawImage(java.awt.Image img, java.awt.geom.AffineTransform xform, java.awt.image.ImageObserver obs) {
        // TODO
        return false;
    }

    @Override
    public void drawImage(java.awt.image.BufferedImage img, java.awt.image.BufferedImageOp op, int x, int y) {
        // TODO
    }

    @Override
    public void drawRenderedImage(java.awt.image.RenderedImage img, java.awt.geom.AffineTransform xform) {
        // TODO
    }

    @Override
    public void drawRenderableImage(java.awt.image.renderable.RenderableImage img, java.awt.geom.AffineTransform xform) {
        // TODO
    }

    @Override
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        if (font == null)
            setFont(defaultFont);
        beforeDraw();
        canvas.drawString(str, x, y, skiaFont, paint);
    }

    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, int x, int y) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, float x, float y) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void drawGlyphVector(java.awt.font.GlyphVector g, float x, float y) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void fill(java.awt.Shape s) {
        fill(() -> canvas.drawPath(getPathFromShape(s), paint));
    }

    @Override
    public boolean hit(java.awt.Rectangle rect, java.awt.Shape s, boolean onStroke) {
        Path intersection = Path.makeCombining(getPathFromShape(rect), getPathFromShape(s),
                                               PathOp.INTERSECT);
        if (intersection == null) {
            return false;
        } else {
            return !intersection.isEmpty();
        }
    }

    @Override
    public java.awt.GraphicsConfiguration getDeviceConfiguration() {
        return SkijaGraphicsConfig.INSTANCE;
    }

    @Override
    public void setComposite(java.awt.Composite comp) {
        // TODO ?
        if (comp == java.awt.AlphaComposite.Clear) {
            paint.setBlendMode(BlendMode.CLEAR);
        }
        else if (comp == java.awt.AlphaComposite.SrcOver) {
            paint.setBlendMode(BlendMode.SRC_OVER);
        }
        else if (comp instanceof java.awt.AlphaComposite) {
            throw new UnsupportedOperationException(" UNKNOWN COMPOSITE MODE " + ((java.awt.AlphaComposite) comp).getRule());
        }
    }

    @Override
    public void setPaint(java.awt.Paint paint) {
        PaintUtils.setSkijaPaintPropertiesFromAWTPaint(this.paint, paint);
        awtPaint = paint;
    }

    @Override
    public void setStroke(java.awt.Stroke s) {
        StrokeUtils.setPaintPropertiesFromStroke(paint, s);
        stroke = s;
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        renderingHints.put(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(java.awt.RenderingHints.Key hintKey) {
        return renderingHints.get(hintKey);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        renderingHints.clear();
        renderingHints.putAll(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        renderingHints.putAll(hints);
    }

    @Override
    public java.awt.RenderingHints getRenderingHints() {
        return renderingHints;
    }

    @Override
    public void translate(int x, int y) {
        translate((double) x, (double) y);
    }

    @Override
    public void translate(double tx, double ty) {
        matrix = matrix.makeConcat(Matrix33.makeTranslate((float) tx, (float) ty));
        if (clip != null) {
            Path newClip = new Path();
            clip.transform(Matrix33.makeTranslate((float) -tx, (float) -ty), newClip);
            clip = newClip;
        }
    }

    @Override
    public void rotate(double theta) {
        double degrees = Math.toDegrees(theta);
        matrix = matrix.makeConcat(Matrix33.makeRotate((float) degrees));
        if (clip != null) {
            Path newClip = new Path();
            clip.transform(Matrix33.makeRotate((float) -degrees), newClip);
            clip = newClip;
        }
    }

    @Override
    public void rotate(double theta, double x, double y) {
        double degrees = Math.toDegrees(theta);
        matrix = matrix.makeConcat(Matrix33.makeTranslate((float) x, (float) y))
                       .makeConcat(Matrix33.makeRotate((float) degrees))
                       .makeConcat(Matrix33.makeTranslate((float) -x, (float) -y));
        if (clip != null) {
            Path newClip = new Path();
            Matrix33 inverse = Matrix33.makeTranslate((float) x, (float) y)
                    .makeConcat(Matrix33.makeRotate((float) -degrees))
                    .makeConcat(Matrix33.makeTranslate((float) -x, (float) -y));
            clip.transform(inverse, newClip);
            clip = newClip;
        }
    }

    @Override
    public void scale(double sx, double sy) {
        matrix = matrix.makeConcat(Matrix33.makeScale((float) sx, (float) sy));
        if (clip != null) {
            Path newClip = new Path();
            clip.transform(Matrix33.makeScale((float) (1 / sx), (float) (1 / sy)), newClip);
            clip = newClip;
        }
    }

    @Override
    public void shear(double shx, double shy) {
        matrix = matrix.makeConcat(Matrix33.makeSkew((float) shx, (float) shy));
        if (clip != null) {
            Path newClip = new Path();
            clip.transform(Matrix33.makeSkew((float) (1 / shx), (float) (1 / shy)), newClip);
            clip = newClip;
        }
    }

    @Override
    public void transform(java.awt.geom.AffineTransform Tx) {
        Matrix33 transformMatrix = AffineTransformUtils.getMatrix33FromAffineTransform(Tx);
        matrix = matrix.makeConcat(transformMatrix);
        if (clip != null && Tx.getDeterminant() != 0) {
            try {
                Path newClip = new Path();
                Matrix33 inverseMatrix = AffineTransformUtils.getMatrix33FromAffineTransform(Tx.createInverse());
                clip.transform(inverseMatrix, newClip);
                clip = newClip;
            } catch (NoninvertibleTransformException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void setTransform(java.awt.geom.AffineTransform Tx) {
        Matrix33 transformMatrix = AffineTransformUtils.getMatrix33FromAffineTransform(Tx);
        AffineTransform currentTransformAsAffine = AffineTransformUtils.getAffineTransformFromMatrix33(matrix);

        // First, undo the old transform done to the clip
        if (clip != null && currentTransformAsAffine.getDeterminant() != 0) {
            try {
                Path newClip = new Path();
                Matrix33 inverseMatrix = AffineTransformUtils.getMatrix33FromAffineTransform(currentTransformAsAffine.createInverse());
                clip.transform(inverseMatrix, newClip);
                clip = newClip;
            } catch (NoninvertibleTransformException e) {
                throw new IllegalStateException(e);
            }
        }
        matrix = transformMatrix;

        // Next, apply the new transform to the clip
        if (clip != null) {
            Path newClip = new Path();
            clip.transform(transformMatrix, newClip);
            clip = newClip;
        }
    }

    @Override
    public java.awt.geom.AffineTransform getTransform() {
        float[] parts = matrix.getMat();
        return new AffineTransform(parts[0], parts[2], parts[1], parts[3], parts[4], parts[5]);
    }

    @Override
    public java.awt.Paint getPaint() {
        return awtPaint;
    }

    @Override
    public java.awt.Composite getComposite() {
        // TODO
        return null;
    }

    @Override
    public void setBackground(java.awt.Color color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color.getRGB());
    }

    @Override
    public java.awt.Color getBackground() {
        return backgroundColor;
    }

    @Override
    public java.awt.Stroke getStroke() {
        return stroke;
    }

    @Override
    public void clip(java.awt.Shape s) {
        if (s instanceof java.awt.Rectangle) {
            var r = (java.awt.Rectangle) s;
            clipRect(r.x, r.y, r.width, r.height);
        } else {
            if (clip != null) {
                Path shapePath = getPathFromShape(s);
                Path newClip = Path.makeCombining(clip, shapePath, PathOp.INTERSECT);
                clip = Objects.requireNonNullElseGet(newClip, Path::new);
            } else {
                clip = getPathFromShape(s);
            }
        }
    }

    @Override
    public java.awt.font.FontRenderContext getFontRenderContext() {
        return new FontRenderContext(AffineTransformUtils.getAffineTransformFromMatrix33(matrix),
                                     skiaFont.getEdging() != FontEdging.ALIAS,
                                     skiaFont.isSubpixel());
    }

    @Override
    public java.awt.Graphics create() {
        return new SkijaGraphics2D(this);
    }

    @Override
    public java.awt.Color getColor() {
        return color;
    }

    @Override
    public void setColor(java.awt.Color c) {
        this.color = c;
        paint.setColor(c == null ? 0xFFFFFFFF : c.getRGB());
        paint.setShader(null);
        awtPaint = c;
    }

    @Override
    public void setPaintMode() {
        // TODO
    }

    @Override
    public void setXORMode(java.awt.Color c1) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public java.awt.Font getFont() {
        if (font == null)
            setFont(defaultFont);
        return font;
    }

    @Override
    public void setFont(java.awt.Font font) {
        if (this.font != font) {
            this.font = font;

            var cachedFont = fontCache.get(font);
            if (cachedFont == null) {
                cachedFont = skiaFont(font);
                fontCache.put(font, cachedFont);
            }

            skiaFont = cachedFont;
        }
    }

    public static final Map<java.awt.Font, Font> fontCache = new ConcurrentHashMap<>();

    public static Font skiaFont(java.awt.Font font) {
        FontStyle style;
        if (font.getStyle() == java.awt.Font.PLAIN) {
            style = FontStyle.NORMAL;
        }
        else if (font.getStyle() == java.awt.Font.BOLD) {
            style = FontStyle.BOLD;
        }
        else if (font.getStyle() == java.awt.Font.ITALIC) {
            style = FontStyle.ITALIC;
        }
        else if (font.getStyle() == java.awt.Font.BOLD + java.awt.Font.ITALIC) {
            style = FontStyle.BOLD_ITALIC;
        }
        else {
            throw new RuntimeException("Unknown font style: " + font.getStyle() + " in " + font);
        }

        var typeface = FontMgr.getDefault().matchFamiliesStyle(new String[] {"System Font", "Segoe UI", "Ubuntu"}, style);
        if (typeface == null) {
            typeface = Typeface.makeDefault();
        }
        return new Font(typeface, font.getSize());
    }

    @Override
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) {
        return new FontMetrics(f) {
            @Override
            public java.awt.Font getFont() {
                return super.getFont();
            }
        };
    }

    @Override
    public java.awt.Rectangle getClipBounds() {
        if (clip == null) {
            return new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        IRect bounds = clip.getBounds().toIRect();
        return new Rectangle(bounds.getLeft(), bounds.getTop(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        if (clip == null) {
            setClip(x, y, width, height);
        }
        else {
            Path newClip = Path.makeCombining(clip, getPathFromShape(new Rectangle(x,y,width,height)), PathOp.INTERSECT);
            clip = Objects.requireNonNullElseGet(newClip, Path::new);
        }
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        clip = getPathFromShape(new java.awt.Rectangle(x, y, width, height));
    }

    @Override
    public java.awt.Shape getClip() {
        return new SkijaShape(clip);
    }

    @Override
    public void setClip(java.awt.Shape shapeClip) {
        if (shapeClip != null) {
            clip = getPathFromShape(shapeClip);
        } else {
            clip = null;
        }
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        Bitmap copiedArea = new Bitmap();
        copiedArea.allocN32Pixels(width, height);
        canvas.readPixels(copiedArea, x, y);
        canvas.writePixels(copiedArea, x + dx, y + dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        beforeDraw();
        fill(() -> canvas.drawLine(x1, y1, x2, y2, paint));
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        beforeDraw();
        fill(() -> canvas.drawRect(Rect.makeXYWH(x, y, width, height), paint));
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        beforeDraw();
        fill(() -> canvas.drawRect(Rect.makeXYWH(x,y, width, height),
                        new Paint()
                                .setMode(PaintMode.FILL)
                                .setColor(backgroundColor.getRGB())));
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        fill(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        stroke(() -> canvas.drawOval(Rect.makeXYWH(x, y, width, height), paint));
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        fill(() -> canvas.drawOval(Rect.makeXYWH(x, y, width, height), paint));
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        stroke(() -> canvas.drawArc(x, y, width, height, startAngle, arcAngle, false, paint));
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        fill(() -> canvas.drawArc(x, y, width, height, startAngle, arcAngle, false, paint));
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        stroke(() -> {
            try (var path = new Path()) {
                path.moveTo(xPoints[0], yPoints[0]);
                for (int i = 1; i < nPoints; ++i) {
                    path.lineTo(xPoints[i], yPoints[i]);
                }
                canvas.drawPath(path, paint);
            }
        });
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        stroke(() -> {
            try (var path = new Path()) {
                path.moveTo(xPoints[0], yPoints[0]);
                for (int i = 1; i < nPoints; ++i) {
                    path.lineTo(xPoints[i], yPoints[i]);
                }
                path.lineTo(xPoints[0], yPoints[0]);
                path.closePath();
                canvas.drawPath(path, paint);
            }
        });
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        fill(() -> {
            try (var path = new Path()) {
                path.moveTo(xPoints[0], yPoints[0]);
                for (int i = 1; i < nPoints; ++i) {
                    path.lineTo(xPoints[i], yPoints[i]);
                }
                path.lineTo(xPoints[0], yPoints[0]);
                path.closePath();
                canvas.drawPath(path, paint);
            }
        });
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.image.ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(null), img.getHeight(null), null, observer);
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.image.ImageObserver observer) {
        return drawImage(img, x, y, x + width, y + height, 0, 0, width, height, null, observer);
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(null), img.getHeight(null), bgcolor, observer);
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) {
        return drawImage(img, x, y, x + width, y + height, 0, 0, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.image.ImageObserver observer) {
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
    }

    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) {
        if (img instanceof SkijaVolatileImage) {
            beforeDraw();
            try (org.jetbrains.skija.Image image = ((SkijaVolatileImage) img).surface.makeImageSnapshot()) {
                canvas.drawImageIRect(image, IRect.makeLTRB(sx1, sy1, sx2, sy2), Rect.makeLTRB(dx1, dy1, dx2, dy2));
            }
        } else if (img instanceof java.awt.image.AbstractMultiResolutionImage) {
            var variant = ((java.awt.image.AbstractMultiResolutionImage) img).getResolutionVariants().get(0);
            if (variant instanceof java.awt.image.BufferedImage) {
                drawImage(variant, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
            } else {
                throw new UnsupportedOperationException("Unrecognized variant: " + variant.getClass());
            }
        } else if (img instanceof java.awt.image.BufferedImage) {
            beforeDraw();
            java.awt.image.BufferedImage bi = (java.awt.image.BufferedImage) img;
            canvas.drawBitmapIRect(skBitmap(bi), IRect.makeLTRB(sx1, sy1, sx2, sy2), Rect.makeLTRB(dx1, dy1, dx2, dy2));
        } else {
            throw new UnsupportedOperationException("Unrecognized variant: " + img.getClass());
        }
        return false;
    }

    public static final Map<java.awt.image.BufferedImage, Bitmap> rasterCache = new ConcurrentHashMap<>();

    public static Bitmap skBitmap(java.awt.image.BufferedImage bi) {
        Bitmap b = rasterCache.get(bi);
        if (b != null)
            return b;

        b = new Bitmap();
        b.allocN32Pixels(bi.getWidth(), bi.getHeight());
        for (int x = 0; x < bi.getWidth(); ++x) {
            for (int y = 0; y < bi.getHeight(); ++y) {
                int color = bi.getRGB(x, y);
                b.erase(color, IRect.makeXYWH(x, y, 1, 1));
            }
        }
        rasterCache.put(bi, b);
        return b;
    }

    @Override
    public void dispose() {
        paint.close();
        backgroundPaint.close();
    }
}
