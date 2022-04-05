package org.ru.pso;

import org.ru.vec.FixedVector;

public record Solution<V extends FixedVector>(double fitnessScore, V solution) {
    @Override
    public String toString() {
        return "Solution{" +
                "fitnessScore=" + fitnessScore +
                ", solution=" + solution +
                '}';
    }
}
