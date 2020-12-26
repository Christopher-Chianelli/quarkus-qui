package io.quarkus.qui.skija.awt;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import org.jetbrains.skija.IRect;
import org.jetbrains.skija.Matrix33;
import org.jetbrains.skija.Path;
import org.jetbrains.skija.PathFillMode;
import org.jetbrains.skija.PathOp;
import org.jetbrains.skija.Rect;

public class SkijaShape implements Shape {
    Path path;

    public SkijaShape(Path path) {
        if (path == null) {
            path = new Path();
        }
        this.path = path;
    }

    public SkijaShape(Shape shape) {
        path = new Path();
        PathIterator pathIterator = shape.getPathIterator(null);
        float[] currentSegment = new float[6];
        while (!pathIterator.isDone()) {
            pathIterator.next();
            if (pathIterator.isDone()) {
                break;
            }
            int kind = pathIterator.currentSegment(currentSegment);
            switch (kind) {
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                case PathIterator.SEG_MOVETO:
                    path.moveTo(currentSegment[0], currentSegment[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(currentSegment[0], currentSegment[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(currentSegment[0], currentSegment[1], currentSegment[2], currentSegment[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.cubicTo(currentSegment[0], currentSegment[1], currentSegment[2], currentSegment[3],
                                 currentSegment[4], currentSegment[5]);
                    break;
            }
            switch (pathIterator.getWindingRule()) {
                case java.awt.geom.Path2D.WIND_EVEN_ODD:
                    path.setFillMode(PathFillMode.EVEN_ODD);
                case java.awt.geom.Path2D.WIND_NON_ZERO:
                    path.setFillMode(PathFillMode.WINDING);
            }
        }
    }

    public Path getPath() {
        return path;
    }

    private static Rect makeRect(Rectangle2D rect) {
        return new Rect((float) rect.getMinX(), (float) rect.getMinY(),
                        (float) rect.getMaxX(), (float) rect.getMaxY());
    }

    private static Rect makeRect(double x, double y, double w, double h) {
        return new Rect((float) x, (float) y,
                        (float) (x + w), (float) (y + h));
    }

    @Override
    public Rectangle getBounds() {
        IRect bounds = path.getBounds().toIRect();
        return new Rectangle(bounds.getLeft(), bounds.getTop(),
                             bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rect bounds = path.getBounds();
        return new Rectangle2D.Float(bounds.getLeft(), bounds.getTop(),
                                     bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public boolean contains(double x, double y) {
        return path.contains((float) x, (float) y);
    }

    @Override
    public boolean contains(Point2D p) {
        return path.contains((float) p.getX(), (float) p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        Path rect = new Path().addRect(makeRect(x, y, w, h));
        Optional<Path> result = Optional.ofNullable(Path.makeCombining(path, rect, PathOp.INTERSECT));
        return !result.map(path -> !path.isEmpty()).orElse(false);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return path.conservativelyContainsRect(makeRect(x, y, w, h));
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return path.conservativelyContainsRect(makeRect(r));
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        if (at == null) {
            return new SkijaPathIterator(path.iterator(), path.getFillMode());
        }

        Path modifiedPath = new Path();
        Matrix33 transformMatrix = AffineTransformUtils.getMatrix33FromAffineTransform(at);
        path.transform(transformMatrix, modifiedPath);
        return new SkijaPathIterator(modifiedPath.iterator(), path.getFillMode());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        if (at == null) {
            return new FlatteningPathIterator(new SkijaPathIterator(path.iterator(), path.getFillMode()), flatness);
        }

        Path modifiedPath = new Path();
        Matrix33 transformMatrix = AffineTransformUtils.getMatrix33FromAffineTransform(at);
        path.transform(transformMatrix, modifiedPath);

        modifiedPath.getPoints();
        return new FlatteningPathIterator(new SkijaPathIterator(modifiedPath.iterator(), modifiedPath.getFillMode()), flatness);
    }
}
