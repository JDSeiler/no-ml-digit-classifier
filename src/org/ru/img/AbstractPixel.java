package org.ru.img;

/**
 * An AbstractPixel is a mathematical point representation of a pixel.
 * The `x` and `y` coordinates represent the position of the pixel.
 * The `grayscaleValue` is how dark the pixel is. 0 being pure white
 * and 100 being pure black
 */
public record AbstractPixel(double x, double y, double grayscaleValue) {
}
