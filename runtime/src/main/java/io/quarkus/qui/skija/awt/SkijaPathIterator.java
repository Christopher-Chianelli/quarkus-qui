package io.quarkus.qui.skija.awt;

import java.awt.geom.PathIterator;

import org.jetbrains.skija.Path;
import org.jetbrains.skija.PathFillMode;
import org.jetbrains.skija.PathSegment;
import org.jetbrains.skija.PathSegmentIterator;
import org.jetbrains.skija.PathVerb;
import org.jetbrains.skija.Point;

public class SkijaPathIterator implements PathIterator {
    PathSegmentIterator iterator;
    PathSegment current;
    PathFillMode pathFillMode;

    public SkijaPathIterator(PathSegmentIterator iterator, PathFillMode pathFillMode) {
        this.iterator = iterator;
        this.pathFillMode = pathFillMode;
    }

    @Override
    public int getWindingRule() {
        switch (pathFillMode) {
            case WINDING:
            case INVERSE_WINDING:
                return PathIterator.WIND_NON_ZERO;
            case EVEN_ODD:
            case INVERSE_EVEN_ODD:
                return PathIterator.WIND_EVEN_ODD;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean isDone() {
        return iterator.hasNext();
    }

    @Override
    public void next() {
        current = iterator.next();
    }

    @Override
    public int currentSegment(float[] coords) {
        if (current.getP0() != null) {
            coords[0] = current.getP0().getX();
            coords[1] = current.getP0().getY();
        }

        if (current.getP1() != null) {
            coords[0] = current.getP1().getX();
            coords[1] = current.getP1().getY();
        }

        if (current.getP2() != null) {
            coords[2] = current.getP2().getX();
            coords[3] = current.getP2().getY();
        }

        if (current.getP3() != null) {
            coords[4] = current.getP3().getX();
            coords[5] = current.getP3().getY();
        }

        if (current.getVerb() == PathVerb.CONIC) {
            // Not the best approximation
            Point[] quad = Path.convertConicToQuads(current.getP0(), current.getP1(), current.getP2(), current.getConicWeight(), 0);
            coords[0] = quad[0].getX();
            coords[1] = quad[0].getY();
            coords[2] = quad[1].getX();
            coords[3] = quad[1].getY();
            coords[4] = quad[2].getX();
            coords[5] = quad[2].getY();
        }

        switch (current.getVerb()) {
            case MOVE:
                return PathIterator.SEG_MOVETO;
            case LINE:
                return PathIterator.SEG_LINETO;
            case QUAD:
            case CONIC:
                return PathIterator.SEG_QUADTO;
            case CUBIC:
                return PathIterator.SEG_CUBICTO;
            case CLOSE:
            case DONE:
                return PathIterator.SEG_CLOSE;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public int currentSegment(double[] coords) {
        if (current.getP0() != null) {
            coords[0] = current.getP0().getX();
            coords[1] = current.getP0().getY();
        }

        if (current.getP1() != null) {
            coords[0] = current.getP1().getX();
            coords[1] = current.getP1().getY();
        }

        if (current.getP2() != null) {
            coords[2] = current.getP2().getX();
            coords[3] = current.getP2().getY();
        }

        if (current.getP3() != null) {
            coords[4] = current.getP3().getX();
            coords[5] = current.getP3().getY();
        }

        if (current.getVerb() == PathVerb.CONIC) {
            // Not the best approximation
            Point[] quad = Path.convertConicToQuads(current.getP0(), current.getP1(), current.getP2(), current.getConicWeight(), 0);
            coords[0] = quad[0].getX();
            coords[1] = quad[0].getY();
            coords[2] = quad[1].getX();
            coords[3] = quad[1].getY();
            coords[4] = quad[2].getX();
            coords[5] = quad[2].getY();
        }

        switch (current.getVerb()) {
            case MOVE:
                return PathIterator.SEG_MOVETO;
            case LINE:
                return PathIterator.SEG_LINETO;
            case QUAD:
            case CONIC:
                return PathIterator.SEG_QUADTO;
            case CUBIC:
                return PathIterator.SEG_CUBICTO;
            case CLOSE:
            case DONE:
                return PathIterator.SEG_CLOSE;
            default:
                throw new IllegalStateException();
        }
    }
}
