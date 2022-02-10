import java.util.Arrays;

public class Vec2D implements FixedVector {
    private static int size = 2;
    private double[] v;

    public Vec2D() {
        v = new double[this.size];
        Arrays.fill(v, 0.0);
    }

    public Vec2D(double[] initialValues) {
        if (initialValues.length > 2) {
            throw new AssertionError(String.format("Vec2D expects 2 elements, got %d", initialValues.length));
        }
        v = initialValues;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public double[] components() {
        return Arrays.copyOf(v, this.size);
    }

    @Override
    public void set(double[] newValues) {
        if (newValues.length > 2) {
            throw new AssertionError(String.format("Vec2D expects 2 elements, got %d", newValues.length));
        }
        v = newValues;
    }
}
