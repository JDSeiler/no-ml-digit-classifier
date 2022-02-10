public interface FixedVector {
    /**
     * @return the number of elements in the FixedVector
     */
    int size();

    /**
     * @return the components of the vector as an array
     */
    double[] components();

    /**
     * @param newValues the new values to give to this FixedVector
     * @precondition the length of `newValues` **must** be equal to the return value of `size()`
     */
    void set(double[] newValues);
}
