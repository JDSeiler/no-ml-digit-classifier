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

    /*
    * This business of returning FixedVector means you have to do unchecked casts
    * when you have two concrete implementations of FixedVector and you want to add
    * them and get a thing of the same type.
    *
    * you can fix this by making FixedVector generic with a bound over itself?
    * `public interface FixedVector<T extends FixedVector<T>>`
    * Then make everything take and return `T`.
    *
    * To implement it, you say:
    * `public MyVec implements FixedVector<MyVec>`
    *
    * To use it, you change:
    * `public VecUser<V extends FixedVector>`
    * to:
    * `public VecUser<V extends FixedVector<V>>`
    *
    * I don't know which situation is worse?
    * */

    FixedVector scale(double scalar);

    FixedVector add(FixedVector other);

    FixedVector subtract(FixedVector other);

    /**
     * Randomly scale each component of the vector individually using values
     * uniformly distributed in the range [0, 1)
     * @return the jittered vector
     */
    FixedVector jitter();
}
