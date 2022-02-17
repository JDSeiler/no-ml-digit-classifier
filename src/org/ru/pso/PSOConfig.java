package org.ru.pso;

import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.FixedVector;

public record PSOConfig<V extends FixedVector>(
        int swarmSize,
        double inertiaScalar,
        double personalBestScalar,
        double neighborHoodBestScalar,
        Topology neighborhoodTopology,
        Placement placementStrategy,
        double initialVelocityBound,
        V initializationRegionMaximums // Take the negative of each v[i] for the other bound
        // TODO: Consider adding a "maximumVelocity" parameter to prevent explosions
    ) {
}
