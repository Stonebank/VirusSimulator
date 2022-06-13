package hk;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BoidRunner extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;

    public static final int BOID_COUNT = 1200;

    public static int totalInfected = 1, deathCount = 0, healthyCount = 0, criticalCount = 0,
            aliveCount, recoveryCount = 0, visiblyDead = 0, diagnosedCount = 0, paramedicCount = 0, paranoidCount = 0;

    private static final ArrayList<Boid> flock = new ArrayList<>();

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

}
