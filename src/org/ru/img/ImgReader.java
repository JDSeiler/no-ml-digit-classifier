package org.ru.img;

import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.nio.file.Path;

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
     * @return
     */
    public BufferedImage getImage(String location) {
        Path imgLocation = this.BASE_IMAGE_DIRECTORY.resolve(location);
        // TODO
    }
}
