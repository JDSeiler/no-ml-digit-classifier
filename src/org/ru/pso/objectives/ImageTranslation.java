package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;
import org.ru.ot.Mapping;
import org.ru.vec.Vec2D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ImageTranslation {
    private final List<AbstractPixel> refImg;
    private final List<AbstractPixel> candidateImg;

    public ImageTranslation(BufferedImage referenceImage, BufferedImage candidateImage) {
        this.refImg = this.normalizeAbstractPixels(
                ImgReader.convertToAbstractPixels(referenceImage, 1.0)
        );
        this.candidateImg = this.normalizeAbstractPixels(
                ImgReader.convertToAbstractPixels(candidateImage, 1.0)
        );

        if (this.refImg.size() != this.candidateImg.size()) {
            System.err.printf("Ref Img: %d, Candidate Img: %d", this.refImg.size(), this.candidateImg.size());
            throw new RuntimeException("Reference image and candidate image must have the same number of abstract pixels in them!");
        }
    }

    public double compute(Vec2D v) {
        List<AbstractPixel> adjustedCandidate = this.applyTransformation(v, this.candidateImg);

        int n = this.refImg.size();
        double[] supplies = this.getGrayscaleArray(this.refImg);
        double[] demands = this.getGrayscaleArray(adjustedCandidate);
        double[][] costs = this.computeCostMatrix(this.refImg, adjustedCandidate);
        Mapping mapping = new Mapping(n, supplies, demands, costs, 0.01);
        return mapping.getTotalCost();
    }

    private double[] getGrayscaleArray(List<AbstractPixel> img) {
        double[] contents = new double[img.size()];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = img.get(i).grayscaleValue();
        }
        return contents;
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

    private double[][] computeCostMatrix(List<AbstractPixel> refImg, List<AbstractPixel> candidateImg) {
        double[][] costMatrix = new double[refImg.size()][refImg.size()];

        for (int i = 0; i < refImg.size(); i++) {
            AbstractPixel refPixel = refImg.get(i);
            for (int j = 0; j < candidateImg.size(); j++) {
                AbstractPixel candidatePixel = candidateImg.get(j);
                double cost = this.euclideanDistance(
                        refPixel.x(),
                        refPixel.y(),
                        candidatePixel.x(),
                        candidatePixel.y()
                );
                costMatrix[i][j] = cost;
                costMatrix[j][i] = cost;
            }
        }

        return costMatrix;
    }

    private double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(
                Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)
        );
    }

    private List<AbstractPixel> normalizeAbstractPixels(List<AbstractPixel> img) {
        double totalInk = img.stream().map(AbstractPixel::grayscaleValue).reduce(0.0, Double::sum);
        return img.stream().map(oldPixel -> {
            double newGreyscaleValue = oldPixel.grayscaleValue() / totalInk;
            return new AbstractPixel(oldPixel.x(), oldPixel.y(), newGreyscaleValue);
        }).collect(Collectors.toList());
    }

    public List<AbstractPixel> getRefImg() {
        return refImg;
    }

    public List<AbstractPixel> getCandidateImg() {
        return candidateImg;
    }
}
