package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;
import org.ru.vec.Vec3D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ImageTranslationAndRotation extends ImageComparisonBase<Vec3D> {
    private List<AbstractPixel> lastSetOfCandidatePoints = null;
    private List<AbstractPixel> lastSetOfReferencePoints = null;


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

        // These are exposed PURELY for drawing code -- we need the coordinates of the points to draw diagrams
        // of the results.
        this.lastSetOfCandidatePoints = shiftedAndRotatedCandidate;
        this.lastSetOfReferencePoints = this.refImg;

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(shiftedAndRotatedCandidate);
        /*
         * TODO: Improve 'knowability' of delta's performance
         * What we want: To know how "good" a given delta error is.
         * What we need: A known max edge cost.
         *
         * What to do:
         * 1. Normalize all edge costs so that the largest edge cost is 1 (divide all edges by the existing max edge cost)
         * 2. Run OT
         * 3. Take the fitness result, and multiply it by the max edge cost we used to do the scaling.
         * */
        double[][] costs = this.computeCostMatrix(this.refImg, shiftedAndRotatedCandidate);


        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.01);
        // Penalize rotation
        // TODO: This rotation penalty needs to be tuned. It doesn't perform well right now.
        double rotationPenalty = Math.abs(theta);

        return mapping.getTotalCost() + (rotationPenalty*rotationPenalty);
    }



    public List<AbstractPixel> getLastSetOfCandidatePoints() {
        return lastSetOfCandidatePoints;
    }

    public List<AbstractPixel> getLastSetOfReferencePoints() {
        return lastSetOfReferencePoints;
    }
}
