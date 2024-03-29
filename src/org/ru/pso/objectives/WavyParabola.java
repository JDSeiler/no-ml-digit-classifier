package org.ru.pso.objectives;

import org.ru.vec.Vec2D;

public class WavyParabola {
    public static double compute(Vec2D v) {
        double x = v.components()[0];
        double y = v.components()[1];

        double xTerm = Math.pow((2d*x + 5d*Math.sin(x)), 2);
        double yTerm = Math.pow((2d*y + 5d*Math.sin(y)), 2);

        return (xTerm + yTerm) / 40.0d;
    }
}
