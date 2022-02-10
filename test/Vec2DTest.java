import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.ru.Vec2D;

public class Vec2DTest {
    public static final double DELTA = 0.0000001d;

    @Test
    void sizeCorrectlyReports() {
        assertEquals(2, (new Vec2D()).size());
    }

    @Test
    void bareConstructorSetsAllComponentsZero() {
        Vec2D testVec = new Vec2D();
        for (double vi : testVec.components()) {
            assertEquals(0.0d, vi, DELTA);
        }
    }

    @Test
    void providingDefaultsSetsDefaults() {
        double[] outerVec = {1.0, 4.56};
        Vec2D testVec = new Vec2D(outerVec);

        double[] v = testVec.components();
        for (int i = 0; i < v.length; i++) {
            assertEquals(outerVec[i], v[i], DELTA);
        }
    }

    @Test
    void vectorCopiesProvidedContents() {
        double[] outerVec = {10.0, 10.0};
        Vec2D testVec = new Vec2D(outerVec);

        outerVec[0] = 0.0d;
        outerVec[1] = 0.0d;

        double[] v = testVec.components();
        for (int i = 0; i < v[i]; i++) {
            assertEquals(10.0d, v[i], DELTA);
        }
    }

    @Test
    void settingNewValuesBehavesAsExpected() {
        double[] init = {1.0, 2.0};
        Vec2D testVec = new Vec2D(init);

        double[] v = testVec.components();
        for (int i = 0; i < v.length; i++) {
            assertEquals(init[i], v[i], DELTA);
        }

        double[] newValues = {3.0, 4.0};
        testVec.set(newValues);

        double[] u = testVec.components();
        for (int i = 0; i < u.length; i++) {
            assertEquals(newValues[i], u[i], DELTA);
        }
    }

    @Test
    void constructingWithIncorrectlySizedArrThrows() {
        assertThrows(AssertionError.class, () -> {
           double[] badInit = {1.0, 2.0, 3.0};
           new Vec2D(badInit);
        });

        assertThrows(AssertionError.class, () -> {
            double[] badInit = {1.0};
            new Vec2D(badInit);
        });
    }

    @Test
    void settingWithIncorrectlySizedArrThrows() {
        assertThrows(AssertionError.class, () -> {
            Vec2D testVec = new Vec2D();
            double[] badNewVals = {1.0, 2.0, 3.0};
            testVec.set(badNewVals);
        });

        assertThrows(AssertionError.class, () -> {
            Vec2D testVec = new Vec2D();
            double[] badNewVals = {1.0};
            testVec.set(badNewVals);
        });
    }
}
