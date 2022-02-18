package org.ru.img;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ImgReader {
    private Path BASE_IMAGE_DIRECTORY = Paths.get("").toAbsolutePath();

    // TODO: Refine these comments, really the directory is the BASE from which
    // I will construct paths to images.

    /**
     * Creates an ImgReader and sets the location of the directory
     * it will read images from to the current working directory
     * of the program. If the current working directory is `/foo/bar`
     * and you request an image `img/dog.png`, the ImgReader
     * will look for the file at `/foo/bar/img/dog.png.
     */
    public ImgReader() {}

    /**
     * Creates an ImgReader and sets the location of the directory
     * it will read images from.
     * @param baseImgDir A "unix-like" path, using forward slash as
     *                   a separator. The path may begin with a forward
     *                   slash, in which case it will be an absolute path.
     *                   Otherwise, the path is made relative to the current
     *                   working directory of the program.
     */
    public ImgReader(String baseImgDir) {
        this.BASE_IMAGE_DIRECTORY = Paths.get(baseImgDir).toAbsolutePath();
    }

    /**
     * TODO: Finish
     * @param location The path to the image, relative to `BASE_IMAGE_DIRECTORY`
     * @return the BufferedImage at the specified location, or null if it cannot be found / another error occurred.
     */
    public BufferedImage getImage(String location) {
        Path imgLocation = this.BASE_IMAGE_DIRECTORY.resolve(location);
        try {
            return ImageIO.read(new File(imgLocation.toString()));
        } catch (IOException e) {
            System.err.println(String.format("Error reading image file at %s", imgLocation));
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static int[][][] convertToPixelGrid(BufferedImage img, int numComponentsPerPixel) {
        int w = img.getWidth();
        int h = img.getHeight();
        Raster raster = img.getData();
        // I don't know if it's a bad ideally to optimistically only allocate 3 slots for color data?
        int[][][] pixels = new int[w][h][numComponentsPerPixel];
        for (int r = 0; r < w; r++) {
            for (int c = 0; c < h; c++) {
                // I don't want to prematurely optimize, but dumping the entire image to memory and
                // manually selecting 3 elements from the 1D sample array could be faster?
                raster.getPixels(r, c, 1, 1, pixels[r][c]);
            }
        }
        return pixels;
    }

    public static List<AbstractPixel> convertToAbstractPixels(BufferedImage img, double threshold) {
        // Pre-allocate some space in the array, use (# pixels) / 3 as a wild guess
        int w = img.getWidth();
        int h = img.getHeight();
        Raster raster = img.getData();
        int guessNumberOfPixels = (int) Math.floor(w * h / 3.0);

        ArrayList<AbstractPixel> pixels = new ArrayList<>(guessNumberOfPixels);
        for (int r = 0; r < w; r++) {
            for (int c = 0; c < h; c++) {
                int[] pixelData = raster.getPixels(r, c, 1, 1, (int[]) null);
                // Make the potentially unsafe assumption that the RGB values are all the same
                // No fast way I can think of to check the image is gr
                int grayscaleByte = pixelData[0];
                double greyscaleValue = 1.0 - (grayscaleByte / 255.0);
                if (greyscaleValue >= threshold) {
                    pixels.add(new AbstractPixel(r, c, greyscaleValue));
                }
            }
        }
        return pixels;
    }
}
