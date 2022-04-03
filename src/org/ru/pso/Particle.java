package org.ru.pso;

import org.ru.vec.FixedVector;

public class Particle<V extends FixedVector> {
    private V pos;
    private V vel;
    private double personalBestFitness = Double.MAX_VALUE;
    private V personalBestLocation = null;

    Particle(V initPosition, V initVelocity) {
        this.pos = initPosition;
        this.vel = initVelocity;
    }

    public V getPos() {
        return this.pos;
    }

    public V getVel() {
        return this.vel;
    }

    public double getPersonalBestFitness() {
        return this.personalBestFitness;
    }

    /**
     * Sets the best fitness score of this particle
     * AND updates its internal "best location" to be
     * its current position
     * @param personalBestFitness the new fitness score
     */
    public void setPersonalBest(double personalBestFitness) {
        this.personalBestFitness = personalBestFitness;
        this.personalBestLocation = this.pos;
    }

    public V getPersonalBestLocation() {
        return this.personalBestLocation;
    }

    @SuppressWarnings("unchecked")
    public void move() {
        // We can't declare `pos` and `vel` as FixedVector because
        // the generic type V ensures that `pos` and `vel` use the
        // same IMPLEMENTATION of FixedVector. It's not sufficient
        // to only assert that they implement FixedVector, since we
        // care about the size of the vector.
        // Mutating the vector (make add `void`) is also an option,
        // but I prefer returning a new Vector.
        this.pos = (V) this.pos.add(this.vel);
    }

    @SuppressWarnings("unchecked")
    public void update(V nBest, double inertiaWeight, double pBestWeight, double nBestWeight) {
        V inertiaTerm = (V) this.vel.scale(inertiaWeight);

        V towardsPersonalBest = (V) this.personalBestLocation.subtract(this.pos).jitter();
        V towardsNeighborhoodBest = (V) nBest.subtract(this.pos).jitter();
        V personalBestTerm = (V) towardsPersonalBest.scale(pBestWeight);
        V neighborhoodBestTerm = (V) towardsNeighborhoodBest.scale(nBestWeight);

        this.vel = (V) inertiaTerm.add(personalBestTerm).add(neighborhoodBestTerm);
    }

    @Override
    public String toString() {
        return "Particle{" +
                "pos=" + pos +
                ", vel=" + vel +
                ", personalBestFitness=" + personalBestFitness +
                ", personalBestLocation=" + personalBestLocation +
                '}';
    }
}
