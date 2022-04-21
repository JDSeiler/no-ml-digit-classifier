package org.ru.pso;

import org.ru.img.AbstractPixel;
import org.ru.vec.Vec2D;
import org.ru.vec.Vec5D;

import java.util.List;
import java.util.stream.Collectors;

public class RandomTransformer {
    /**
     * Random Translation, Rotation, and Scaling
     * @param bounds 5D vector with strictly positive components representing the bounds of each transformation.
     *               [xShift, yShift, theta, xScale, yScale]
     *               For each bound component `b`, a transformation value is generated in the range [-b, b]
     * @param untransformedImage the unmodified image
     * @return The image after applying random translation, rotation, and scaling
     */
    public static List<AbstractPixel> randomTRS(Vec5D bounds, List<AbstractPixel> untransformedImage) {
        double[] b = bounds.components();
        double xShift = generateInRange(-b[0], b[0]);
        double yShift = generateInRange(-b[1], b[1]);
        double theta  = generateInRange(-b[2], b[2]);
        double xScale = generateInRange(-b[3], b[3]);
        double yScale = generateInRange(-b[4], b[4]);

        Vec2D shift = new Vec2D(new double[] {
                xShift,
                yShift
        });

        List<AbstractPixel> shiftedPixels = translateAllPixels(shift, untransformedImage);

        Vec2D centerOfMass = findCenterOfMass(shiftedPixels);
//        List<AbstractPixel> shiftedAndScaledCandidate = scaleAllPixels(xScale, yScale, centerOfMass, shiftedPixels);

        Vec2D centerOfRotation = findCenterOfMass(shiftedPixels);
        return rotateAllPixelsAround(centerOfRotation, theta, shiftedPixels);
    }

    /*
    Basically all the code below this line is ripped out of some other file.
    If I wanted to clean this up I would factor out all the transformations into their own module.
    They can all exist as static functions anyway.
    */

    private static double generateInRange(double min, double max) {
        // This method is what the Math.random() docs suggest for generating a value
        // between two values, inclusively.
        double interpolationFactor = Math.random()/Math.nextDown(1.0);
        // This formula is the linear interpolation (convex combination) between min and max.
        return min*(1.0-interpolationFactor) + max*interpolationFactor;
    }

    private static List<AbstractPixel> scaleAllPixels(double xScaleFactor, double yScaleFactor, Vec2D centerOfMass, List<AbstractPixel> img) {
        return img.stream().map(oldPixel ->
                translatePixelBy(centerOfMass.scale(-1), oldPixel)
        ).map(nonScaledPixel ->
                scalePixel(xScaleFactor, yScaleFactor, nonScaledPixel)
        ).map(scaledPixel ->
                translatePixelBy(centerOfMass, scaledPixel)
        ).toList();
    }

    private static AbstractPixel scalePixel(double xScaleFactor, double yScaleFactor, AbstractPixel p) {
        /* Multiplying the x or y values of a dud pixel can result in NaN through the following process:
        1. MAX_VALUE * basically_anything = Infinity
        2. Infinity * 0.0 = NaN

        NaN causes all kinds of issues.
        So, if a pixel is dud, just return it directly. Don't transform it.
        TODO: Probably should avoid ALL computation with dud pixels, just to be safe?
        */
        if (p.isDud()) {
            return p;
        }

        return new AbstractPixel(
                p.x() * Math.abs(xScaleFactor),
                p.y() * Math.abs(yScaleFactor),
                p.grayscaleValue(),
                p.isDud()
        );
    }

    private static List<AbstractPixel> translateAllPixels(Vec2D v, List<AbstractPixel> img) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];

        return img.stream().map(oldPixel -> new AbstractPixel(
                oldPixel.x() + xShift,
                oldPixel.y() + yShift,
                oldPixel.grayscaleValue(),
                oldPixel.isDud()
        )).collect(Collectors.toList());
    }

    private static List<AbstractPixel> rotateAllPixelsAround(Vec2D centerOfRotation, double theta, List<AbstractPixel> img) {
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
                translatePixelBy(centerOfRotation.scale(-1), oldPixel)
        ).map(unrotatedPixel ->
                rotatePixelBy(theta, unrotatedPixel)
        ).map(rotatedPixel ->
                translatePixelBy(centerOfRotation, rotatedPixel)
        ).toList();
    }

    private static AbstractPixel translatePixelBy(Vec2D v, AbstractPixel pixel) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];

        return new AbstractPixel(
                pixel.x() + xShift,
                pixel.y() + yShift,
                pixel.grayscaleValue(),
                pixel.isDud()
        );
    }

    /**
     * Rotates a pixel counterclockwise by theta radians
     * @param theta angle of rotation in radians
     * @param pixel the pixel/point to rotate
     * @return a new point which is the original, rotated counterclockwise by theta radians
     */
    private static AbstractPixel rotatePixelBy(double theta, AbstractPixel pixel) {
        double x = pixel.x();
        double y = pixel.y();

        double newX = (x * Math.cos(theta)) - (y * Math.sin(theta));
        double newY = (x * Math.sin(theta)) + (y * Math.cos(theta));

        return new AbstractPixel(newX, newY, pixel.grayscaleValue(), pixel.isDud());
    }

    private static Vec2D findCenterOfMass(List<AbstractPixel> img) {
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
