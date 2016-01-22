package com.bolaihui.weight.gui.main;

import com.bolaihui.weight.gui.form.MainForm;

import javax.swing.*;

/**
 * Created by fz on 2015/12/30.
 */
public class MainGUI {

    public static void main(String[] args) {

        Runnable doCreateAndShowGUI = () -> createAndShowGUI();
        SwingUtilities.invokeLater(doCreateAndShowGUI);
    }

    private static void createAndShowGUI(){
        JFrame frame = new JFrame();
        MainForm mainPanel = new MainForm();

        frame.setContentPane(mainPanel.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(80, 25);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("自贸达称重程序");
    }
}
