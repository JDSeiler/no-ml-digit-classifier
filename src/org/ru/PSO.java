package org.ru;

import java.util.List;
import java.util.function.Function;

public class PSO<V extends FixedVector> {
    private List<Particle<V>> swarm;
    private Function<V, Double> cost;

    PSO(int swarmSize, Function<V, Double> objectiveFunction) {
        // init the swarm - this is not so trivial.
        this.cost = objectiveFunction;
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
