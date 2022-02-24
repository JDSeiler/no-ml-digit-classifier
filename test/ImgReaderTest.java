import org.junit.jupiter.api.Test;

import org.ru.img.AbstractPixel;
import org.ru.img.ImgReader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImgReaderTest {
    @Test
    void successfullyReadsImage() {
        ImgReader reader = new ImgReader("test/fixture");
        BufferedImage img = reader.getImage("tiny-chevron.bmp");
        assertNotNull(img);
    }

    @Test
    void baseDirectoryCorrectlySet() {
        ImgReader reader = new ImgReader("test/fixture");
        assertTrue(reader.getBaseImageDirectory().endsWith("itec498/test/fixture"));
    }

    @Test
    void defaultBaseDirectoryIsCorrect() {
        ImgReader reader = new ImgReader();
        assertTrue(reader.getBaseImageDirectory().endsWith("itec498"));
    }

    @Test
    void convertToPixelGridReturnsCorrectResult() {
        ImgReader reader = new ImgReader("test/fixture");
        BufferedImage img = reader.getImage("tiny-chevron.bmp");

        int[][][] actual  = ImgReader.convertToPixelGrid(img, 3);
        int[][][] expected = new int[][][]{
                new int[][]{
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255}
                },
                new int[][]{
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255},
                        new int[]{0, 0, 0},
                        new int[]{255, 255, 255}
                },
                new int[][]{
                        new int[]{255, 255, 255},
                        new int[]{0, 0, 0},
                        new int[]{0, 0, 0},
                        new int[]{255, 255, 255}
                },
                new int[][]{
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255},
                        new int[]{255, 255, 255}
                }
        };

        for (int i = 0; i < actual.length; i++) {
            for (int j = 0; j < actual[i].length; j++) {
                assertArrayEquals(expected[i][j], actual[i][j]);
            }
        }
    }

    @Test
    void convertToAbstractPixelsReturnsAllPixelsAtZeroThreshold() {
        ImgReader reader = new ImgReader("test/fixture");
        BufferedImage img = reader.getImage("tiny-chevron.bmp");

        List<AbstractPixel> actual  = ImgReader.convertToAbstractPixels(img, 0);
        List<AbstractPixel> expected = new ArrayList<>();
        expected.add(new AbstractPixel(0, 0, 0));
        expected.add(new AbstractPixel(1, 0, 0));
        expected.add(new AbstractPixel(2, 0, 0));
        expected.add(new AbstractPixel(3, 0, 0));

        expected.add(new AbstractPixel(0, 1, 0));
        expected.add(new AbstractPixel(1, 1, 0));
        expected.add(new AbstractPixel(2, 1, 1));
        expected.add(new AbstractPixel(3, 1, 0));

        expected.add(new AbstractPixel(0, 2, 0));
        expected.add(new AbstractPixel(1, 2, 1));
        expected.add(new AbstractPixel(2, 2, 1));
        expected.add(new AbstractPixel(3, 2, 0));

        expected.add(new AbstractPixel(0, 3, 0));
        expected.add(new AbstractPixel(1, 3, 0));
        expected.add(new AbstractPixel(2, 3, 0));
        expected.add(new AbstractPixel(3, 3, 0));

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    void convertToAbstractPixelsOnlyReturnsBlackWithFullThreshold() {

        ImgReader reader = new ImgReader("test/fixture");
        BufferedImage img = reader.getImage("tiny-chevron.bmp");

        List<AbstractPixel> actual  = ImgReader.convertToAbstractPixels(img, 1);
        List<AbstractPixel> expected = new ArrayList<>();

        expected.add(new AbstractPixel(2, 1, 1));
        expected.add(new AbstractPixel(1, 2, 1));
        expected.add(new AbstractPixel(2, 2, 1));

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
