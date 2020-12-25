package io.quarkus.qui;

import java.awt.Graphics2D;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Path;
import org.jetbrains.skija.Rect;

public interface QuiCanvas {

    /**
     * Returns the boundary of the canvas.
     *
     * If drawBoundary,
     * (0,0) is at the top-left corner of the smallest
     * rectangle containing the boundary.
     *
     * If drawInsideBoundary,
     * (0,0) is at the top-left corner of the largest
     * rectangle contained by the boundary.
     *
     * @return The boundary of this canvas.
     */
    Path getBoundary();
    QuiCanvas getSubcanvas(Path newBoundary);
    default QuiCanvas getSubcanvas(float x, float y, float w, float h) {
        return getSubcanvas(new Path().addRect(new Rect(x, y, x + w, y + h)));
    }
    default QuiCanvas getSubcanvas(Rect rect) {
        return getSubcanvas(new Path().addRect(rect));
    }
    void drawInsideBoundary(BiConsumer<Path, Graphics2D> drawer);
    void drawBoundary(BiConsumer<Path, Graphics2D> drawer);
}
