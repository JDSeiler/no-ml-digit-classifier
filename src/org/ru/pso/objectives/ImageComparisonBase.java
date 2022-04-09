package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;
import org.ru.vec.FixedVector;
import org.ru.vec.Vec2D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

abstract public class ImageComparisonBase<V extends FixedVector> {
    protected final List<AbstractPixel> refImg;
    protected final List<AbstractPixel> candidateImg;
    protected boolean useSquaredEuclidean = false;

    public ImageComparisonBase(BufferedImage referenceImage, BufferedImage candidateImage, double threshold, boolean useSquaredEuclidean) {
        this.refImg = this.normalizeAbstractPixels(
                ImgReader.convertToAbstractPixels(referenceImage, threshold)
        );
        this.candidateImg = this.normalizeAbstractPixels(
                ImgReader.convertToAbstractPixels(candidateImage, threshold)
        );

        this.useSquaredEuclidean = useSquaredEuclidean;

        int largestImage = Integer.max(this.refImg.size(), this.candidateImg.size());
        this.balanceNumberOfPixels(this.refImg, largestImage);
        this.balanceNumberOfPixels(this.candidateImg, largestImage);

        if (this.refImg.size() != this.candidateImg.size()) {
            System.err.printf("Ref Img: %d, Candidate Img: %d", this.refImg.size(), this.candidateImg.size());
            throw new RuntimeException("Reference image and candidate image must have the same number of abstract pixels in them!");
        }
    }

    public ImageComparisonBase(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        this(referenceImage, candidateImage, 1.0, useSquaredEuclidean);
    }


    abstract double compute(V v);

    protected double[][] computeCostMatrix(List<AbstractPixel> refImg, List<AbstractPixel> candidateImg) {
        double[][] costMatrix = new double[refImg.size()][refImg.size()];

        for (int i = 0; i < refImg.size(); i++) {
            AbstractPixel refPixel = refImg.get(i);
            for (int j = 0; j < candidateImg.size(); j++) {
                AbstractPixel candidatePixel = candidateImg.get(j);
                double cost;
                if (this.useSquaredEuclidean) {
                    cost = this.squaredEuclideanDistance(
                            refPixel.x(),
                            refPixel.y(),
                            candidatePixel.x(),
                            candidatePixel.y()
                    );
                } else {
                    cost = this.euclideanDistance(
                            refPixel.x(),
                            refPixel.y(),
                            candidatePixel.x(),
                            candidatePixel.y()
                    );
                }
                // Ref image is ALWAYS the supply
                // Cand image is ALWAYS the demand
                // Mapping.java expects C (the costs) to be defined, such that
                // C[p][q] == cost from demand vertex P to supply vertex Q
                // i is a ref pixel
                // j is a cand pixel
                // Therefore, C[j][i] == jth demand to ith supply, exactly what we want
                if (candidatePixel.isDud() || refPixel.isDud()) {
                    // costMatrix[i][j] = 0;
                    costMatrix[j][i] = 0;
                } else {
                    // costMatrix[i][j] = cost;
                    costMatrix[j][i] = cost;
                }
            }
        }

        return costMatrix;
    }

    /**
     * Convert a List of AbstractPixels to an array of greyscale values.
     *
     * @param img the image to convert
     * @return the greyscale values of `img` in an array
     */
    protected double[] getGrayscaleArray(List<AbstractPixel> img) {
        double[] contents = new double[img.size()];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = img.get(i).grayscaleValue();
        }
        return contents;
    }

    private double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(
                Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)
        );
    }

    private double squaredEuclideanDistance(double x1, double y1, double x2, double y2) {
        // Something about this causes the program to hang badly, no clue why...
        // return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);

        // Sometimes, the program will still get stuck even doing this.
        return ((x2-x1) * (x2-x1)) + ((y2-y1) * (y2-y1));
    }

    private List<AbstractPixel> normalizeAbstractPixels(List<AbstractPixel> img) {
        double totalInk = img.stream().map(AbstractPixel::grayscaleValue).reduce(0.0, Double::sum);
        return img.stream().map(oldPixel -> {
            double newGreyscaleValue = oldPixel.grayscaleValue() / totalInk;
            return new AbstractPixel(oldPixel.x(), oldPixel.y(), newGreyscaleValue);
        }).collect(Collectors.toList());
    }

    /**
     * Adds dud pixels to the provided image until it contains `largestImage` number
     * of AbstractPixels.
     *
     * If the size of the provided image is greater than or equal to `largestImage`, this
     * method does nothing.
     * @param img the image to balance
     * @param largestImage the number of AbstractPixels in the largest image
     */
    private void balanceNumberOfPixels(List<AbstractPixel> img, int largestImage) {
        if (img.size() < largestImage) {
            int numPixelsToAdd = largestImage - img.size();
            for (int i = 0; i < numPixelsToAdd; i++) {
                // The grayscale value is the supply or demand of the pixel from the perspective
                // of OT. So pixels with 0 weight are effectively ignored. This lets us run OT
                // on images where the number of dark pixels is not equal. We also set the 'isDud'
                // field to true, which means their cost will also be 0 when computing the cost matrix.

                img.add(new AbstractPixel(Double.MAX_VALUE, Double.MAX_VALUE, 0, true));
            }
        }
        // If this IS the largest image, do nothing.
        // If the image is larger than the "largestImage", then "largestImage" is wrong.
    }

    protected List<AbstractPixel> translateAllPixels(Vec2D v, List<AbstractPixel> img) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];

        return img.stream().map(oldPixel -> new AbstractPixel(
                oldPixel.x() + xShift,
                oldPixel.y() + yShift,
                oldPixel.grayscaleValue(),
                oldPixel.isDud()
        )).collect(Collectors.toList());
    }

    protected List<AbstractPixel> rotateAllPixelsAround(Vec2D centerOfRotation, double theta, List<AbstractPixel> img) {
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

    protected AbstractPixel translatePixelBy(Vec2D v, AbstractPixel pixel) {
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
    protected AbstractPixel rotatePixelBy(double theta, AbstractPixel pixel) {
        double x = pixel.x();
        double y = pixel.y();

        double newX = (x * Math.cos(theta)) - (y * Math.sin(theta));
        double newY = (x * Math.sin(theta)) + (y * Math.cos(theta));

        return new AbstractPixel(newX, newY, pixel.grayscaleValue(), pixel.isDud());
    }

    protected Vec2D findCenterOfMass(List<AbstractPixel> img) {
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
