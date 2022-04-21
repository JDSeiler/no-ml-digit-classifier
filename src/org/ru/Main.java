package org.ru;

import org.ru.concurrent.ImageClassificationResult;
import org.ru.concurrent.ThreadableImageClassification;
import org.ru.drawing.PointCloud;
import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;
import org.ru.pso.*;
import org.ru.pso.objectives.ImageTRS;
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
        System.out.println("=== Starting ITEC 498 Classifier : 2x2 kernel with random shifts ===\n");
        long start = System.nanoTime();

        try {
            runFullTests();
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
        // 0: Set up universal resources that we want to be able to clean up.
        OutputWriter comparisons = new OutputWriter("temp/comparisons.csv");
        OutputWriter classifications = new OutputWriter("temp/classifications.csv");
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try {
            comparisons.writeLine("ref_label,cand_label,cand_img_id,fitness,x_shift,y_shift,rotation,x_scale,y_scale,iterations");
            classifications.writeLine("cand_label,cand_img_id,classified_as,0-fitness,1-fitness,2-fitness,3-fitness,4-fitness,5-fitness,6-fitness,7-fitness,8-fitness,9-fitness,i_xShift,i_yShift,i_theta,i_xScale,i_yScale");

            // 1: Load the heatmap reference images
            ImgReader referenceImages = new ImgReader("img/heatmaps");
            ArrayList<BufferedImage> heatmaps = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                heatmaps.add(referenceImages.getImage(String.format("digit-%d-heatmap.bmp", i)));
            }

            // Each folder corresponds to a set of digits, all of which are the same type.
            // 2: Classify a batch of digits
            for (int candidateLabel = 0; candidateLabel < 10; candidateLabel++) {
                File locationOfImages = new File(String.format("img/mnist-tests-1/%d", candidateLabel));
                ImgReader candidateReader = new ImgReader(String.format("img/mnist-tests-1/%d", candidateLabel));
                List<String> testImages = Arrays.stream(Objects.requireNonNull(locationOfImages.listFiles())).map(File::getName).toList();


                // 3: Classify each candidate in the batch
                for (String candidateImageName : testImages) {
                    System.out.printf("Classifying %s%n", candidateImageName);
                    BufferedImage candidateImage = candidateReader.getImage(candidateImageName);
                    // This code uses 10 threads to classify each digit against the same set of heatmap references.

                    // Before we do the classification, we randomly shift the candidate to show that we're transformation invariant.
                    // MAKE SURE the threshold here matches the one set in ThreadableImageClassification
                    // This is a bit of a hack NGL
                    List<AbstractPixel> candidatePixels = ImgReader.convertToAbstractPixelsViaKernel(candidateImage, 0.1);
                    Vec5D randomTransformBounds = new Vec5D(new double[]{
                            5,
                            5,
                            1.5,
                            0.1,
                            0.1
                    });
                    List<AbstractPixel> randomlyShiftedPixels = RandomTransformer.randomTRS(randomTransformBounds, candidatePixels);
                    double[] randomShift = RandomTransformer.lastShift;

                    // This is the multithreaded part so there should be no issue making lastShift static.
                    ArrayList<ImageClassificationResult<Vec5D>> results = classifyThisCandidate(candidateLabel, randomlyShiftedPixels, heatmaps, pool);

                    int classifiedAs = -1;
                    double bestFitnessScore = Double.MAX_VALUE;
                    double[] allScores = new double[10];

                    // 4: Record each comparison and see which one was best.
                    for (int i = 0; i < 10; i++) {
                        ImageClassificationResult<Vec5D> res = results.get(i);
                        double[] solVec = res.result().solution().components();

                        comparisons.writeLine(String.format(
                                "%d,%d,%s,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%d",
                                res.referenceDigit(),
                                res.candidateDigit(),
                                candidateImageName,
                                res.result().fitnessScore(),
                                solVec[0],
                                solVec[1],
                                solVec[2],
                                solVec[3],
                                solVec[4],
                                res.iterations()
                        ));

                        if (res.result().fitnessScore() < bestFitnessScore) {
                            bestFitnessScore = res.result().fitnessScore();
                            classifiedAs = res.referenceDigit();
                        }
                        allScores[i] = res.result().fitnessScore();
                    }

                    // 5: Record the classification of this digit.
                    classifications.writeLine(String.format(
                            "%d,%s,%d,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f,%.05f",
                            candidateLabel,
                            candidateImageName,
                            classifiedAs,
                            allScores[0],
                            allScores[1],
                            allScores[2],
                            allScores[3],
                            allScores[4],
                            allScores[5],
                            allScores[6],
                            allScores[7],
                            allScores[8],
                            allScores[9],
                            randomShift[0],
                            randomShift[1],
                            randomShift[2],
                            randomShift[3],
                            randomShift[4]
                    ));
                }
            }
        } finally {
            // No matter what happens, clean up your file handles!
            comparisons.close();
            classifications.close();
            pool.shutdownNow();
        }
    }

    public static void testPair() throws IOException {
        ImgReader candidateReader = new ImgReader("img/mnist-tests-1/4");
        ImgReader refReader = new ImgReader("img/heatmaps");

        String candidateName = "d4-2212";
        String referenceName = "digit-4-heatmap";

        BufferedImage cand = candidateReader.getImage(String.format("%s.bmp", candidateName));
        BufferedImage ref = refReader.getImage(String.format("%s.bmp", referenceName));
      
        List<AbstractPixel> candidatePixels = ImgReader.convertToAbstractPixels(cand, 0.10);
        Vec5D randomTransformBounds = new Vec5D(new double[]{
                5,
                5,
                1.5,
                0.1,
                0.1
        });
        List<AbstractPixel> randomlyShiftedPixels = RandomTransformer.randomTRS(randomTransformBounds, candidatePixels);

        ImageTRS objectiveFunction = new ImageTRS(ref, randomlyShiftedPixels, 0.10, false);

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
        System.out.printf("Fitness Score was: %.05f%n", foundMinimum.fitnessScore());
        System.out.println(foundMinimum.solution());

        List<AbstractPixel> finalCandidatePoints = objectiveFunction.getLastSetOfCandidatePoints();
        List<AbstractPixel> referencePoints = objectiveFunction.getLastSetOfReferencePoints();

        PointCloud.drawDigitComparison(
                referencePoints,
                String.format("r-%s.txt",referenceName),
                finalCandidatePoints,
                String.format("c-%s.txt", candidateName)
        );
    }

    public static ArrayList<ImageClassificationResult<Vec5D>> classifyThisCandidate(
            int candidateLabel,
            List<AbstractPixel> candidateImage,
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
