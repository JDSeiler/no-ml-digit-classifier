package org.ru;

import org.ru.img.ImgReader;
import org.ru.pso.objectives.ImageTranslationAndRotation;
import org.ru.pso.PSO;
import org.ru.pso.PSOConfig;
import org.ru.pso.Solution;
import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.Vec3D;

import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Starting PSO ===\n");
        long start = System.nanoTime();

        testImageComparison();

        System.out.println("\n=== PSO is complete ===\n");
        long end = System.nanoTime();
        double timeInMs = (end - start) / 1_000_000.0;
        double timeInSec = timeInMs / 1_000.0;
        System.out.printf("PSO took %.2f ms (%.5f s)", timeInMs, timeInSec);
    }

    public static void testImageComparison() {
        ImgReader reader = new ImgReader("img/initial-tests");
        BufferedImage ref = reader.getImage("unbalanced/reference.bmp");
        BufferedImage candidate = reader.getImage("unbalanced/candidate.bmp");

        ImageTranslationAndRotation objectiveFunction = new ImageTranslationAndRotation(ref, candidate, false);

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
        System.out.println("Particle Locations:");
        pso.printSwarm();
        System.out.println("Best solution at end:");
        double[] solution = foundMinimum.solution().components();
        System.out.printf("Coordinates: %f, %f%n", solution[0], solution[1]);
        System.out.printf("Rotation in radians: %f%n", solution[2]);
        System.out.printf("Rotation in degrees mod 360: %f%n", (solution[2] * 180 / Math.PI) % 360);
    }

}
