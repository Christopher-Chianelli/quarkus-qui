package io.quarkus.qui.skija.awt;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.jetbrains.skija.Paint;
import org.jetbrains.skija.PaintStrokeCap;
import org.jetbrains.skija.PaintStrokeJoin;
import org.jetbrains.skija.PathEffect;

public class StrokeUtils {
    private StrokeUtils() {}

    public static void setPaintPropertiesFromStroke(Paint paint, Stroke stroke) {
        if (stroke instanceof BasicStroke) {
            setPaintPropertiesFromBasicStroke(paint, (BasicStroke) stroke);
        } else {
            throw new IllegalArgumentException("Unrecognized Stroke Type: " + stroke.getClass());
        }
    }

    public static void setPaintPropertiesFromBasicStroke(Paint paint, BasicStroke stroke) {
        paint.setStrokeWidth(stroke.getLineWidth());
        paint.setStrokeMiter(stroke.getMiterLimit());
        switch (stroke.getEndCap()) {
            case BasicStroke.CAP_BUTT:
                paint.setStrokeCap(PaintStrokeCap.BUTT);
                break;

            case BasicStroke.CAP_ROUND:
                paint.setStrokeCap(PaintStrokeCap.ROUND);
                break;

            case BasicStroke.CAP_SQUARE:
                paint.setStrokeCap(PaintStrokeCap.SQUARE);
                break;

            default:
                throw new IllegalArgumentException();
        }

        switch (stroke.getLineJoin()) {
            case BasicStroke.JOIN_MITER:
                paint.setStrokeJoin(PaintStrokeJoin.MITER);
                break;

            case BasicStroke.JOIN_ROUND:
                paint.setStrokeJoin(PaintStrokeJoin.ROUND);
                break;

            case BasicStroke.JOIN_BEVEL:
                paint.setStrokeJoin(PaintStrokeJoin.BEVEL);
                break;

            default:
                throw new IllegalArgumentException();
        }

        if (stroke.getDashArray() != null) {
            paint.setPathEffect(PathEffect.makeDash(stroke.getDashArray(), stroke.getDashPhase()));
        }
    }

}
