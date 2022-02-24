import org.ru.pso.PSO;
import org.ru.pso.PSOConfig;
import org.ru.pso.Solution;
import org.ru.pso.objectives.WavyParabola;
import org.ru.pso.strategies.Placement;
import org.ru.pso.strategies.Topology;
import org.ru.vec.Vec2D;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PSOTest {
    // Have to use such a high delta because the fitness score and X/Y coordinates
    // have different tolerances. The fitness score is supposed to be REAL SMALL
    // but the positions can be out by quite a bit because of jitter
    public static final double LARGE_DELTA = 0.00001d;
    public static final double SMALL_DELTA = 0.00000001d;

    @Test
    void psoFindsMinimumInEasyCase() {
        PSOConfig<Vec2D> config = new PSOConfig<>(
                15,
                0.75,
                1.3,
                1.5,
                Topology.COMPLETE,
                Placement.RANDOM,
                15.0,
                new Vec2D(new double[]{100.0, 100.0})
        );
        PSO<Vec2D> pso = new PSO<>(config, WavyParabola::compute, Vec2D::new);
        Solution<Vec2D> foundMinimum = pso.run();
        double x = foundMinimum.solution().components()[0];
        double y = foundMinimum.solution().components()[1];

        assertEquals(0, x, LARGE_DELTA);
        assertEquals(0, y, LARGE_DELTA);
        assertEquals(0, foundMinimum.fitnessScore(), SMALL_DELTA);
    }
}
