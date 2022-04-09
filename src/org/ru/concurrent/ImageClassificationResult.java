package org.ru.concurrent;

import org.ru.pso.Solution;
import org.ru.vec.FixedVector;


public record ImageClassificationResult<V extends FixedVector>(int referenceDigit, int candidateDigit, Solution<V> result, int iterations) {
}
