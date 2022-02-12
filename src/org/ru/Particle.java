package org.ru;

public class Particle<V extends FixedVector> {
    private V pos;
    private V vel;
    private double personalBest;

    Particle(V initPosition, V initVelocity) {
        this.pos = initPosition;
        this.vel = initVelocity;
    }

    public V getPos() {
        return pos;
    }

    public V getVel() {
        return vel;
    }

    public void move() {
        // TODO: STUB
    }

    public void update(double nBest, double inertiaWeight, double pBestWeight, double nBestWeight) {
        // TODO: STUB
    }

    public String toString() {
        return String.format("Particle { pos: %s, vel: %s }", pos.toString(), vel.toString());
    }
}
