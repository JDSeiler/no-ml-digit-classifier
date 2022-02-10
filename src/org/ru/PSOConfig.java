package org.ru;

import org.ru.strategies.Placement;
import org.ru.strategies.Topology;

public record PSOConfig<V extends FixedVector>(
        int swarmSize,
        double inertiaScalar,
        double personalBestScalar,
        double neighborHoodBestScalar,
        Topology neighborhoodTopology,
        Placement placementStrategy,
        double initialVelocityBound,
        V initializationRegionMaximums // Take the negative of each v[i] for the other bound
    ) {
}
