package org.ru;

import org.ru.concurrent.ImageClassificationResult;
import org.ru.concurrent.ThreadableImageClassification;
import org.ru.drawing.PointCloud;
import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;
import org.ru.pso.OutputWriter;
import org.ru.pso.objectives.ImageTRS;
import org.ru.pso.PSO;
import org.ru.pso.PSOConfig;
import org.ru.pso.Solution;
import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.Vec5D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Starting PSO ===\n");
        long start = System.nanoTime();

        try {
            testPair();
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

    public static void runFullTests() throws IOException, ExecutionException, InterruptedException {
        // This is the image reader for the heatmaps.
        ImgReader referenceImages = new ImgReader();
        ArrayList<BufferedImage> heatmaps = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            heatmaps.add(referenceImages.getImage(String.format("heatmap-%d.bmp", i)));
        }

        for (int candidateLabel = 0; candidateLabel< 10; candidateLabel++) {
            File locationOfImages = new File(String.format("img/mnist/%d", candidateLabel));
            ImgReader candidateReader = new ImgReader(String.format("img/mnist/%d", candidateLabel));
            List<String> testImages = Arrays.stream(Objects.requireNonNull(locationOfImages.listFiles())).map(File::getName).toList();

            ExecutorService pool = Executors.newFixedThreadPool(10);

            for (String candidate : testImages) {
                BufferedImage candidateImage = candidateReader.getImage(candidate);
                // TODO - Each comparison needs to return enough data so that I can record all the data I want *after*
                // every comparison is done.
                ArrayList<ImageClassificationResult<Vec5D>> results = classifyThisCandidate(candidateLabel, candidateImage, heatmaps, pool);

                int classifiedAs = -1;
                double bestFitnessScore = Double.MAX_VALUE;

                for (ImageClassificationResult<Vec5D> res : results) {
                    // Here is where we can output the individual comparisons to a file. EZ PZ
                    if (res.result().fitnessScore() < bestFitnessScore) {
                        bestFitnessScore = res.result().fitnessScore();
                        classifiedAs = res.referenceDigit();
                    }
                }
                // At this point we can output the classification
            }
        }
    }

    public static void testPair() throws IOException {
        OutputWriter out = new OutputWriter("temp/results.txt");
        /*
        * Candidate 2:
        * - Reference 1
        * - Reference 2
        *
        * Candidate 5:
        * - Reference 6
        * - Reference 5
        * After adding scaling, r5-c5 was 0.77145
        * r6-c5 was 0.95134
        *
        * Candidate 8:
        * - Reference 3
        * - Reference 8
        * */
        ImgReader reader = new ImgReader("img/moderate-tests");
        BufferedImage ref = reader.getImage("reference-8.bmp");
        BufferedImage cand = reader.getImage("candidate-8.bmp");

        // Remember that if you scale by a negative number you can flip images through axis.
        // Just like a 90 degree rotation... Neat!
        ImageTRS objectiveFunction = new ImageTRS(ref, cand, false);

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
        System.out.printf("ref8 to cand8 was: %.05f%n", foundMinimum.fitnessScore());
        System.out.println(foundMinimum.solution());

        out.write("ref8 to cand8:");
        out.write(foundMinimum.toString());

        List<AbstractPixel> finalCandidatePoints = objectiveFunction.getLastSetOfCandidatePoints();
        List<AbstractPixel> referencePoints = objectiveFunction.getLastSetOfReferencePoints();

        PointCloud.drawDigitComparison(referencePoints, "ref8.txt", finalCandidatePoints, "cand8.txt");

        out.close();
    }

    public static ArrayList<ImageClassificationResult<Vec5D>> classifyThisCandidate(
            int candidateLabel,
            BufferedImage candidateImage,
            ArrayList<BufferedImage> references,
            ExecutorService pool
    ) throws InterruptedException, ExecutionException {
        /*
        * It might feel kind of strange to put the candidate in the outer loop and then the reference.
        * Think of it this way:
        * 1. We have some candidate, we don't know what it is
        * 2. We're going to compare that candidate to all available reference images.
        *
        * Thus, we are trying to identify each candidate in turn. Then to identify each candidate we
        * select each reference in turn.
        *
        * Just remember this convention: The Candidate is ALWAYS ALWAYS ALWAYS the image that moves.
        * The reference image is ALWAYS stationary.
        * */
        ArrayList<ImageClassificationResult<Vec5D>> answers = new ArrayList<>();
        ArrayList<ThreadableImageClassification> comparisons = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            BufferedImage ref = references.get(i);

            ThreadableImageClassification comparison = new ThreadableImageClassification(i, ref, candidateLabel, candidateImage);
            comparisons.add(comparison);
        }

        List<Future<ImageClassificationResult<Vec5D>>> futureResults = pool.invokeAll(comparisons);
        for (Future<ImageClassificationResult<Vec5D>> ans : futureResults) {
            answers.add(ans.get());
        }
        return answers;
    }
}
