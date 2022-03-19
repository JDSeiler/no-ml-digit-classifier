package org.ru;

import org.ru.concurrent.ImageClassificationResult;
import org.ru.concurrent.ThreadableImageClassification;
import org.ru.img.ImgReader;
import org.ru.pso.objectives.ImageTranslationAndRotation;
import org.ru.pso.PSO;
import org.ru.pso.PSOConfig;
import org.ru.pso.Solution;
import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.Vec3D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Starting PSO ===\n");
        long start = System.nanoTime();

        try {
            testDigitRecognition();
        } catch(Exception e) {
            System.out.println("Something went wrong!");
            System.err.println(e);
        }

        System.out.println("\n=== PSO is complete ===\n");
        long end = System.nanoTime();
        double timeInMs = (end - start) / 1_000_000.0;
        double timeInSec = timeInMs / 1_000.0;
        System.out.printf("PSO took %.2f ms (%.5f s)", timeInMs, timeInSec);
    }

    public static void testDigitRecognition() throws InterruptedException, ExecutionException {
        ImgReader reader = new ImgReader("img/moderate-tests");

        // TODO: It doesn't really matter, but I've got the terms "reference" and "candidate" swapped from what they
        // should be in this code. I need to shore all that up so I understand exactly what images are being used for what.

        // TODO: More thorough inspection of EVERYTHING. Something is no right with these fitness scores

        ArrayList<BufferedImage> references = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            references.add(reader.getImage(String.format("reference-%d.bmp", i)));
        }

        ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            System.out.printf("Candidate: %d%n", i);
            BufferedImage cand = reader.getImage(String.format("candidate-%d.bmp", i));

            ArrayList<ThreadableImageClassification> comparisons = new ArrayList<>();
            ArrayList<ImageClassificationResult<Vec3D>> answers = new ArrayList<>();

            for (int j = 0; j < 10; j++) {
                BufferedImage ref = references.get(j);

                ThreadableImageClassification comparison = new ThreadableImageClassification(i, cand, j, ref);
                comparisons.add(comparison);
            }

            List<Future<ImageClassificationResult<Vec3D>>> futureResults = pool.invokeAll(comparisons);
            for (Future<ImageClassificationResult<Vec3D>> ans : futureResults) {
                answers.add(ans.get());
            }

            for (ImageClassificationResult<Vec3D> ans : answers) {
                System.out.printf(
                        "Similarity between %d and %d : %.5f%n",
                        ans.candidateDigit(),
                        ans.referenceDigit(),
                        ans.result().fitnessScore());
            }
        }

        pool.shutdown();
    }
}
