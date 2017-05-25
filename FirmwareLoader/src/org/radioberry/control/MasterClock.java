package org.radioberry.control;


import org.radioberry.clock.SI570;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by pa3gsb on 7-5-2017.
 */
public class MasterClock {
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JButton downButton;
    private JButton upButton;
    private JComboBox step;
    private JLabel freq;


    public MasterClock() {

        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer fstep = Integer.valueOf((String) step.getItemAt(step.getSelectedIndex()));
                String newFreq = new Integer(Integer.valueOf(freq.getText()) - fstep).toString();
                freq.setText(newFreq);

                try {
                    new SI570().setFrequency(Integer.valueOf(newFreq));
                } catch (Exception ex){ex.getStackTrace();}
            }
        });
        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer fstep = Integer.valueOf((String) step.getItemAt(step.getSelectedIndex()));
                String newFreq = new Integer(Integer.valueOf(freq.getText()) + fstep).toString();
                freq.setText(newFreq);

                try {
                    new SI570().setFrequency(Integer.valueOf(newFreq));
                } catch (Exception ex){ex.getStackTrace();}
            }
        });
    }

    public static void main(String... args) {
        JFrame frame = new JFrame("Clock");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height * 1 / 4;
        int width = screenSize.width * 1 / 4;
        frame.setPreferredSize(new Dimension(width, height));

        MasterClock mc = new MasterClock();
        frame.setContentPane(mc.mainPanel);

        frame.setDefaultCloseOperation(JInternalFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
