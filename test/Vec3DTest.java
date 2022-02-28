import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.ru.vec.Vec2D;
import org.ru.vec.Vec3D;

public class Vec3DTest {
    public static final double DELTA = 0.0000001d;

    @Test
    void sizeCorrectlyReports() {
        assertEquals(3, (new Vec3D()).size());
    }

    @Test
    void bareConstructorSetsAllComponentsZero() {
        Vec3D testVec = new Vec3D();
        for (double vi : testVec.components()) {
            assertEquals(0.0d, vi, DELTA);
        }
    }

    @Test
    void providingDefaultsSetsDefaults() {
        double[] outerVec = {1.0, 4.56, -9.0};
        Vec3D testVec = new Vec3D(outerVec);

        double[] v = testVec.components();
        for (int i = 0; i < v.length; i++) {
            assertEquals(outerVec[i], v[i], DELTA);
        }
    }

    @Test
    void vectorCopiesProvidedContents() {
        double[] outerVec = {10.0, 10.0, 10.0};
        Vec3D testVec = new Vec3D(outerVec);

        outerVec[0] = 0.0d;
        outerVec[1] = 0.0d;
        outerVec[2] = 0.0d;

        double[] v = testVec.components();
        for (int i = 0; i < v[i]; i++) {
            assertEquals(10.0d, v[i], DELTA);
        }
    }

    @Test
    void settingNewValuesBehavesAsExpected() {
        double[] init = {1.0, 2.0, 3.0};
        Vec3D testVec = new Vec3D(init);

        double[] v = testVec.components();
        for (int i = 0; i < v.length; i++) {
            assertEquals(init[i], v[i], DELTA);
        }

        double[] newValues = {3.0, 4.0, 5.0};
        testVec.set(newValues);

        double[] u = testVec.components();
        for (int i = 0; i < u.length; i++) {
            assertEquals(newValues[i], u[i], DELTA);
        }
    }

    @Test
    void constructingWithIncorrectlySizedArrThrows() {
        assertThrows(AssertionError.class, () -> {
            double[] badInit = {1.0, 2.0, 3.0, 4.0};
            new Vec3D(badInit);
        });

        assertThrows(AssertionError.class, () -> {
            double[] badInit = {1.0, 2.0};
            new Vec3D(badInit);
        });
    }

    @Test
    void settingWithIncorrectlySizedArrThrows() {
        assertThrows(AssertionError.class, () -> {
            Vec3D testVec = new Vec3D();
            double[] badNewVals = {1.0, 2.0, 3.0, 4.0};
            testVec.set(badNewVals);
        });

        assertThrows(AssertionError.class, () -> {
            Vec3D testVec = new Vec3D();
            double[] badNewVals = {1.0};
            testVec.set(badNewVals);
        });
    }

    @Test
    void additionBehavesAsExpected() {
        Vec3D u = new Vec3D(new double[] {1.0, 2.0, 3.0});
        Vec3D v = new Vec3D(new double[] {4.0, 3.0, 2.0});
        double[] res = u.add(v).components();

        assertEquals(res[0], 5.0, DELTA);
        assertEquals(res[1], 5.0, DELTA);
        assertEquals(res[2], 5.0, DELTA);
    }

    @Test
    void subtractionBehavesAsExpected() {
        Vec3D u = new Vec3D(new double[] {1.0, 2.0, 3.0});
        Vec3D v = new Vec3D(new double[] {4.0, 3.0, 2.0});
        double[] res = u.subtract(v).components();

        assertEquals(res[0], -3.0, DELTA);
        assertEquals(res[1], -1.0, DELTA);
        assertEquals(res[2], 1.0, DELTA);
    }

    @Test
    void scalingBehavesAsExpected() {
        Vec3D u = new Vec3D(new double[] {1.0, 2.0, 3.0});
        double[] res = u.scale(5.0).components();

        assertEquals(res[0], 5.0, DELTA);
        assertEquals(res[1], 10.0, DELTA);
        assertEquals(res[2], 15.0, DELTA);
    }

    @Test
    void additionThrowsIfSizeMismatch() {
        Vec3D u = new Vec3D(new double[] {1.0, 2.0, 3.0});
        Vec2D v = new Vec2D(new double[] {4.0, 3.0});

        assertThrows(AssertionError.class, () -> {
            u.add(v);
        });
    }

    @Test
    void subtractionThrowsIfSizeMismatch() {
        Vec3D u = new Vec3D(new double[] {1.0, 2.0, 3.0});
        Vec2D v = new Vec2D(new double[] {4.0, 3.0});

        assertThrows(AssertionError.class, () -> {
            u.subtract(v);
        });
    }
}
