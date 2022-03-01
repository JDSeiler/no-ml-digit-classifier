package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ImageTranslation extends ImageComparisonBase {
    public ImageTranslation(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        super(referenceImage, candidateImage, useSquaredEuclidean);
    }

    public double compute(Vec2D v) {
        List<AbstractPixel> adjustedCandidate = this.applyTransformation(v, this.candidateImg);

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(adjustedCandidate);
        double[][] costs = super.computeCostMatrix(this.refImg, adjustedCandidate);
        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.01);
        return mapping.getTotalCost();
    }

    private List<AbstractPixel> applyTransformation(Vec2D v, List<AbstractPixel> img) {
        double xShift = v.components()[0];
        double yShift = v.components()[1];
        return img.stream().map(oldPixel -> new AbstractPixel(
                oldPixel.x() + xShift,
                oldPixel.y() + yShift,
                oldPixel.grayscaleValue()
        )).collect(Collectors.toList());
    }

    private double[] getGrayscaleArray(List<AbstractPixel> img) {
        double[] contents = new double[img.size()];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = img.get(i).grayscaleValue();
        }
        return contents;
    }
}
