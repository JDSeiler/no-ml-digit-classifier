package org.ru;

import java.util.Arrays;

public class Vec2D implements FixedVector {
    private static final int size = 2;
    private double[] v;

    public Vec2D() {
        v = new double[Vec2D.size];
        Arrays.fill(v, 0.0);
    }

    public Vec2D(double[] initialValues) {
        if (initialValues.length != 2) {
            throw new AssertionError(String.format("org.ru.Vec2D expects 2 elements, got %d", initialValues.length));
        }
        v = initialValues;
    }

    @Override
    public  int size() {
        return Vec2D.size;
    }

    @Override
    public double[] components() {
        return Arrays.copyOf(v, Vec2D.size);
    }

    @Override
    public void set(double[] newValues) {
        if (newValues.length != 2) {
            throw new AssertionError(String.format("org.ru.Vec2D expects 2 elements, got %d", newValues.length));
        }
        v = newValues;
    }
}
