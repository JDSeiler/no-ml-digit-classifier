package org.ru.pso.objectives;

import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;
import org.ru.vec.FixedVector;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

abstract public class ImageComparisonBase<V extends FixedVector> {
    protected final List<AbstractPixel> refImg;
    protected final List<AbstractPixel> candidateImg;
    protected boolean useSquaredEuclidean = false;

    public ImageComparisonBase(BufferedImage referenceImage, BufferedImage candidateImage, boolean useSquaredEuclidean) {
        this.refImg = this.normalizeAbstractPixels(
                ImgReader.convertToAbstractPixels(referenceImage, 1.0)
        );
        this.candidateImg = this.normalizeAbstractPixels(
                ImgReader.convertToAbstractPixels(candidateImage, 1.0)
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
                if (candidatePixel.isDud()) {
                    costMatrix[i][j] = 0;
                    costMatrix[j][i] = 0;
                } else {
                    costMatrix[i][j] = cost;
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
        return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
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
}
