package hk;

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

    public static final Color RECOVERED = new Color(101,194,255), DEAD = new Color(154, 74, 178),
            HEALTHY = Color.WHITE, INFECTED = Color.RED,  PARANOID = new Color(174,243,177);

    public static int TRAVEL_TIME;
    public static int PATIENT_BLINK;
    public static int PATIENT_BLINK_COUNT;

    public static boolean HAS_INFECTED;
    public static boolean LOCKED_ON;

    public static double SEPARATION_MAX_SPEED = MAX_SPEED;
    public static double SEPARATION_MAX_FORCE = MAX_FORCE;

    public static Boid PATIENT;

    public static SoundManager SOUND;

    static {
        SHAPE.moveTo(0, -SIZE * 2);
        SHAPE.lineTo(-SIZE, SIZE * 2);
        SHAPE.lineTo(SIZE, SIZE * 2);
        SHAPE.closePath();
    }

    private Vector position;
    private Vector velocity;
    private Vector acceleration;

    private boolean diagnosed;
    private boolean hasDisease;
    private boolean isImmune;
    private boolean isParamedic;

    private boolean dead;

    private double immunity = Math.random() * 10 + 5;
    private double immunityCap = immunity;
    private double initialImmunity = immunity;
    private double immunityLife;

    private double lifeSpan = (Math.random() * 300 + 500) * 2;
    private double initialLifeSpan = lifeSpan;

    private double healTime = initialImmunity;

    private int siren;
    private int sirenCount;

    private double deathAngle;

    private Color healthStatus = HEALTHY;
    private Color paramedic = Color.BLUE;
    private Color diagnosed_color = new Color(134, 0, 0);

    public Boid() {

        if (!HAS_INFECTED) {
            healthStatus = INFECTED;
            hasDisease = true;
            HAS_INFECTED = true;
            lifeSpan = 2000;
        }

        this.position = new Vector((double) (Math.random() * BoidRunner.WIDTH), (double) (Math.random() * BoidRunner.HEIGHT));

        double angle = Math.random() * 360;
        double radius = Math.random() * 2 + 2;

        this.velocity = new Vector((radius * Math.cos(angle)), (radius * Math.sin(angle)));
        this.acceleration = new Vector(0, 0);

        if ((int) (Math.random() * 500) == 0 && !hasDisease) {
            isParamedic = true;
            healthStatus = paramedic;
            immunity = 2000;
        }

    }

    public Boid(int mouseX, int mouseY, boolean addedInfected) {

        if (addedInfected) {
            healthStatus = INFECTED;
            hasDisease = true;
            HAS_INFECTED = true;
        }

        this.position = new Vector(mouseX, mouseY);

        double angle = Math.random() * 360;
        double radius = Math.random() * 2 + 2;

        this.velocity = new Vector((radius * Math.cos(angle)), (radius * Math.sin(angle)));
        this.acceleration = new Vector(0, 0);

        if (BoidRunner.totalInfected == 1)
            lifeSpan = 12000;

    }

    public Boid(boolean addedParamedic) {

        this.position = new Vector((int) BoidRunner.WIDTH, (int) BoidRunner.HEIGHT);

        double angle = Math.random() * 360;
        double radius = Math.random() * 2 + 2;

        this.velocity = new Vector((radius * Math.cos(angle)), (radius * Math.sin(angle)));
        this.acceleration = new Vector(0, 0);

        if (addedParamedic) {
            isParamedic = true;
            healthStatus = paramedic;
            immunity = 500;
        }

    }

    public Vector align(ArrayList<Boid> flock) {

        int perceptionRadius = (int) ALIGNMENT_PERCEPTION_RADIUS;
        int total = 0;

        Vector steering = new Vector(0, 0);

        if (hasDisease && !dead && !isImmune) {

            lifeSpan--;
            if (lifeSpan <= 0) {
                if ((int) (Math.random() * 100) < MORTALITY_RATE) {
                    this.dead = true;
                    BoidRunner.updateDead();
                    healthStatus = DEAD;
                } else {
                    hasDisease = false;
                    isImmune = true;
                    if (diagnosed) {
                        PATIENT = null;
                        LOCKED_ON = false;
                    }
                }

                new SoundManager("recovery.wav");

                healthStatus = RECOVERED;
                immunity = immunityCap * (Math.random() * 50 + 100);
                immunityCap = immunity;
                immunityLife = initialLifeSpan * (6 * (Math.random() * 0.8 + 0.5));

                if (diagnosed) {
                    diagnosed = false;
                    if (this == PATIENT) {
                        Boid.TRAVEL_TIME = 0;
                        SOUND.stop();
                        SOUND = null;
                    }
                }

            }

        } else if (isImmune) {
            immunityLife--;
            if (immunityLife < 0) {
                isImmune = false;
                healthStatus = HEALTHY;
                immunity = initialImmunity * (Math.random() * 0.8 + 0.4);
                immunityCap = immunity;
                immunityLife = initialLifeSpan * (6 * (Math.random() * 0.8 + 0.5));
                lifeSpan = initialLifeSpan;
                new SoundManager("immunitylost.wav");
            }
        }

        if (!isParamedic || (isParamedic && !LOCKED_ON)) {
            for (int i = 0; i < flock.size(); i++) {
                if (isParamedic && flock.get(i).diagnosed) {
                    PATIENT = flock.get(i);
                    LOCKED_ON = true;
                    if (SOUND == null) {
                        switch ((int) (Math.random() * 3)) {
                            case 0 -> SOUND = new SoundManager("ambulance.wav");
                            case 1 -> SOUND = new SoundManager("ambulance2.wav");
                            case 2 -> SOUND = new SoundManager("ambulance3.wav");
                        }
                    }
                }
            }

        }

    }

}
