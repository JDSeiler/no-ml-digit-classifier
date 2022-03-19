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
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Starting PSO ===\n");
        long start = System.nanoTime();

        testDigitRecognition();

        System.out.println("\n=== PSO is complete ===\n");
        long end = System.nanoTime();
        double timeInMs = (end - start) / 1_000_000.0;
        double timeInSec = timeInMs / 1_000.0;
        System.out.printf("PSO took %.2f ms (%.5f s)", timeInMs, timeInSec);
    }

    public static void manualObjectiveFunctionTesting() {
        ImgReader reader = new ImgReader("img/moderate-tests");
        BufferedImage cand = reader.getImage("candidate-3.bmp");
        BufferedImage ref = reader.getImage("reference-3.bmp");

        ImageTranslationAndRotation objectiveFunction = new ImageTranslationAndRotation(ref, cand, false);
        Vec3D testTransformation = new Vec3D(new double[]{1.0, 2.00, 0.0});
        double ans = objectiveFunction.compute(testTransformation);

        System.out.printf("%f%n", ans);
    }

    public static void testDigitRecognition() {
        ImgReader reader = new ImgReader("img/moderate-tests");
        // The number `j` at index `i` => PSO categorized the digit `i` as `j`
        int[] psoAnswers = new int[10];
        Arrays.fill(psoAnswers, -1);

        for (int i = 3; i < 4; i++) {
            System.out.printf("Candidate: %d%n", i);
            BufferedImage cand = reader.getImage(String.format("candidate-%d.bmp", i));
            // lower is better
            double bestFitnessForThisDigit = Double.MAX_VALUE;
            for (int j = 0; j < 10; j++) {
                System.out.printf("%n%nChecking against reference digit: %d%n", j);
                BufferedImage ref = reader.getImage(String.format("reference-%d.bmp", j));

                ImageTranslationAndRotation objectiveFunction = new ImageTranslationAndRotation(ref, cand, false);

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

                double[] solution = foundMinimum.solution().components();
                System.out.printf("Fitness against reference digit %d is: %.6f%n", j, foundMinimum.fitnessScore());
                System.out.printf("Coordinates: %f, %f%n", solution[0], solution[1]);
                System.out.printf("Rotation in radians: %f%n", solution[2]);
                System.out.printf("Rotation in degrees mod 360: %f%n", (solution[2] * 180 / Math.PI) % 360);

                if (foundMinimum.fitnessScore() < bestFitnessForThisDigit) {
                    bestFitnessForThisDigit = foundMinimum.fitnessScore();
                    psoAnswers[i] = j;
                }
            }
        }

        System.out.println("\n\nPSO Answers: ");
        for (int i = 0; i < psoAnswers.length; i++) {
            System.out.printf("Candidate %d matched to: %d%n", i, psoAnswers[i]);
        }
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
