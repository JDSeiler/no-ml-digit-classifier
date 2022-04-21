package org.ru.concurrent;

import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;
import org.ru.pso.PSO;
import org.ru.pso.PSOConfig;
import org.ru.pso.Solution;
import org.ru.pso.objectives.ImageTRS;
import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.Vec5D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Callable;

public class ThreadableImageClassification implements Callable<ImageClassificationResult<Vec5D>> {
    private final BufferedImage reference;
    private final List<AbstractPixel> candidate;
    private final int referenceId;
    private final int candidateId;

    private static final double GRAYSCALE_THRESHOLD = 0.6;

    public ThreadableImageClassification(int referenceId, BufferedImage referenceImage, int candidateId, List<AbstractPixel> candidateImage) {
        this.reference = referenceImage;
        this.candidate = candidateImage;

        this.referenceId = referenceId;
        this.candidateId = candidateId;
    }

    public ThreadableImageClassification(int referenceId, BufferedImage referenceImage, int candidateId, BufferedImage candidateImage) {
        this.reference = referenceImage;
        this.candidate = ImgReader.convertToAbstractPixels(candidateImage, GRAYSCALE_THRESHOLD);

        this.referenceId = referenceId;
        this.candidateId = candidateId;
    }

    @Override
    public ImageClassificationResult<Vec5D> call() throws Exception {
        ImageTRS objectiveFunction = new ImageTRS(this.reference, this.candidate, GRAYSCALE_THRESHOLD, false);

        PSOConfig<Vec5D> config = new PSOConfig<>(
                15,
                0.75,
                1.3,
                1.5,
                Topology.COMPLETE,
                Placement.RANDOM,
                5.0,
                new Vec5D(new double[]{10.0, 10.0, 3.0, 1.0, 1.0})
        );

        PSO<Vec5D> pso = new PSO<>(config, objectiveFunction::compute, Vec5D::new);
        Solution<Vec5D> foundMinimum = pso.run();
        return new ImageClassificationResult<>(this.referenceId, this.candidateId, foundMinimum, pso.getLastSolutionIterations());
    }
}
