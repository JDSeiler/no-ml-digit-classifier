package org.ru.drawing;

import org.ru.img.AbstractPixel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PointCloud {

    public static void drawDigitComparison(List<AbstractPixel> reference, String referenceLabel, List<AbstractPixel> candidate, String candidateLabel) throws IOException {
        // TODO: Delete temporary files after?
        dumpToFile(reference, referenceLabel);
        dumpToFile(candidate, candidateLabel);
        invokeMatplotLib(referenceLabel, candidateLabel);
    }
    public static void invokeMatplotLib(String referenceFileName, String candidateFileName) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "python",
                "./util/draw_image_comparison.py",
                String.format("./temp/%s", referenceFileName),
                String.format("./temp/%s", candidateFileName)
                );
        pb.inheritIO();
        pb.start();
    }

    public static void dumpToFile(List<AbstractPixel> pixels) {
        Random rand = new Random();
        int id = rand.nextInt(10_000);

        String fileName = String.format("points-%04d.txt", id);
        dumpToFile(pixels, fileName);
    }

    public static void dumpToFile(List<AbstractPixel> pixels, String fileName) {
        StringBuilder output = new StringBuilder();

        output.append("num_lines_metadata: 4\n");
        output.append("type: PIXEL_LIST\n");
        output.append("origin: TOP_LEFT\n");
        output.append("pos_x: RIGHT\n");
        output.append("pos_y: DOWN\n\n");

        for(AbstractPixel p : pixels) {
            if (!p.isDud()) {
                output.append(
                        String.format("%.5f, %.5f, %.5f\n", p.x(), p.y(), p.grayscaleValue())
                );
            }
        }
        writeFile(output.toString().getBytes(StandardCharsets.UTF_8), fileName);
    }

    private static void writeFile(byte[] data, String fileName) {
        Path p = Paths.get(String.format("./temp/%s", fileName));

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING))) {

            out.write(data, 0, data.length);
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
}
