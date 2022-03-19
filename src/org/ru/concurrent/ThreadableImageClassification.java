package org.ru.concurrent;

import org.ru.pso.PSO;
import org.ru.pso.PSOConfig;
import org.ru.pso.Solution;
import org.ru.pso.objectives.ImageTranslationAndRotation;
import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.Vec3D;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class ThreadableImageClassification implements Callable<ImageClassificationResult<Vec3D>> {
    private final BufferedImage reference;
    private final BufferedImage candidate;
    private final int referenceId;
    private final int candidateId;

    public ThreadableImageClassification(int referenceId, BufferedImage referenceImage, int candidateId, BufferedImage candidateImage) {
        this.reference = referenceImage;
        this.candidate = candidateImage;

        this.referenceId = referenceId;
        this.candidateId = candidateId;
    }

    @Override
    public ImageClassificationResult<Vec3D> call() throws Exception {
        ImageTranslationAndRotation objectiveFunction = new ImageTranslationAndRotation(this.reference, this.candidate, false);

        PSOConfig<Vec3D> config = new PSOConfig<>(
                15,
                0.75,
                1.3,
                1.5,
                Topology.COMPLETE,
                Placement.RANDOM,
                5.0,
                new Vec3D(new double[]{10.0, 10.0, 3.0})
        );

        PSO<Vec3D> pso = new PSO<>(config, objectiveFunction::compute, Vec3D::new);
        Solution<Vec3D> foundMinimum = pso.run();
        return new ImageClassificationResult<>(this.referenceId, this.candidateId, foundMinimum);
    }
}
