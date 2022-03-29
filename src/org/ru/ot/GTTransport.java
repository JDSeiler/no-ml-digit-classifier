package org.ru.ot;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Sourced from: https://github.com/nathaniellahn/CombinatorialOptimalTransport/blob/master/GabowTarjanJavaCode/GTTransport/src/optimaltransport/GTTransport.java
 * with permission from one the authors (Dr. Nathaniel Lahn - https://github.com/nathaniellahn) of the following paper:
 * https://arxiv.org/abs/1905.11830
 */

public class GTTransport {
    private int[][] CBA;
    private int[][] CAB;

    private int[] y;
    private int[][] capacityAB;
    private int[][] capacityBA;
    private boolean[] bFree;
    private boolean[] aFree;
    private int n;
    private int[] vertexVisited;

    private long timeTaken;
    private long timeTakenAugment;

    private int iterations;
    private int APLengths;

    //Number of calls to findAP
    private int numAPCalls = 0;

    // Preallocated array of size 2*n for findAP function.
    private ArrayList<Integer> path;

    private int[][] finalCapacity;

    //Note: Does not actually compute the solution. Rather, returns the
    //solution that was previously computed by the constructor.
    public int[][] getSolution() {
        return finalCapacity;
    }

    public double timeTakenInSeconds() {
        return timeTaken / 1000.;
    }

    public double timeTakenAugmentInSeconds() {
        return (double)timeTakenAugment / 1E9;
    }

    public int getIterations() {
        return iterations;
    }

    public int getNumAPCalls() {
        return this.numAPCalls;
    }

    public int APLengths() {
        return APLengths;
    }

    @Override
    public String toString() {
        return "GTTransport [timeTaken=" + timeTaken + ", iterations=" + iterations + ", APLengths=" + APLengths + "]";
    }


    //Solves instance of problem in constructor.
    //Solution is then recovered by calling the above "get solution" method.
    //Other relevant values can be recovered using their respective getter methods.
    //Solving problem in constructor guarantees any input instance is only
    //solved once.
    public GTTransport(int[][] C, int[] supplies, int[] demands, int n) {
        long startTime = System.currentTimeMillis();
        this.n = n;

        //Convention: CBA contains costs for edges directed from B to A.
        //CBA[i][j], 0 <= i,j < n, gives the cost of the edge from the
        //ith vertex of B to the jth vertex of A.
        //CAB[j][i] is the cost from the ith vertex of A to the jth vertex of B.
        //These costs are symmetric, but storing both is useful for efficiency reasons.
        //We follow this convention also for slacks, etc.
        //Note that this convention is the opposite of that used for the original Matlab implementation of this code.
        //The difference is because of how both languages store arrays (column vs. row order)
        // Transposing swaps things over the diagonal. So to get the same exact edge you
        // must switch the order of i and j when you index.
        CBA = transpose(C);
        CAB = C;

        //Whether all supplies or demands of a vertex have been satisfied.
        bFree = new boolean[n];
        aFree = new boolean[n];

        // Trivially, if the input supply or demand of a vertex is 0, we can say it's free.
        for(int i = 0; i < n; i++) {
            bFree[i] = supplies[i] != 0;
            aFree[i] = demands[i] != 0;
        }

        //Dual weights for B and A
        y = new int[2*n];

        //Remaining "unmatched" supply or demand.
        int[] deficiencyB = supplies;
        int[] deficiencyA = demands;
        capacityAB = new int[n][n];
        capacityBA = new int[n][n];

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                // The residual capacity of a forward edge: (b, a) is the minimum between the remaining supply at b
                // Or the unsatisfied demand at a.
                capacityBA[i][j] = Math.min(deficiencyB[i], deficiencyA[j]);
            }
        }

        //Preallocate maximum AP length of 2*n
        path = new ArrayList<Integer>(2*n);

        //Main iteration loop. Continue executing until all supply vertices have no more residual.
        while (any(bFree)) {
            iterations++;
            //Perform Dijkstra's algorithm
            //To identify the distances lv
            //in slack from the nearest free vertex of B.
            //First n vertices are type B.
            //Second set of n vertices are type A.
            int[] lv = new int[2*n];

            //The minimum distance to any free vertex of A.
            int distAF = Integer.MAX_VALUE;

            //For each vertex, store whether this vertex has been chosen
            //As minDist before. All such vertices have already been included
            //in the Dijkstra shortest path tree, and are final.
            boolean[] finalDist = new boolean[2*n];
            Arrays.fill(lv, Integer.MAX_VALUE);
            for (int i = 0; i < n; i++) {
                // This is part of the algorithm, we connect S to every free vertex of B
                // with a 0 cost edge. So the ith vertex of B trivially has l_v of 0.
                if (bFree[i]) {
                    lv[i] = 0;
                }
            }

            //Main Dijkstra loop. Each iteration adds one vertex to the shortest
            //path tree. Total time for all iterations is O(n^2).
            //Will break out early if free vertex of A is reached.
            for (int i = 0; i < 2*n; i++) {
                //Find the next vertex to add to the shortest path tree
                // This loop finds the vertex with the cheapest l_v so far. Dijkstra's
                // explores the cheapest path first so this vertex (represented by minIndex)
                // is the place we should explore from next.
                int minDist = Integer.MAX_VALUE;
                int minIndex = -1; //Placeholder
                for (int v = 0; v < 2*n; v++) {
                    if (lv[v] < minDist && !finalDist[v]) {
                        minDist = lv[v];
                        minIndex = v;
                    }
                }
                finalDist[minIndex] = true;

                //From the for loop condition, the early breaking out upon
                //reaching a free vertex, and the fact that the total supply is
                //less than the total demand, there should always be an augmenting
                //path found, meaning minDist < Inf. The asserts double check this if enabled.
                assert(minIndex != -1);
                assert(minDist < Integer.MAX_VALUE);

                // Recall that in lv the first n vertices are in B, the second n vertices are in A
                // So if minIndex is < n, the cheapest vertex must be in B. Similarly, if it's >= n
                // then the closest/"cheapest" vertex must be in A.
                if (minIndex < n) {
                    //Add a vertex of type B to the tree
                    //Update distances to all neighbors in A
                    for (int a = 0; a < n; a++) {
                        // If there is no capacity, that means one of the incident
                        // vertices has no supply/demand. In which case we don't
                        // bother exploring through thsi vertex because that will
                        // make the bottleneck capacity 0.
                        if (capacityBA[minIndex][a] > 0) {
                            int aIndex = a + n;
                            // Recall that the costs of the edges in the residual graph are actually the slacks.
                            // This is exactly the definition of slack for forward edges.
                            int newDist = lv[minIndex] + CBA[minIndex][a] + 1 - y[minIndex] - y[aIndex];

                            // Normal Dijkstra's stuff, if we found a cheaper path to this vertex, update it.
                            if (newDist < lv[aIndex]) {
                                lv[aIndex] = newDist;
                            }
                        }
                    }
                }
                else {
                    //Add a vertex of type A to the tree
                    //Update distances to all neighbors in B

                    // lv is a single array containing a and b, but other data structures are kept as two
                    // separate arrays, one for a and one for b. This line "normalizes" the lv index so that
                    // it can be used in an array which only contains vertices of a.
                    int a = minIndex - n;
                    // If we found a free vertex of A, we found an augmenting path and can end early.
                    if (aFree[a]) {
                        distAF = lv[minIndex];
                        break;
                    }

                    // Otherwise, do the same edge-cost update operation.
                    for (int b = 0; b < n; b++) {
                        if (capacityAB[a][b] > 0) {
                            int newDist = lv[minIndex] + y[a + n] + y[b] - CAB[a][b];
                            if (newDist < lv[b]) {
                                lv[b] = newDist;
                            }
                        }
                    }
                }
            }

            //Since there is a free vertex of B left, there is always some AP left
            assert(distAF < Integer.MAX_VALUE);

            assert(distAF > 0); //Ensures that a maximal set of vertex-disjoint shortest augmenting paths was found last phase.

            //Now, perform dual adjustments
            for (int i = 0; i < lv.length; i++) {
                // distAF - lv[i] === lt - lv on page 6
                int delta = Math.max(distAF - lv[i], 0);
                if (i < n) {
                    //i is a vertex of B; increase dual
                    // Same as y(v) + (lt - lv)
                    y[i] += delta;
                    assert(y[i] >= 0);
                }
                else {
                    // i is a vertex of A. Decrease dual.
                    // Same as y(v) - lt + lv
                    y[i] -= delta;
                    assert(y[i] <= 0);
                }
            }

            //Let the admissible graph be the set of 0 slack edges.
            //Now, we iteratively execute partial DFS searches from each free
            //vertex of B to find a maximal set of vertex-disjoint admissible
            //augmenting paths.

            //This is used by the DFS procedure to track the
            //largest explored neighbor index of every vertex.
            //Following our convention, 0:n-1 -> B and n:2n-1 -> A.
            //These values persist throughout all partial DFS searches this phase
            vertexVisited = new int[2*n];
            for (int i = 0; i < n; i++) {
                // TODO: This sentinel value kind of confuses me.
                vertexVisited[i] = n-1;
            }
            for (int i = 0; i < n; i++) {
                vertexVisited[i + n] = -1;
            }

            for (int vertex = 0; vertex < n; vertex++) {
                if (bFree[vertex]) {
                    //For each free vertex, repeatedly find admissible APs
                    //until no more can be found.

                    // Clearly if we've checked every last vertex in the array (the array only has 2*n elements)
                    // then we've DFS'd everything and can stop. But that assumes we visit vertices in strictly increasing
                    // order of index?
                    while (deficiencyB[vertex] > 0 && vertexVisited[vertex] < 2*n - 1) {
                        ArrayList<Integer> ap = null;

                        ap = findAP(vertex);

                        //Comment this out to make code faster
                        //Uncomment if time taken by augmentations needs to be measured.
                        //long startTimeAugment = System.nanoTime();
                        if (ap.size() == 0) {
                            //No AP was found. Move to next free vertex.
                            break;
                        }
                        APLengths += ap.size() - 1;

                        //Compute the bottleneck capacity beta: the maximum amount of flow that can be pushed
                        //without violating some vertex / edge capacity constraint.

                        // These are the first two constrains in computing r_p
                        int beta = Integer.min(deficiencyB[ap.get(0)], deficiencyA[ap.get(ap.size() - 1) - n]);
                        // This loop checks for the bottleneck edge set. Checks adjacent pairs of vertices (edges)
                        for (int j = 0; j < ap.size() - 1; j++) {
                            int u = ap.get(j);
                            int v = ap.get(j + 1);
                            if (u >= n) {
                                //edge is directed from A to B
                                beta = Integer.min(beta, capacityAB[u - n][v]);
                            }
                            else {
                                //edge is directed from B to A
                                beta = Integer.min(beta, capacityBA[u][v - n]);
                            }
                        }

                        //Augment along path by value beta
                        //First, update the edge capacities.
                        for (int j = 0; j < ap.size() - 1; j++) {
                            int u = ap.get(j);
                            int v = ap.get(j + 1);
                            if (u >= n) {
                                //edge is directed from A to B

                                // This was a detail I didn't fully grasp. Each undirected edge of the input has a forward
                                // and backward version in the residual graph. When we augment we remove flow from the forward
                                // edge and push it to the backward edge. Every reduction/increase in flow is accompanied by the
                                // inverse operation on the edge that goes between the same vertices, but in the opposite direction.

                                // Here the edge is from A -> B, so we "free up" some flow by redirecting it to the forward edge.
                                capacityAB[u - n][v] -= beta;
                                capacityBA[v][u - n] += beta;
                                if (capacityAB[u - n][v] > 0) {
                                    //Allow edge to be reused on future augmenting
                                    //paths this phase:
                                    vertexVisited[u] = v - 1;
                                }
                            }
                            else {
                                //edge is directed from B to A

                                // Here, we are taking some "unmatched" flow and committing it to this edge (a,b).
                                capacityBA[u][v - n] -= beta;
                                capacityAB[v - n][u] += beta;
                                if (capacityBA[u][v - n] > 0) {
                                    //Allow edge to be reused on future augmenting
                                    //paths this phase:
                                    vertexVisited[u] = v - 1;
                                }
                            }
                        }

                        /*
                        * We only have to update the "deficiencies" (residual capacity?) of the endpoints for the
                        * following reason. Imagine some vertex `v` in the middle of the augmenting path `P`.
                        * P contains one edge that goes to v, and one edge that leaves from v. Imagine we augment
                        * by `k` units. It's easy to get tangled up here so I'll keep this general.
                        *
                        * There is some edge from B -> A (u,v) in P, and another edge A -> B (v, w) in P. When we augment
                        * along (u,v), that's an edge from B -> A, so we shift flow from the forward edge to the backward
                        * edge. This means we satisfy K units of demand at v.
                        *
                        * But, when we then augment (v,w), we shift flow from the backward edge to the forward edge.
                        * This reintroduces those k units of demand. A fundamentally identical argument can be used
                        * for internal vertices that live in B, just swap "supply" for "demand".
                        *
                        * The only vertices that do not have their supply/demand symmetrically change like this are
                        * the vertices on the ends. At the end, we end up with less un-transported supply from the
                        * start vertex B. And less unsatisfied demand at the end vertex A.
                        *
                        * */

                        //Next, update the deficiencies of the endpoints of the path
                        int first = ap.get(0);
                        deficiencyB[first] -= beta;
                        if (deficiencyB[first] == 0) {
                            bFree[first] = false;
                        }

                        int last = ap.get(ap.size() - 1) - n;
                        deficiencyA[last] -= beta;
                        if (deficiencyA[last] == 0) {
                            aFree[last] = false;
                        }

                        //Comment this out to make code faster.
                        //Uncomment if time taken by augment procedure needs to be measured.
                        //timeTakenAugment += System.nanoTime() - startTimeAugment;
                    }
                }
            }//End of main for loop for DFS'


        }//End of while loop (phase loop)

        //Finally, convert capacities to a slightly different format used
        //by the rest of the program (i.e., the Mapping class).

        finalCapacity = new int[2*n][2*n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // The edges are "backwards" in the residual graph. An edge from A -> B represents
                // how much flow we've matched (are transporting) across the undirected edge a,b
                // An edge from B -> A represents how much flow we are not transporting (this breaks down a bit?
                // maybe I don't understand it?) along the undirected edge a,b
                // Point being, I have a hunch this is just about mapping the residual graph back to something
                // more user friendly.
                finalCapacity[i + n][j] = capacityAB[i][j];
                finalCapacity[i][j + n] = capacityBA[i][j];
            }
        }

        timeTaken = System.currentTimeMillis() - startTime;
    }

    //Execute a partial DFS from the given vertex.
    //If an admissible AP is found, return it.
    //else, return an empty list.
    public ArrayList<Integer> findAP(int start) {
        numAPCalls++;
        //Path is a preallocated array of size 2*n
        path.clear();
        path.add(start);
        while (!path.isEmpty()) {
            int end = path.get(path.size() - 1);
            // If the end is a free vertex of A
            if (end >= n && aFree[end - n]) {
                //Found an AP
                return path;
            }
            //Attempt to expand path
            boolean backtrack = true;
            int rangeStart = vertexVisited[end] + 1;
            // If the current vertex is in B (< n) we want to explore things in A.
            // Otherwise, we only want to explore things in B.
            int rangeEnd = end < n ? 2*n : n;

            // This makes sure we explore things in increasing index order, I think
            for (int i = rangeStart; i < rangeEnd; i++) {
                // This stops us from visiting this vertex again when we backtrack.
                vertexVisited[end] = i;
                if (end < n) {
                    //current vertex is type B
                    int a = i - n;
                    // If the edge is admissible and there is residual capacity (the edge is useful at all)
                    if (CBA[end][a] + 1 - y[end] - y[a + n] == 0 && capacityBA[end][a] > 0) {
                        backtrack = false;
                        //Add vertex to path
                        path.add(i);
                        break;
                    }
                }
                else {
                    //current vertex is type A
                    int a = end - n;
                    // Same deal, as B -> A :: Check the edge is admissible and that it has capacity
                    if (capacityAB[a][i] > 0 && y[a + n] + y[i] == CAB[a][i]) {
                        backtrack = false;
                        //Add vertex to path
                        path.add(i);
                        break;
                    }
                }
            }
            if (backtrack) {
                //No more options to explore from this vertex. Remove
                //last vertex from path.
                path.remove(path.size() - 1);
            }

        }
        return path;
    }

    //Returns true if there is any instance of true in the input array.
    //Otherwise, returns false.
    static boolean any(boolean[] free) {
        for (int i = 0; i < free.length; i++) {
            if (free[i]) {
                return true;
            }
        }
        return false;
    }

    public static int[][] transpose(int[][] matrix){
        int[][] t = new int[matrix.length][matrix[0].length];
        for(int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[0].length; j++) {
                t[j][i] = matrix[i][j];
            }
        }
        return t;
    }

    //Some methods for outputting matrix contents if desired.
    public static String arrToString(int[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                sb.append(arr[i][j]);
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String arrToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < arr.length; j++) {
            sb.append(arr[j]);
            sb.append(' ');
        }
        sb.append('\n');

        return sb.toString();
    }
}