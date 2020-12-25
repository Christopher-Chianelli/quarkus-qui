package io.quarkus.qui.skija;

import java.awt.geom.AffineTransform;

import org.jetbrains.skija.Matrix33;

public class AffineTransformUtils {
    private AffineTransformUtils() {}

    public static Matrix33 getMatrix33FromAffineTransform(AffineTransform affineTransform) {
        double[] flatMatrix = new double[6];
        affineTransform.getMatrix(flatMatrix);
        return new Matrix33((float) flatMatrix[0], (float) flatMatrix[2], (float) flatMatrix[4],
                            (float) flatMatrix[1], (float) flatMatrix[3], (float) flatMatrix[5],
                            0, 0, 1);
    }

    public static AffineTransform getAffineTransformFromMatrix33(Matrix33 matrix33) {
        float[] flatMatrix = matrix33.getMat();
        return new AffineTransform(flatMatrix[0], flatMatrix[4], flatMatrix[1],
                                   flatMatrix[3], flatMatrix[2], flatMatrix[5]);
    }
}
