package hk.Boid;

import hk.Boid.Boid;
import hk.sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BoidRunner extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    private final int mouseX = WIDTH / 2;
    private final int mouseY = HEIGHT / 2;

    private boolean addedBoid;

    private boolean intensityPlayed;
    private boolean milestonePlayed;

    private boolean clearGrid;

    public static final int WIDTH = 600;
    public static final int HEIGHT = 800;

    public static final int BOID_COUNT = 100;

    public static int totalInfected = 1, deathCount = 0, healthyCount = 0, criticalCount = 0,
            aliveCount, recoveryCount = 0, visiblyDead = 0, diagnosedCount = 0, paramedicCount = 0, paranoidCount = 0;

    private static JLabel infectedDisplay, deathDisplay, healthyDisplay, criticalDisplay, aliveDisplay, recoveredDisplay;

    private static final ArrayList<Boid> flock = new ArrayList<>();
    private static final ArrayList<Boid> boids = new ArrayList<>();

    public BoidRunner() {
        setLayout(null);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        addKeyListener(this);
        addMouseListener(this);

        createLabels();

        for (int i = 0; i < BOID_COUNT; i++)
            flock.add(new Boid());

        SoundManager music = new SoundManager("plague");

    }

    public void run() {
        while (true) {

            int toAdd = 0;
            totalInfected = 0; healthyCount = 0; recoveryCount = 0; visiblyDead = 0; diagnosedCount = 0; paramedicCount = 0; paranoidCount = 0;

            for (int i = 0; i < flock.size(); i++) {

                flock.get(i).edges();
                flock.get(i).flock(flock);
                flock.get(i).update();

                if (flock.get(i).isParamedic)
                    paramedicCount++;

                if (flock.get(i).healthStatus == Boid.HEALTHY)
                    healthyCount++;

                if (flock.get(i).healthStatus == Boid.INFECTED)
                    totalInfected++;

                if (flock.get(i).healthStatus == Boid.RECOVERED)
                    recoveryCount++;

                if (flock.get(i).healthStatus == Boid.DIAGNOSED)
                    diagnosedCount++;

                if (flock.get(i).healthStatus == Boid.PARANOID)
                    paramedicCount++;
                else
                    visiblyDead++;

                if(flock.get(i).dead && ((int)(Math.random()*(totalInfected*600+((totalInfected == 0)?1:0))) <= visiblyDead)) {
                    flock.remove(i);
                    i--;
                }

                if (flock.get(i).isParamedic && totalInfected <= flock.size() * 0.25 && (int) (Math.random() * 10000 * (flock.size() - totalInfected)) == 0) {
                    flock.remove(i);
                    i--;
                    new SoundManager("bell");
                }

                if (flock.get(i).isParamedic && Boid.LOCKED_ON) {
                    flock.get(i).sirenCount++;
                    if (flock.get(i).sirenCount % 3 == 0) {
                        flock.get(i).siren++;
                        switch (flock.get(i).siren) {
                            case 0 -> flock.get(i).paramedic = Color.BLUE;
                            case 1 -> flock.get(i).paramedic = Color.WHITE;
                            case 2 -> flock.get(i).paramedic = Color.RED;
                        }

                        flock.get(i).healthStatus = flock.get(i).paramedic;

                    }

                    if (flock.get(i).siren > 2)
                        flock.get(i).siren = -1;

                } else if (flock.get(i).isParamedic && flock.get(i).paramedic != Color.BLUE) {
                    flock.get(i).paramedic = Color.BLUE;
                    flock.get(i).healthStatus = flock.get(i).paramedic;
                }

                if ((int) (Math.random() * healthyCount * 2000 + ((healthyCount == 0) ? 1 : 0)) == 0 && !flock.get(i).hasDisease && diagnosedCount >= 3 && flock.get(i).healthStatus != Boid.PARANOID && paranoidCount <= 15) {
                    flock.get(i).healthStatus = Boid.PARANOID;
                    new SoundManager("paranoia");
                }

                if (recoveryCount >= 800 && flock.get(i).healthStatus == Boid.PARANOID && (int) (Math.random() * totalInfected * 200 + ((totalInfected == 0) ? 1 : 0)) == 0) {
                    flock.get(i).healthStatus = Boid.HEALTHY;
                    new SoundManager("paranoiaEnded");
                }

            }

            if (clearGrid) {
                for (int i = 0; i < boids.size(); i++) {
                    flock.remove(i);
                    i--;
                }
                clearGrid = false;
            }

            if (boids.size() != 0) {
                for (int i = 0; i < boids.size(); i++) {
                    flock.add(boids.get(i));
                    boids.remove(i);
                    i--;
                }
            }

            if (paranoidCount <= 2 && diagnosedCount != 0) {
                flock.add(new Boid(true));
                new SoundManager("ambulance");
            }

            if (!intensityPlayed && (flock.size() + 1) % 100 == 0)
                intensityPlayed = true;

            if (totalInfected == 0)
                flock.add(new Boid((int) (Math.random() * WIDTH), (int) (Math.random() * HEIGHT), true));

            if (totalInfected >= (int) (flock.size() * 0.8) && !intensityPlayed) {
                new SoundManager("intensity");
                intensityPlayed = !intensityPlayed;
            }

            if (deathCount >= 100) {
                if (!milestonePlayed && deathCount % 100 == 0) {
                    new SoundManager("deathmilestone");
                    milestonePlayed = true;
                } else if ((deathCount - 1) % 100 == 0)
                    milestonePlayed = false;
            }

            updateValues();

            int more = (int) (Math.random() * ((flock.size() >= 900) ? 1000 : 500));
            if (more == 0)
                flock.add(new Boid());

            if (addedBoid) {
                flock.add(new Boid(mouseX, mouseY, false));
                addedBoid = false;
            }

            this.repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.err.println("Thread was interrupted! " + e);
                e.printStackTrace();
            }

        }

    }

    private void createLabels() {
        //Healthy
        healthyDisplay = new JLabel("Healthy: "+ healthyCount);
        this.setLayout(new FlowLayout());
        this.add(healthyDisplay);
        healthyDisplay.setFont(new Font("Courier New", Font.PLAIN, 20));
        healthyDisplay.setForeground(Color.WHITE);
        healthyDisplay.setVisible(true);
        healthyDisplay.setLocation((int)WIDTH/2-400, 200);
        //Infected
        infectedDisplay = new JLabel(" Infected: "+ totalInfected);
        this.setLayout(new FlowLayout());
        this.add(infectedDisplay);
        infectedDisplay.setFont(new Font("Courier New", Font.PLAIN, 20));
        infectedDisplay.setForeground(Color.RED);
        infectedDisplay.setVisible(true);
        infectedDisplay.setLocation((int)WIDTH/2, 200);
        //Recovered
        recoveredDisplay = new JLabel(" Recovered: "+ criticalCount);
        this.setLayout(new FlowLayout());
        this.add(recoveredDisplay);
        recoveredDisplay.setFont(new Font("Courier New", Font.PLAIN, 20));
        recoveredDisplay.setForeground(Boid.RECOVERED);
        recoveredDisplay.setVisible(true);
        recoveredDisplay.setLocation((int)WIDTH/2+400, 200);
        //Death
        deathDisplay = new JLabel(" Dead: "+ deathCount);
        this.setLayout(new FlowLayout());
        this.add(deathDisplay);
        deathDisplay.setFont(new Font("Courier New", Font.PLAIN, 20));
        deathDisplay.setForeground(Boid.DEAD);
        deathDisplay.setVisible(true);
        deathDisplay.setLocation((int)WIDTH/2+200, 300);
    }

    @Override
    public void paintComponent(Graphics page) {
        super.paintComponent(page);
        Graphics2D g = (Graphics2D) page;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for(Boid boid: flock) {
            boid.draw(g);
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    static void toggleCounts(boolean setting) {
        healthyDisplay.setVisible(setting);
        infectedDisplay.setVisible(setting);
        recoveredDisplay.setVisible(setting);
        deathDisplay.setVisible(setting);
    }

    static void updateValues() {
        healthyDisplay.setText("Healthy: " + healthyCount);
        infectedDisplay.setText(" Infected: " + totalInfected);
        recoveredDisplay.setText(" Recovered: " + recoveryCount);
        deathDisplay.setText(" Dead: " + deathCount);
    }

    static void updateHealthy() {
        healthyCount = flock.size()-totalInfected-deathCount;
        healthyDisplay.setText("Healthy: " + healthyCount);
    }

    static void updateInfected() {
        totalInfected++;
        healthyCount--;
        infectedDisplay.setText(" Infected: " + totalInfected);
        new SoundManager("newpatient");
    }

    static void updateRecovered() {
        recoveryCount++;
        healthyCount++;
        totalInfected--;
        infectedDisplay.setText(" Infected: " + totalInfected);
        recoveredDisplay.setText(" Recovered: " + recoveryCount);
        new SoundManager("recovery");
    }

    static void updateDead() {
        deathCount++;
        totalInfected--;
        infectedDisplay.setText(" Infected: " + totalInfected);
        deathDisplay.setText(" Dead: " + deathCount);
        new SoundManager("death");
    }

    static void updateCritical() {
        criticalCount++;
        criticalDisplay.setText(" Critical: " + criticalCount);
    }

    static void updateAlive() {
        aliveCount = flock.size()-deathCount;
        aliveDisplay.setText(" Alive: " + aliveCount);
    }

    static void lostImmunity() {
        recoveryCount--;
        recoveredDisplay.setText(" Recovered: " + recoveryCount);
        new SoundManager("immunitylost");
    }

}
