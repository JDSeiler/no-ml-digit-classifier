package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;

import java.awt.image.BufferedImage;
import java.util.List;

public class ImageTranslation extends ImageComparisonBase<Vec2D> {
    public ImageTranslation(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        super(referenceImage, candidateImage, useSquaredEuclidean);
    }

    public double compute(Vec2D v) {
        List<AbstractPixel> adjustedCandidate = this.translateAllPixels(v, this.candidateImg);

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(adjustedCandidate);
        double[][] costs = this.computeCostMatrix(this.refImg, adjustedCandidate);
        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.01);
        return mapping.getTotalCost();
    }
}
