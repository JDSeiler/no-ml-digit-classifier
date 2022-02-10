package org.ru;

public class Main {
    /**
     * Implementation plan:
     * 1. Need a central list of particles
     * 2. Every particle needs access to the objective function
     *     a) In the context of image recog., the "objective function" can store the images as static properties.
     *        The actual function call will still take a vector as an argument.
     * 3. Need to be able to update each particle. Either the particle updates itself, or a function computes a new particle.
     * 4. Ideally all of this should be agnostic to the dimension of the feasible region.
     * */
    public static void main(String[] args) {
        System.out.println("Hello world!");
        PSO<Vec2D> pso = new PSO<>(10, ObjectiveFunctions::wavyParabola);
    }
}
