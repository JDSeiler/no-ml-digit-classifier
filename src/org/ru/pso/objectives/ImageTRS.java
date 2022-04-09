package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;
import org.ru.vec.Vec5D;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Image Translation, Scaling, and Rotation
 */
public class ImageTRS extends ImageComparisonBase<Vec5D> {
    private List<AbstractPixel> lastSetOfCandidatePoints = null;
    private List<AbstractPixel> lastSetOfReferencePoints = null;


    public ImageTRS(BufferedImage referenceImage, BufferedImage candidateImage, double threshold, boolean useSquaredEuclidean) {
        super(referenceImage, candidateImage, threshold, useSquaredEuclidean);
    }

    public ImageTRS(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        super(referenceImage, candidateImage, 1.0, useSquaredEuclidean);
    }

    public double compute(Vec5D v) {
        double [] transformationComponents = v.components();
        Vec2D shift = new Vec2D(new double[]{
                transformationComponents[0],
                transformationComponents[1],
        });
        double theta = transformationComponents[2];
        double xScaleFactor = transformationComponents[3];
        double yScaleFactor = transformationComponents[4];

        List<AbstractPixel> shiftedPixels = this.translateAllPixels(shift, this.candidateImg);

        Vec2D centerOfMass = this.findCenterOfMass(shiftedPixels);
        List<AbstractPixel> shiftedAndScaledCandidate = this.scaleAllPixels(xScaleFactor, yScaleFactor, centerOfMass, shiftedPixels);

        Vec2D centerOfRotation = this.findCenterOfMass(shiftedAndScaledCandidate);
        List<AbstractPixel> fullyTransformedPixels = this.rotateAllPixelsAround(centerOfRotation, theta, shiftedAndScaledCandidate);

        // These are exposed PURELY for drawing code -- we need the coordinates of the points to draw diagrams
        // of the results.
        this.lastSetOfCandidatePoints = fullyTransformedPixels;
        this.lastSetOfReferencePoints = this.refImg;

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(fullyTransformedPixels);

        double[][] costs = this.computeCostMatrix(this.refImg, fullyTransformedPixels);

        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.10);

        // Keep penalizing rotation, maybe penalize scaling?
        double rotationPenalty = Math.abs(theta);
        double xScalingPenalty = Math.abs(1.0 - Math.abs(xScaleFactor));
        double yScalingPenalty = Math.abs(1.0 - Math.abs(yScaleFactor));

        return mapping.getTotalCost() + (rotationPenalty*rotationPenalty) + xScalingPenalty + yScalingPenalty;
    }

    private List<AbstractPixel> scaleAllPixels(double xScaleFactor, double yScaleFactor, Vec2D centerOfMass, List<AbstractPixel> img) {
        return img.stream().map(oldPixel ->
            this.translatePixelBy(centerOfMass.scale(-1), oldPixel)
        ).map(nonScaledPixel ->
            this.scalePixel(xScaleFactor, yScaleFactor, nonScaledPixel)
        ).map(scaledPixel ->
            this.translatePixelBy(centerOfMass, scaledPixel)
        ).toList();
    }

    private AbstractPixel scalePixel(double xScaleFactor, double yScaleFactor, AbstractPixel p) {
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

    public List<AbstractPixel> getLastSetOfCandidatePoints() {
        return lastSetOfCandidatePoints;
    }

    public List<AbstractPixel> getLastSetOfReferencePoints() {
        return lastSetOfReferencePoints;
    }
}
