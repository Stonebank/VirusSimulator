package hk.boid;

import hk.math.Vector;
import hk.sound.SoundManager;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class Boid {

    public static final int FIELD_OF_VIEW = 120;
    public static final int SIZE = 3;

    public static final int MORTALITY_RATE = 14;

    public static final double ALIGNMENT_PERCEPTION_RADIUS = 50;
    public static final double COHESION_PERCEPTION_RADIUS = 100;
    public static final double SEPARATION_PERCEPTION_RADIUS = 100;

    public static final double MAX_SPEED = 2;
    public static final double MAX_FORCE = 0.2;

    public static final double FORCE_CHANGE_VALUE = 1;

    public static final Path2D SHAPE = new Path2D.Double();

    public static final Color RECOVERED = new Color(101, 194, 255);
    public static final Color DEAD = new Color(154, 74, 178);
    public static final Color HEALTHY = Color.WHITE;
    public static final Color INFECTED = Color.RED;
    public static final Color PARANOID = new Color(174, 243, 177);

    public static int TRAVEL_TIME;
    public static int PATIENT_BLINK;
    public static int PATIENT_BLINK_COUNT;

    public static boolean HAS_INFECTED;
    public static boolean LOCKED_ON;

    public static double SEPARATION_MAX_SPEED = MAX_SPEED;
    public static double SEPARATION_MAX_FORCE = MAX_FORCE;

    public static Color DIAGNOSED = new Color(134, 0, 0);;

    public static Boid PATIENT;

    public static SoundManager SOUND;

    static {
        SHAPE.moveTo(0, -SIZE * 2);
        SHAPE.lineTo(-SIZE, SIZE * 2);
        SHAPE.lineTo(SIZE, SIZE * 2);
        SHAPE.closePath();
    }

    public Color paramedic = Color.BLUE;
    public Color healthStatus = HEALTHY;

    public int siren;
    public int sirenCount;

    public boolean dead;
    public boolean hasDisease;
    public boolean isParamedic;

    private Vector position;
    private Vector velocity;
    private Vector acceleration;

    private boolean diagnosed;
    private boolean isImmune;

    private double immunity = Math.random() * 10 + 5;
    private double immunityCap = immunity;
    private double initialImmunity = immunity;
    private double immunityLife;

    private double lifeSpan = (Math.random() * 300 + 500) * 2;
    private double initialLifeSpan = lifeSpan;

    private double healTime = initialImmunity;

    private double deathAngle;

    private double patientDistance;

    public Boid() {

        if (!HAS_INFECTED) {
            this.healthStatus = INFECTED;
            this.hasDisease = true;
            this.lifeSpan = 2000;
            HAS_INFECTED = true;
        }

        this.position = new Vector(Math.random() * BoidRunner.WIDTH, Math.random() * BoidRunner.HEIGHT);

        double angle = Math.random() * 360;
        double radius = Math.random() + 2 + 2;

        this.velocity = new Vector(radius * Math.cos(angle), radius * Math.sin(angle));
        this.acceleration = new Vector(0, 0);

        if ((int) (Math.random() * 500) == 0 && !hasDisease) {
            this.isParamedic = true;
            this.healthStatus = paramedic;
            immunity = 2000;
        }

    }

    public Boid(int mouseX, int mouseY, boolean hasInfection) {

        if (hasInfection) {
            this.healthStatus = INFECTED;
            this.hasDisease = true;
            HAS_INFECTED = true;
        }

        this.position = new Vector(mouseX, mouseY);
        this.acceleration = new Vector(0, 0);

        if (BoidRunner.totalInfected == 1)
            this.lifeSpan = 12000;

    }

    public Boid(boolean isParamedic) {

        this.position = new Vector(BoidRunner.WIDTH, BoidRunner.HEIGHT);

        double angle = Math.random() * 360;
        double radius = Math.random() * 2 + 2;

        this.velocity = new Vector(radius * Math.cos(angle), radius * Math.sin(angle));
        this.acceleration = new Vector(0, 0);

        if (isParamedic) {
            this.isParamedic = true;
            this.healthStatus = paramedic;
            this.immunity = 500;
        }

    }

    public Vector align(ArrayList<Boid> flock) {

        var perceptionRadius = (int) ALIGNMENT_PERCEPTION_RADIUS;
        var total = 0;

        var steering = new Vector(0, 0);

        if (this.hasDisease && !this.dead && !this.isImmune) {

            lifeSpan--;
            if (lifeSpan <= 0) {
                if ((int) (Math.random() * 100) < MORTALITY_RATE) {
                    this.dead = true;
                    this.healthStatus = DEAD;
                    BoidRunner.updateDead();
                } else {
                    this.hasDisease = false;
                    this.isImmune = true;

                    if (this.diagnosed) {
                        PATIENT = null;
                        LOCKED_ON = false;
                    }

                    new SoundManager("recovery");
                    this.healthStatus = RECOVERED;
                    this.immunity = this.immunityCap * (Math.random() * 50 + 100);
                    this.immunityCap = immunity;
                    this.immunityLife = initialLifeSpan * (6 * (Math.random() * 0.8 + 0.5));

                    if (this.diagnosed) {
                        this.diagnosed = false;
                        if (this == PATIENT) {
                            Boid.TRAVEL_TIME = 0;
                            SOUND.stop();
                            SOUND = null;
                        }
                    }

                }
            }

        } else if (this.isImmune) {

            this.immunityLife--;
            if (this.immunityLife < 0) {
                this.isImmune = false;
                this.healthStatus = HEALTHY;
                this.immunity = initialImmunity * (Math.random() * 0.8 + 0.4);
                this.immunityCap = immunity;
                this.immunityLife = initialLifeSpan * (6 * (Math.random() * 0.8 + 0.5));
                this.lifeSpan = initialLifeSpan;
                new SoundManager("immunitylost");
            }

        }

        if (!this.isParamedic || (this.isParamedic && !LOCKED_ON)) {

            for (Boid boid : flock) {
                if (this.isParamedic && boid.diagnosed) {

                    PATIENT = boid;
                    LOCKED_ON = true;

                    if (SOUND == null) {

                        switch ((int) (Math.random() * 3)) {
                            case 0 -> SOUND = new SoundManager("ambulance");
                            case 1 -> SOUND = new SoundManager("ambulance2");
                            case 2 -> SOUND = new SoundManager("ambulance3");
                        }

                    }

                }

                double distance = distance(this.position.getX(), this.position.getY(), boid.position.getX(), boid.position.getY());
                if (boid != this && distance < perceptionRadius) {
                    if (!(this.diagnosed && boid.isParamedic)) {
                        steering.add(boid.velocity);
                        total++;
                    }

                    if (this.hasDisease && !boid.hasDisease && (!this.isImmune || boid.dead)) {

                        if (boid.immunity <= 0) {

                            if (boid.healthStatus == PARANOID)
                                new SoundManager("paranoiaEnded");

                            boid.healthStatus = INFECTED;
                            boid.hasDisease = true;
                            new SoundManager("newpatient");

                            if (this.isParamedic) {
                                this.isParamedic = false;
                                new SoundManager("bell");
                            }

                        } else {

                            if ((int) (Math.random() * 40000) == 0 && !this.diagnosed && !this.dead) {
                                this.healthStatus = DIAGNOSED;
                                this.diagnosed = true;
                                new SoundManager("diagnosis");
                            }

                            boid.immunity -= (1 / distance) * ((BoidRunner.totalInfected > 35) ? 1 : ((BoidRunner.totalInfected > 11) ? 2.5 : ((BoidRunner.totalInfected < 5) ? (BoidRunner.totalInfected < 2 ? 5 : 4) : 3.5)));


                        }

                    } else if (!this.hasDisease && !boid.hasDisease && boid.immunity < boid.immunityCap && !boid.isImmune) {
                        boid.immunity += (Math.random() * 5 + 1) / ((BoidRunner.totalInfected > 35) ? 10000 : 100);
                        if (boid.immunity > boid.immunityCap)
                            boid.immunity = boid.immunityCap;
                    }

                    if (boid.isParamedic && this.diagnosed && distance < 5) {

                        healTime--;
                        if (healTime <= 0) {
                            this.hasDisease = false;
                            this.isImmune = true;
                            this.diagnosed = false;

                            if (SOUND != null)
                                SOUND.stop();
                            SOUND = null;

                            new SoundManager("treatment");

                            this.healthStatus = RECOVERED;
                            this.immunity = immunityCap * (Math.random() * 50 + 100);
                            this.immunityCap = immunity;
                            this.immunityLife = initialLifeSpan * (6 * (Math.random() * 0.8 + 0.5));

                            LOCKED_ON = false;
                            PATIENT = null;
                            Boid.TRAVEL_TIME = 0;

                        }

                    }

                }

            }

            if (total > 0) {
                steering.divide(total);
                steering.setMagnitude(MAX_SPEED);
                steering.subtract(this.velocity);
                steering.limit(MAX_FORCE);
            }

        }

        return steering;

    }

    public Vector cohesion(ArrayList<Boid> flock) {

        var perceptionRadius = (int) COHESION_PERCEPTION_RADIUS;
        var total = 0;

        var steering = new Vector(0, 0);

        if (!this.isParamedic || (this.isParamedic && !LOCKED_ON)) {

            for (Boid boid : flock) {

                double distance = distance(this.position.getX(), this.position.getY(), boid.position.getX(), boid.position.getY());

                if (boid != this && distance < perceptionRadius) {
                    steering.add(boid.position);
                    total++;
                }

            }

        }

        if ((total > 0 || (this.isParamedic && LOCKED_ON && PATIENT.velocity.movement() != 0))) {

            if (total > 0)
                steering.divide(total);
            else {
                patientDistance = distance(this.position.getX(), this.position.getY(),  PATIENT.position.getX(), PATIENT.position.getY());
                steering.add(PATIENT.position);
            }
            steering.subtract(this.position);
            steering.setMagnitude(MAX_SPEED);
            steering.subtract(this.velocity);
            steering.limit(MAX_FORCE * ((this.isParamedic && LOCKED_ON) ? 3 : 1));
        }

        return steering;

    }

    public Vector separation(ArrayList<Boid> flock) {

        var perceptionRadius = (int) SEPARATION_PERCEPTION_RADIUS;
        var total = 0;

        var paramedicPresent = false;

        var steering = new Vector(0, 0);

        for (Boid boid : flock) {

            double distance = distance(this.position.getX(), this.position.getY(), boid.position.getX(), boid.position.getY());

            if (boid != this && distance < perceptionRadius && !(this.diagnosed && boid.isParamedic)) {

                var difference = new Vector(this.position.getX(), this.position.getY());
                difference.subtract(boid.position);

                if (distance == 0.0)
                    distance += 0.001;

                difference.divide(distance * distance);

                if ((boid.dead || (boid.diagnosed && !this.isParamedic) || this.healthStatus == PARANOID || (boid.isParamedic && LOCKED_ON) && !this.isParamedic))
                    difference.multiply(Math.random() * 5 + ((boid.isParamedic && LOCKED_ON) ? 80 : 20));

                if (this.isParamedic && boid.isParamedic && LOCKED_ON && distance(this.position.getX(), this.position.getY(), PATIENT.position.getX(), boid.position.getY()) > 150 && distance < 5)
                    difference.multiply(15);

                if (boid.isParamedic && LOCKED_ON && !this.isParamedic)
                    paramedicPresent = true;

                steering.add(difference);
                total++;

            }

        }

        if (total > 0) {
            steering.divide(total);
            steering.setMagnitude(((total > 40 || paramedicPresent ? SEPARATION_MAX_SPEED * ((paramedicPresent) ? 6 : 2) : ((this.healthStatus == PARANOID) ? SEPARATION_MAX_SPEED * 5 : SEPARATION_MAX_SPEED))));
            steering.subtract(this.velocity);
            steering.limit(((total > 40 || paramedicPresent ? SEPARATION_MAX_SPEED * ((paramedicPresent) ? 6 : 2) : ((this.healthStatus == PARANOID) ? SEPARATION_MAX_SPEED * 5 : SEPARATION_MAX_SPEED))));
        }

        return steering;

    }

    public void flock(ArrayList<Boid> flock) {

        var healInfected = false;

        if (this.isParamedic && LOCKED_ON)
            healInfected = true;

        this.acceleration.set(0, 0);

        var alignment = this.align(flock);
        var cohesion = this.cohesion(flock);
        var separation = this.separation(flock);

        if (!healInfected)
            this.acceleration.add(alignment);

        this.acceleration.add(separation);
        this.acceleration.add(cohesion);

    }

    public void update() {

        if (!this.dead) {

            if (this.isParamedic && LOCKED_ON && patientDistance >= 10) {

                if ((int) (Math.random() * BoidRunner.paramedicCount) == 0)
                    Boid.TRAVEL_TIME++;

                var acuteVelocity = this.velocity.setMagnitude(this.velocity.getMagnitude() * 2 + ((Boid.TRAVEL_TIME > 20) ? Boid.TRAVEL_TIME / 200 : 1));
                this.position.add(acuteVelocity);
            } else
                this.position.add(this.velocity);

        }

        this.velocity.add(this.acceleration);
        this.velocity.limit(MAX_SPEED);

        if (this.dead && deathAngle == 0)
            deathAngle = this.velocity.dir() + Math.PI / 2;

        if (PATIENT == this && LOCKED_ON) {

            PATIENT_BLINK_COUNT++;

            if (PATIENT_BLINK_COUNT % 4 == 0) {

                PATIENT_BLINK++;
                switch (PATIENT_BLINK) {

                    case 0 -> DIAGNOSED = new Color(252, 52, 52);
                    case 1 -> DIAGNOSED = new Color(134, 0, 0);

                }

                PATIENT.healthStatus = DIAGNOSED;

                if (PATIENT_BLINK > 1)
                    PATIENT_BLINK = -1;

            }

        }

        if (this.isParamedic && LOCKED_ON && PATIENT.dead) {
            PATIENT.diagnosed = false;
            SOUND.stop();
            SOUND = null;
            LOCKED_ON = false;
            PATIENT = null;
            Boid.TRAVEL_TIME = 0;
        }

    }

    public void edges() {

        if (this.position.getX() > BoidRunner.WIDTH)
            this.position.setX(0);
        if (this.position.getX() < 0)
            this.position.setX(BoidRunner.WIDTH);

        if (this.position.getY() > BoidRunner.HEIGHT)
            this.position.setY(0);
        if (this.position.getY() < 0)
            this.position.setY(BoidRunner.HEIGHT);

    }

    public void draw(Graphics2D g) {

        var save = g.getTransform();

        g.translate(this.position.getX(), this.position.getY());
        g.rotate(this.dead ? deathAngle : this.velocity.dir() + Math.PI / 2);
        g.setColor(healthStatus);
        g.fill(SHAPE);
        g.draw(SHAPE);
        g.setTransform(save);

    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static void pause() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.err.println("ERROR! Thread was interrupted: " + e);
            e.printStackTrace();
        }
    }

    public static void decrementSeparationMaxForce() {
        Boid.SEPARATION_MAX_FORCE -= FORCE_CHANGE_VALUE;
    }

    public static void incrementSeparationMaxForce() {
        Boid.SEPARATION_MAX_FORCE += FORCE_CHANGE_VALUE;
    }

}
