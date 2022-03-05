package org.ru.img;

/**
 * An AbstractPixel is a mathematical point representation of a pixel.
 * The `x` and `y` coordinates represent the position of the pixel.
 * The `grayscaleValue` is how dark the pixel is. 0 being pure white
 * and 1 being pure black
 *
 * `isDud` denotes if this pixel exists only for balancing purposes. Since the
 * number of nodes on each side of the bipartite graph (used in OT) must be the
 * same. If this pixel is a dud, it is ignored when computing the cost matrix.
 * That is, any edge attached to this pixel has a cost of 0
 */
public record AbstractPixel(double x, double y, double grayscaleValue, boolean isDud) {
    public AbstractPixel {
        // Duds should also have a supply/demand of 0. Otherwise, you're
        // simply going to get a wrong answer, no way around it.
        assert !isDud || grayscaleValue == 0.0;
    }

    public AbstractPixel(double x, double y, double grayscaleValue) {
        this(x, y, grayscaleValue, false);
    }
}
