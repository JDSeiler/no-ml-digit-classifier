package org.ru.vec;

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
            throw new AssertionError(String.format("org.ru.vec.Vec2D expects 2 elements, got %d", initialValues.length));
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
            throw new AssertionError(String.format("org.ru.vec.Vec2D expects 2 elements, got %d", newValues.length));
        }
        v = newValues;
    }

    // TODO: Add, subtract, and scale, can be implemented once generically. Worth it?

    @Override
    public Vec2D scale(double scalar) {
        double[] newVec = Arrays.stream(this.v).map((double vi) -> vi*scalar).toArray();
        return new Vec2D(newVec);
    }

    @Override
    public Vec2D add(FixedVector other) {
        if (other.size() != this.size()){
            throw new AssertionError(String.format("Vectors must match in size!%n This vector: %d %n Other vector %d %n", this.size(), other.size()));
        }

        double[] u = other.components();
        double[] newVec = new double[this.size()];
        for (int i = 0; i < this.v.length; i++) {
            newVec[i] = this.v[i] + u[i];
        }

        return new Vec2D(newVec);
    }

    @Override
    public Vec2D subtract(FixedVector other) {
        if (other.size() != this.size()){
            throw new AssertionError(String.format("Vectors must match in size!%n This vector: %d %n Other vector %d %n", this.size(), other.size()));
        }

        double[] u = other.components();
        double[] newVec = new double[this.size()];
        for (int i = 0; i < this.v.length; i++) {
            newVec[i] = this.v[i] - u[i];
        }

        return new Vec2D(newVec);
    }

    @Override
    public Vec2D jitter() {
        double[] jitteredValues = new double[this.size()];
        for (int i = 0; i < jitteredValues.length; i++) {
            jitteredValues[i] = this.v[i] * Math.random();
        }
        return new Vec2D(jitteredValues);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.v);
    }
}
