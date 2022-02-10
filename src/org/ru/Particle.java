package org.ru;

public class Particle<V extends FixedVector> {
    private V pos;
    private V vel;
    private double personalBest;

    Particle(V initPosition, V initVelocity) {
        this.pos = initPosition;
        this.vel = initVelocity;
    }

    public void update(double inertiaWeight, double pBestWeight, double nBestWeight) {
        // TODO
    }
}
