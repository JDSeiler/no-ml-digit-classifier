package org.ru.pso.strategies;

public enum Topology {
//    RING,
    COMPLETE,
//    RANDOM
}

/*
* Could also consider a "von Neumann" topology, which applies
* for swarms of size 9 or larger. You array the nodes in a grid:
*
* 1 2 3
* 4 5 6
* 7 8 9
*
* And then connect them in the following way:
*
*   |   |   |
* - 1 - 2 - 3 -
*   |   |   |
* - 4 - 5 - 6 -
*   |   |   |
* - 7 - 8 - 9 -
*   |   |   |
*
* Any graph edges that go off the edge of the picture "wrap around"
* PacMan style and connect with the vertex on the opposite side.
*
* As an example, the vertex 1 is adjacent to:
* 2 & 4 (directly)
* 3 & 7 (by wrapping around)
*
* The most common topologies (from what I can find) are Ring, Complete, and VN. But VN
* is kinda complicated to implement.
* */
