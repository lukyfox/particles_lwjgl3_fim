package main;

import transforms.Vec3D;
import java.util.Random;

public class Particle {
    Vec3D position;
    Vec3D velocity;
    Vec3D color;
    int duration;
    int ttl;
    Random rand;

    public Particle() {
        rand = new Random();
        this.position = new Vec3D();
        setVelocity();
        this.color = new Vec3D(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
        this.duration = rand.nextInt(99) + 1;
        this.ttl = this.duration;
    }

    public void updateParticle(float spendTime) {
        this.position = new Vec3D(
                this.position.getX() + this.velocity.getX(),
                this.position.getY() + this.velocity.getY(),
                this.position.getZ() + this.velocity.getZ()
        );
        this.ttl -= spendTime;
        if(this.position.getZ() < 0.0f) {
            this.ttl = 0;
        }
    }

    public void respawnParticle() {
        this.duration = rand.nextInt(999) + 1;
        this.ttl = this.duration;
        this.position = new Vec3D();
        setVelocity();
    }

    public Vec3D getPosition() {
        return position;
    }


    public Vec3D getVelocity() {
        return velocity;
    }

    public void setVelocity() {
        int sigX, sigY;
        if(rand.nextBoolean() == true) sigX = -1;
        else sigX = 1;
        if(rand.nextBoolean() == true) sigY = -1;
        else sigY = 1;
        this.velocity = new Vec3D(rand.nextFloat() * sigX, rand.nextFloat() * sigY, rand.nextFloat() + 0.1);
    }

    public void changeDirectionVelocityZ(float param) {
        this.velocity = new Vec3D(velocity.getX(), velocity.getY(), param*velocity.getZ());
    }


    public Vec3D getColor() {
        return color;
    }

    public int getDuration() {
        return duration;
    }

    public int getTtl() {
        return this.ttl;
    }
}
