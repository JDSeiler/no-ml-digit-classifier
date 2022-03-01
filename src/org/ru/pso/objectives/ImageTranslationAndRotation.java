package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;
import org.ru.vec.Vec3D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ImageTranslationAndRotation extends ImageComparisonBase<Vec3D> {
    public ImageTranslationAndRotation(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        super(referenceImage, candidateImage, useSquaredEuclidean);
    }

    public double compute(Vec3D v) {
        double[] transformationComponents = v.components();
        Vec2D shift = new Vec2D(new double[]{
                transformationComponents[0],
                transformationComponents[1],
        });
        double theta = transformationComponents[2];

        List<AbstractPixel> shiftedPixels = this.translateAllPixels(shift, this.candidateImg);
        Vec2D centerOfRotation = this.findCenterOfMass(shiftedPixels);
        List<AbstractPixel> shiftedAndRotatedCandidate = this.rotateAllPixelsAround(centerOfRotation, theta, shiftedPixels);

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(shiftedAndRotatedCandidate);
        double[][] costs = this.computeCostMatrix(this.refImg, shiftedAndRotatedCandidate);
        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.01);
        return mapping.getTotalCost();
    }

    private List<AbstractPixel> translateAllPixels(Vec2D v, List<AbstractPixel> img) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];

        return img.stream().map(oldPixel -> new AbstractPixel(
                oldPixel.x() + xShift,
                oldPixel.y() + yShift,
                oldPixel.grayscaleValue()
        )).collect(Collectors.toList());
    }

    private List<AbstractPixel> rotateAllPixelsAround(Vec2D centerOfRotation, double theta, List<AbstractPixel> img) {
        /*
         * Notes about rotation:
         *
         * First, how do we rotate around the origin? Wolfram Mathworld to the rescue:
         * R_theta =
         * [ cos(theta), -sin(theta)
         *   sin(theta), cos(theta)]
         * Multiply your vector using the matrix R_theta.
         *
         * Now, what about rotating around a point NOT at the origin?
         * Given a point that you want to rotate around, the "center of rotation"
         * 1. Subtract the center of rotation from each point you want to rotate
         * 2. Rotate as if you were rotating around the origin
         * 3. Add the center of rotation back to each point
         *
         * It's literally, "move it to the origin where we know how to rotate, then
         * put it back"
         *
         * see: https://www.youtube.com/watch?v=nu2MR1RoFsA
         * */

        return img.stream().map(oldPixel ->
                this.translatePixelBy(centerOfRotation.scale(-1), oldPixel)
        ).map(unrotatedPixel ->
                this.rotatePixelBy(theta, unrotatedPixel)
        ).map(rotatedPixel ->
                this.translatePixelBy(centerOfRotation, rotatedPixel)
        ).toList();
    }

    private AbstractPixel translatePixelBy(Vec2D v, AbstractPixel pixel) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];

        return new AbstractPixel(
            pixel.x() + xShift,
            pixel.y() + yShift,
            pixel.grayscaleValue()
        );
    }

    private AbstractPixel rotatePixelBy(double theta, AbstractPixel pixel) {
        double x = pixel.x();
        double y = pixel.y();

        double newX = (x * Math.cos(theta)) + (y * -1 * Math.sin(theta));
        double newY = (x * Math.sin(theta)) + (y * Math.sin(theta));

        return new AbstractPixel(newX, newY, pixel.grayscaleValue());
    }

    private Vec2D findCenterOfMass(List<AbstractPixel> img) {
        // Wikipedia says: https://en.wikipedia.org/wiki/Center_of_mass#A_system_of_particles
        // Scale each vector by its mass, sum all of those vectors
        // Divide the sum of all vectors, by the sum of all masses
        // The result is the center of mass of the set of vectors
        // Here mass is called "ink"
        double sumOfInk = 0;
        double[] accumulator = new double[]{0.0, 0.0};

        // This very well could be premature optimization,
        // but I wanted to make this as fast as possible, so I avoided
        // extra indirection/method calls by avoiding Vec2D. Which
        // otherwise would be a logical choice here.
        for(AbstractPixel p : img) {
            double thisPixelsInk = p.grayscaleValue();
            sumOfInk += thisPixelsInk;

            double scaledX = p.x() * thisPixelsInk;
            double scaledY = p.y() * thisPixelsInk;
            accumulator[0] += scaledX;
            accumulator[1] += scaledY;
        }
        accumulator[0] /= sumOfInk;
        accumulator[1] /= sumOfInk;

        return new Vec2D(accumulator);
    }

}
