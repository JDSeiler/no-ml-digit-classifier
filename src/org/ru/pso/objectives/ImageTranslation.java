package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ImageTranslation extends ImageComparisonBase<Vec2D> {
    public ImageTranslation(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        super(referenceImage, candidateImage, useSquaredEuclidean);
    }

    public double compute(Vec2D v) {
        List<AbstractPixel> adjustedCandidate = this.translateBy(v, this.candidateImg);

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(adjustedCandidate);
        double[][] costs = this.computeCostMatrix(this.refImg, adjustedCandidate);
        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.01);
        return mapping.getTotalCost();
    }

    private List<AbstractPixel> translateBy(Vec2D v, List<AbstractPixel> img) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];
        return img.stream().map(oldPixel -> new AbstractPixel(
                oldPixel.x() + xShift,
                oldPixel.y() + yShift,
                oldPixel.grayscaleValue(),
                oldPixel.isDud()
        )).collect(Collectors.toList());
    }
}
