package org.ru.pso;

import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.FixedVector;

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
    private int lastSolutionIterations = 0;

    public PSO(PSOConfig<V> config, Function<V, Double> objectiveFunction, Supplier<V> vectorFactory) {
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

        if (this.config.neighborhoodTopology() != Topology.COMPLETE) {
            System.out.println("COMPLETE is the only supported neighborhood topology");
            System.exit(1);
        }
    }

    public void printSwarm() {
        for (Particle<V> p : this.swarm) {
            System.out.println(p.toString());
        }
    }

    public Solution<V> run() {
        // Make sure to respond to interrupts.
        // If a PSO instance is interrupted, we're probably shutting everything down "barely gracefully"
        // So just return null. The biggest thing is to make sure the thread isn't hanging.
        if (Thread.interrupted()) {
            return null;
        }


        double bestSoFar = Double.MAX_VALUE;
        V locationOfGlobalBest = null;

        boolean bestChangedThisIteration = false;
        int durationBestUnchanged = 0; // measured in iterations

        for (int i = 0; i < 1_000; i++) {
            bestChangedThisIteration = false;

            if (Math.abs(bestSoFar) <= 0.0000001) {
                System.out.printf("Found minimum in %d iterations.%n", i);
                lastSolutionIterations = i;
                return new Solution<>(bestSoFar, locationOfGlobalBest);
            }

            if (durationBestUnchanged >= 150) {
                System.out.printf("Global best has stopped converging after %d iterations.%n", i);
                lastSolutionIterations = i;
                return new Solution<>(bestSoFar, locationOfGlobalBest);
            }

            /*
            * What I'll try, could have issues with off-by-one or other mistakes
            * but I have books/papers to refer to if it goes really poorly.
            * 1. Move each particle (I guess it's ok to move first?)
            * 2. Calculate the cost function for each particle
            * 3. Update personal bests and global best
            * 4. Calculate new velocities of each particle
            * */
            for(Particle<V> p : this.swarm) {
                p.move();
                double particleFitness = cost.apply(p.getPos());

                if (particleFitness < bestSoFar) {
                    double oldBest = bestSoFar;
                    bestSoFar = particleFitness;
                    locationOfGlobalBest = p.getPos();

                    if (Math.abs(bestSoFar - oldBest) > 0.0001) {
                        bestChangedThisIteration = true;
                        durationBestUnchanged = 0;
                    }
                }

                if (particleFitness < p.getPersonalBestFitness()) {
                    p.setPersonalBest(particleFitness);
                }
            }
            for(Particle<V> p : this.swarm) {
                assert locationOfGlobalBest != null : "Global best should not be null after first iteration!";
                p.update(
                        locationOfGlobalBest,
                        config.inertiaScalar(),
                        config.personalBestScalar(),
                        config.neighborHoodBestScalar()
                );
            }
            if (!bestChangedThisIteration) {
                durationBestUnchanged++;
            }
        }
        System.out.println("PSO did not fully converge within 1_000 iterations.");
        lastSolutionIterations = 1_000;
        return new Solution<>(bestSoFar, locationOfGlobalBest);
    }

    private void randomlyInitializeSwarm() {
        // `initializationRegionMaximums` is one corner of an N-dimensional box.
        // This corner must have strictly positive values. We then assume the
        // other corner of the box is located at this corner * -1 (mirror it through the origin)
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

    public int getLastSolutionIterations() {
        return lastSolutionIterations;
    }
}
