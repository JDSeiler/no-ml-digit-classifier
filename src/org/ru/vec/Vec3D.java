package org.ru.vec;

import java.util.Arrays;

public class Vec3D implements FixedVector {
    private static final int size = 3;
    private double[] v;

    public Vec3D() {
        v = new double[Vec3D.size];
        Arrays.fill(v, 0.0);
    }

    public Vec3D(double[] initialValues) {
        if (initialValues.length != 3) {
            throw new AssertionError(String.format("org.ru.vec.Vec3D expects 3 elements, got %d", initialValues.length));
        }
        v = initialValues;
    }

    @Override
    public int size() {
        return Vec3D.size;
    }

    @Override
    public double[] components() {
        return Arrays.copyOf(v, Vec3D.size);
    }

    @Override
    public void set(double[] newValues) {
        if (newValues.length != 3) {
            throw new AssertionError(String.format("org.ru.vec.Vec3D expects 3 elements, got %d", newValues.length));
        }
        v = newValues;
    }

    @Override
    public Vec3D scale(double scalar) {
        double[] newVec = Arrays.stream(this.v).map((double vi) -> vi*scalar).toArray();
        return new Vec3D(newVec);
    }

    @Override
    public Vec3D add(FixedVector other) {
        if (other.size() != this.size()){
            throw new AssertionError(String.format("Vectors must match in size!%n This vector: %d %n Other vector %d %n", this.size(), other.size()));
        }

        double[] u = other.components();
        double[] newVec = new double[this.size()];
        for (int i = 0; i < this.v.length; i++) {
            newVec[i] = this.v[i] + u[i];
        }

        return new Vec3D(newVec);
    }

    @Override
    public Vec3D subtract(FixedVector other) {
        if (other.size() != this.size()){
            throw new AssertionError(String.format("Vectors must match in size!%n This vector: %d %n Other vector %d %n", this.size(), other.size()));
        }

        double[] u = other.components();
        double[] newVec = new double[this.size()];
        for (int i = 0; i < this.v.length; i++) {
            newVec[i] = this.v[i] - u[i];
        }

        return new Vec3D(newVec);
    }

    @Override
    public Vec3D jitter() {
        double[] jitteredValues = new double[this.size()];
        for (int i = 0; i < jitteredValues.length; i++) {
            jitteredValues[i] = this.v[i] * Math.random();
        }
        return new Vec3D(jitteredValues);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.v);
    }
}
