package io.quarkus.qui.skija.awt;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.util.Arrays;

import org.jetbrains.skija.Bitmap;
import org.jetbrains.skija.FilterTileMode;
import org.jetbrains.skija.GradientStyle;
import org.jetbrains.skija.Matrix33;
import org.jetbrains.skija.Paint;
import org.jetbrains.skija.Shader;

public class PaintUtils {
    private PaintUtils() {}

    public static void setSkijaPaintPropertiesFromAWTPaint(Paint paint, java.awt.Paint awtPaint) {
        if (awtPaint instanceof Color) {
            setSkijaPaintPropertiesFromColor(paint, (Color) awtPaint);
        }
        else if (awtPaint instanceof LinearGradientPaint) {
            setSkijaPaintPropertiesFromLinearGradient(paint, (LinearGradientPaint) awtPaint);
        } else if (awtPaint instanceof GradientPaint) {
            setSkijaPaintPropertiesFromGradientPaint(paint, (GradientPaint) awtPaint);
        }
        else if (awtPaint instanceof RadialGradientPaint) {
            setSkijaPaintPropertiesFromRadialGradientPaint(paint, (RadialGradientPaint) awtPaint);
        } else if (awtPaint instanceof TexturePaint) {
            setSkijaPaintPropertiesFromTexturePaint(paint, (TexturePaint) awtPaint);
        }
        else {
            throw new UnsupportedOperationException("setPaint " + paint);
        }
    }

    public static void setSkijaPaintPropertiesFromColor(Paint paint, Color color) {
        paint.setColor(color == null ? 0xFFFFFFFF : color.getRGB());
        paint.setShader(null);
    }

    public static void setSkijaPaintPropertiesFromLinearGradient(Paint paint, LinearGradientPaint gr) {
        int[] colors = new int[gr.getColors().length];
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = gr.getColors()[i].getRGB();
        }

        var shader = Shader.makeLinearGradient((float) gr.getStartPoint().getX(),
                                               (float) gr.getStartPoint().getY(),
                                               (float) gr.getEndPoint().getX(),
                                               (float) gr.getEndPoint().getY(),
                                               colors,
                                               gr.getFractions());
        paint.setShader(shader);
    }

    public static void setSkijaPaintPropertiesFromGradientPaint(Paint paint, GradientPaint gradientPaint) {
        Shader shader;

        if (gradientPaint.isCyclic()) {
            shader = Shader.makeLinearGradient(
                    PointUtils.toSkijaPoint(gradientPaint.getPoint1()),
                    PointUtils.toSkijaPoint(gradientPaint.getPoint2()),
                    new int[] {
                            gradientPaint.getColor1().getRGB(),
                            gradientPaint.getColor2().getRGB()
                    },
                    new float[] {0, 1},
                    new GradientStyle(FilterTileMode.MIRROR, true, Matrix33.IDENTITY)
            );
        } else {
            shader = Shader.makeLinearGradient(
                    PointUtils.toSkijaPoint(gradientPaint.getPoint1()),
                    PointUtils.toSkijaPoint(gradientPaint.getPoint2()),
                    new int[] {
                            gradientPaint.getColor1().getRGB(),
                            gradientPaint.getColor2().getRGB()
                    },
                    new float[] {0, 1},
                    new GradientStyle(FilterTileMode.CLAMP, true, Matrix33.IDENTITY)
            );
        }
        paint.setShader(shader);
    }

    public static void setSkijaPaintPropertiesFromRadialGradientPaint(Paint paint,
                                                                      RadialGradientPaint radialGradientPaint) {
        GradientStyle gradientStyle;
        switch (radialGradientPaint.getCycleMethod()) {
            case NO_CYCLE:
                gradientStyle = new GradientStyle(FilterTileMode.CLAMP, true, AffineTransformUtils.getMatrix33FromAffineTransform(radialGradientPaint.getTransform()));
                break;
            case REFLECT:
                gradientStyle = new GradientStyle(FilterTileMode.MIRROR, true, AffineTransformUtils.getMatrix33FromAffineTransform(radialGradientPaint.getTransform()));
                break;
            case REPEAT:
                gradientStyle = new GradientStyle(FilterTileMode.REPEAT, true, AffineTransformUtils.getMatrix33FromAffineTransform(radialGradientPaint.getTransform()));
                break;

            default:
                throw new IllegalStateException();
        }
        Shader shader = Shader.makeRadialGradient(PointUtils.toSkijaPoint(radialGradientPaint.getCenterPoint()),
                                                  radialGradientPaint.getRadius(),
                                                  Arrays.stream(radialGradientPaint.getColors()).mapToInt(Color::getRGB)
                                                          .toArray(),
                                                  radialGradientPaint.getFractions(),
                                                  gradientStyle
        );
        paint.setShader(shader);
    }

    public static void setSkijaPaintPropertiesFromTexturePaint(Paint paint, TexturePaint texturePaint) {
        Bitmap bitmap = SkijaGraphics2D.skBitmap(texturePaint.getImage());
        float scaleX = (float) (texturePaint.getImage().getWidth() / texturePaint.getAnchorRect().getWidth());
        float scaleY = (float) (texturePaint.getImage().getHeight() / texturePaint.getAnchorRect().getHeight());
        float dx = (float) texturePaint.getAnchorRect().getX();
        float dy = (float) texturePaint.getAnchorRect().getY();
        Matrix33 localMatrix = Matrix33.makeTranslate(dx, dy).makePreScale(scaleX, scaleY);
        Shader shader = bitmap.makeShader(FilterTileMode.REPEAT, FilterTileMode.REPEAT, localMatrix);
        paint.setShader(shader);
    }
}
