package org.ru;

public class ObjectiveFunctions {
    public static double wavyParabola(Vec2D v) {
        double x = v.components()[0];
        double y = v.components()[1];

        double xTerm = Math.pow((x+1.1d*Math.sin(x)), 2);
        double yTerm = Math.pow((y+1.1d*Math.cos(y)), 2);

        return (xTerm + yTerm) / 10.0d;
    }
}
