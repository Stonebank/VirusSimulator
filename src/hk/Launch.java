package hk;

import hk.Boid.BoidRunner;

import javax.swing.*;
import java.awt.*;

public class Launch {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Virus simulation developed by Hassan K");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(BoidRunner.WIDTH, BoidRunner.HEIGHT));
        frame.setResizable(false);

        BoidRunner runner = new BoidRunner();
        frame.add(runner);
        frame.pack();
        frame.setVisible(true);

        runner.run();

    }

}
