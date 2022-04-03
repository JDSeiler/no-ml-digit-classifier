package org.ru.vec;

import java.util.Arrays;

public class Vec5D implements FixedVector {
    private static final int size = 5;
    private double[] v;

    public Vec5D() {
        v = new double[Vec5D.size];
        Arrays.fill(v, 0.0);
    }

    public Vec5D(double[] initialValues) {
        if (initialValues.length != Vec5D.size) {
            throw new AssertionError(String.format("org.ru.vec.Vec5D expects 5 elements, got %d", initialValues.length));
        }
        v = initialValues;
    }

    @Override
    public int size() {
        return Vec5D.size;
    }

    @Override
    public double[] components() {
        return Arrays.copyOf(v, Vec5D.size);
    }

    @Override
    public void set (double[] newValues) {
        if (newValues.length != Vec5D.size) {
            throw new AssertionError(String.format("org.ru.vec.Vec5D expects 5 elements, got %d", newValues.length));
        }
        v = newValues;
    }

    @Override
    public Vec5D scale(double scalar) {
        double[] newVec = Arrays.stream(this.v).map((double vi) -> vi*scalar).toArray();
        return new Vec5D(newVec);
    }

    @Override
    public Vec5D add(FixedVector other) {
        if (other.size() != this.size()){
            throw new AssertionError(String.format("Vectors must match in size!%n This vector: %d %n Other vector %d %n", this.size(), other.size()));
        }

        double[] u = other.components();
        double[] newVec = new double[this.size()];
        for (int i = 0; i < this.v.length; i++) {
            newVec[i] = this.v[i] + u[i];
        }

        return new Vec5D(newVec);
    }

    @Override
    public Vec5D subtract(FixedVector other) {
        if (other.size() != this.size()){
            throw new AssertionError(String.format("Vectors must match in size!%n This vector: %d %n Other vector %d %n", this.size(), other.size()));
        }

        double[] u = other.components();
        double[] newVec = new double[this.size()];
        for (int i = 0; i < this.v.length; i++) {
            newVec[i] = this.v[i] - u[i];
        }

        return new Vec5D(newVec);
    }

    @Override
    public Vec5D jitter() {
        double[] jitteredValues = new double[this.size()];
        for (int i = 0; i < jitteredValues.length; i++) {
            jitteredValues[i] = this.v[i] * Math.random();
        }
        return new Vec5D(jitteredValues);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.v);
    }
}
