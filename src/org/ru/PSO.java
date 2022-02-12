package org.ru;

import org.ru.strategies.Placement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PSO<V extends FixedVector> {
    private final List<Particle<V>> swarm;
    private final Function<V, Double> cost;
    private final PSOConfig<V> config;
    // A Supplier<T> is just a parameterless function which can give you a T
    // It's a surrogate for being able to instantiate generic types
    // directly. Which Java cannot do because its generic system
    // is implemented with type erasure.
    private final Supplier<V> vectorFactory;

    PSO(PSOConfig<V> config, Function<V, Double> objectiveFunction, Supplier<V> vectorFactory) {
        this.swarm = new ArrayList<>(config.swarmSize());
        this.config = config;
        this.cost = objectiveFunction;
        this.vectorFactory = vectorFactory;

        if (this.config.placementStrategy() == Placement.RANDOM) {
            this.randomlyInitializeSwarm();
        } else {
            System.out.println("RANDOM is the only supported placement strategy");
            System.exit(1);
        }
    }

    private void randomlyInitializeSwarm() {
        // `initializationRegionMaximums` is one corner of an N-dimensional cube.
        // This corner must have strictly positive values. From this we can easily
        // infer min/max values for the initialization region.
        double[] bounds = config.initializationRegionMaximums().components();

        // For each particle...
        for (int i = 0; i < config.swarmSize(); i++) {
            double[] thisParticlesPos = new double[bounds.length];
            double[] thisParticleVel = new double[bounds.length];
            // For each component bound...
            for (int j = 0; j < bounds.length; j++) {
                double posMax = bounds[j];
                double posMin = -posMax;

                thisParticlesPos[j] = this.generateInRange(posMin, posMax);
                thisParticleVel[j] = this.generateInRange(
                        -config.initialVelocityBound(),
                        config.initialVelocityBound()
                );
            }
            // Hmm, this is a sign of trouble I think. This is not idiomatic with Java's type erasure.
            // Tolerable for now. Can always pack functionality away in the FixedVector interface.
            V positionVec = vectorFactory.get();
            positionVec.set(thisParticlesPos);

            V velVec = vectorFactory.get();
            velVec.set(thisParticleVel);

            this.swarm.add(new Particle<>(positionVec, velVec));
        }
    }

    private double generateInRange(double min, double max) {
        // This method is what the Math.random() docs suggest for generating a value
        // between two values, inclusively.
        double interpolationFactor = Math.random()/Math.nextDown(1.0);
        // This formula is the linear interpolation (convex combination) between min and max.
        return min*(1.0-interpolationFactor) + max*interpolationFactor;
    }

    /*
    * A good objective function to start testing with:
    * [(x+1.1sin(x))^2 + (y+1.1cos(y))^2] / 10
    *
    * The derivation is as follows:
    * x^2 + y^2 gives a 3D parabola. A parabola rotated around the y-axis.
    *
    * Adding n*sin(x) and m*cos(y) makes the sides of the parabola "wavy".
    * It creates local minimums all along the sides of the "bowl". The larger
    * n and m are the more pronounced the minimums are. If n and m are 1, there
    * are no minimums. The side of the bowl just briefly flattens out before continuing
    * down. So keeping the scalars at 1 to start would be an easy first test.
    *
    * The final division by 10 is just to stop the function from growing too fast.
    * */
}
