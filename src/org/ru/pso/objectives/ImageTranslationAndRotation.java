package org.ru.pso.objectives;

import org.ru.vec.Vec3D;

// TODO: Just a stub
public class ImageTranslationAndRotation {

    public double compute(Vec3D v) {
        /*
        * Notes about rotation:
        *
        * First, how do we rotate around the origin? Wolfram Mathworld to the rescue:
        * R_theta =
        * [ cos(theta), -sin(theta)
        *   sin(theta), cos(theta)]
        * Multiply your vector using the matrix R_theta.
        *
        * Now, what about rotating around a point NOT at the origin?
        * Given a point that you want to rotate around, the "center of rotation"
        * 1. Subtract the center of rotation from each point you want to rotate
        * 2. Rotate as if you were rotating around the origin
        * 3. Add the center of rotation back to each point
        *
        * It's literally, "move it to the origin where we know how to rotate, then
        * put it back"
        *
        * see: https://www.youtube.com/watch?v=nu2MR1RoFsA
        * */
        return 0;
    }
}
