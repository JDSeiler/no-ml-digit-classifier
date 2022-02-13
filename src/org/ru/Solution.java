package org.ru;

public record Solution<V extends FixedVector>(double fitnessScore, V solution) {
}
