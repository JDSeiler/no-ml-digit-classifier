package org.ru;

public interface FixedVector {
    /**
     * @return the number of elements in the org.ru.FixedVector
     */
    int size();

    /**
     * @return the components of the vector as an array
     */
    double[] components();

    /**
     * @param newValues the new values to give to this org.ru.FixedVector
     * @precondition the length of `newValues` **must** be equal to the return value of `size()`
     */
    void set(double[] newValues);

    void scale(double scalar);

    void add(FixedVector other);

    void subtract(FixedVector other);
}
