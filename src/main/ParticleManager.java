package main;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleManager {
    /*
    * ParticleManager holds list of particles
    * @Params:
    *   - max_particle_nr: max number of particles
    *   - reactivationThreshold = when number of active particles falls bellow, activate all deactivated particles
    *   - activeParticleList, deactivatedParticleList = particles with ttl <= 0 move from activeParticleList to
    *     deactivatedParticleList; reactivation moves then back
    *   - generationNr = count of re-activations
    * */
    public static int max_particle_nr = 1000;
    public int reactivationThreshold;
    // ArrayList and Vector cause error due to parallel list modification and cannot be used here
    CopyOnWriteArrayList<Particle> activeParticleList;
    CopyOnWriteArrayList <Particle> deactivatedParticleList;
    Random rand;

    public ParticleManager() {
        this.activeParticleList = new CopyOnWriteArrayList <>();
        this.deactivatedParticleList = new CopyOnWriteArrayList <>();
        this.rand = new Random();
        this.reactivationThreshold = rand.nextInt(25) + 70;
        for(int i=0; i<max_particle_nr; i++) {
            activeParticleList.add(new Particle());
        }
    }

    public void updateParticleList() {
        // update each particle (ttl and position)
        activeParticleList.forEach(particle -> particle.updateParticle(1));
        // move particles with ttl <= 0 to deactivated list and reset them to get them ready for use
        activeParticleList.forEach(particle -> {
            if(particle.getTtl() <= 0) {
                particle.respawnParticle();
                deactivatedParticleList.add(particle);
                activeParticleList.remove(particle);
            }
        });
        // when number of active particles fall bellow percentage threshold, activate them and reset threshold
        if((float)activeParticleList.size() / (float)max_particle_nr * 100.0f <= this.reactivationThreshold) {
            activeParticleList.addAll(deactivatedParticleList);
            deactivatedParticleList.clear();
            this.reactivationThreshold = rand.nextInt(25) + 70;
        }
    }

    public CopyOnWriteArrayList <Particle> getActiveParticleList() {
        return activeParticleList;
    }

}
