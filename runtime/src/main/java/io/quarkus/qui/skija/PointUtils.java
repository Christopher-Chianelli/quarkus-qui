package io.quarkus.qui.skija;

import java.awt.geom.Point2D;

import org.jetbrains.skija.Point;

public class PointUtils {
    private PointUtils() {}

    public static Point toSkijaPoint(java.awt.Point point) {
        return new Point(point.x, point.y);
    }

    public static Point toSkijaPoint(Point2D point) {
        return new Point((float) point.getX(), (float) point.getY());
    }
}
