package main;

import com.sun.xml.internal.ws.wsdl.writer.document.Part;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

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
    private int max_particle_nr;
    public int reactivationThreshold;
    public boolean blackHole;
    // ArrayList and Vector cause error due to parallel list modification and cannot be used here
    CopyOnWriteArrayList<Particle> activeParticleList;
    CopyOnWriteArrayList <Particle> deactivatedParticleList;
    Random rand;

    public ParticleManager(int max_particle_nr, boolean blackHole) {
        this.max_particle_nr = max_particle_nr;
        this.activeParticleList = new CopyOnWriteArrayList <>();
        this.deactivatedParticleList = new CopyOnWriteArrayList <>();
        this.rand = new Random();
        if(blackHole) {
            this.blackHole = true;
            this.reactivationThreshold = -1;
        }
        else {
            this.blackHole = false;
            this.reactivationThreshold = rand.nextInt(25) + 70;
        }

        for(int i=0; i<max_particle_nr; i++) {
            activeParticleList.add(new Particle(i+1, blackHole));
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

    public Particle getParticleById(int ID, boolean activeOnly) {
        if(this.blackHole) {
            return activeParticleList.get(0);
        }
        else {
            Particle item = new Particle(0, false);
            if (ID < 1 || ID > this.max_particle_nr) {
                System.out.println("ID out of range");
                return item;
            }
            try {
                return this.activeParticleList.get(ID-1);
            }
            catch (IndexOutOfBoundsException e) {
                if(!activeOnly) {
                    try {
                        return this.deactivatedParticleList.get(ID-1);
                    }
                    catch (IndexOutOfBoundsException e2) {
                        System.out.println("ID not found in all particles");
                        return item;
                    }
                }
                else {
                    System.out.println("ID not found in active particles");
                    return item;
                }
            }
        }
    }
}
