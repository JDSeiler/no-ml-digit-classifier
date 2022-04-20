package org.ru.img;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for reading off the file system and processing them into more useful formats.
 */
public class ImgReader {
    private Path BASE_IMAGE_DIRECTORY = Paths.get("").toAbsolutePath();

    /**
     * @return The absolute path to the directory this ImgReader is reading images from.
     */
    public Path getBaseImageDirectory() {
        return BASE_IMAGE_DIRECTORY;
    }

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
     * Reads an image given its location relative to this ImgReader's base image directory.
     * For example:
     * <pre>
     * {@code
     * // base image directory is /foo/bar
     * ImgReader reader = new ImageReader();
     * // reads from /foo/bar/img/dog.png
     * reader.getImage("/img/dog.png");
     * }
     * </pre>
     * @param location The "unix-like" path to the image, relative to the base image directory.
     * @return the BufferedImage at the specified location, or null if it cannot be found / another error occurred.
     */
    public BufferedImage getImage(String location) {
        Path imgLocation = this.BASE_IMAGE_DIRECTORY.resolve(location);
        try {
            return ImageIO.read(new File(imgLocation.toString()));
        } catch (IOException e) {
            System.err.printf("Error reading image file at %s%n", imgLocation);
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * Converts a BufferedImage into a 2D array, where each element (i,j) is a
     * tuple (represented as an array) representing the RGB values of
     * the pixel at (i,j) in the image.
     * @param img the image to convert
     * @param numComponentsPerPixel the number of components that make up the pixel. A normal
     *                              RGB pixel will have 3 components. But images with an alpha
     *                              channel require 4.
     * @return the image contents as a 2D array of RGB values
     */
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

                // The AWT library goes by X, Y. Where X increases left-right, and Y increases top-down
                // 0,0 is in the top left in both coordinate systems. To convert from X,Y to Row,Col
                // just switch the order.
                raster.getPixels(c, r, 1, 1, pixels[r][c]);
            }
        }
        return pixels;
    }

    /**
     * Converts a BufferedImage into a list of AbstractPixels. *It is expected that the
     * image is in black and white*
     * @param img the image to convert
     * @param threshold a value in the range [0, 1] describing how dark a pixel must be for it to
     *                  be returned in the result. A value of 0 means all pixels will be returned,
     *                  while a value of 1 means only pure-black pixels will be returned.
     * @return the contents of the image as a list of abstract pixels, such that all returned
     * pixels are at least `threshold` percent black.
     */
    public static List<AbstractPixel> convertToAbstractPixels(BufferedImage img, double threshold) {
        // Pre-allocate some space in the array, use (# pixels) / 3 as a wild guess
        int w = img.getWidth();
        int h = img.getHeight();
        Raster raster = img.getData();
        int guessNumberOfPixels = (int) Math.floor(w * h / 3.0);

        ArrayList<AbstractPixel> pixels = new ArrayList<>(guessNumberOfPixels);
        for (int r = 0; r < w; r++) {
            for (int c = 0; c < h; c++) {
                // See comment in convertToPixelGrid about R,C vs X,Y
                int[] pixelData = raster.getPixels(c, r, 1, 1, (int[]) null);
                // Make the potentially unsafe assumption that the RGB values are all the same
                // No fast way I can think of to check the image is gr
                int grayscaleByte = pixelData[0];
                double greyscaleValue = 1.0 - (grayscaleByte / 255.0);
                // TODO: Do a fuzzy check against threshold - floating point issues...
                if (greyscaleValue >= threshold) {
                    // I suppose I also want AbstractPixels to be X,Y based
                    pixels.add(new AbstractPixel(c, r, greyscaleValue));
                }
            }
        }
        return pixels;
    }
}
