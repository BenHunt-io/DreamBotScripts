package Script;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by Ben on 8/28/2017.
 * Singleton
 */
public class Gui {

    private static Gui gui; // single instance
    private JFrame jFrame;
    private boolean mule;
    private boolean bot;

    private Gui(){

        AccountButtonListener acctButtonListener = new AccountButtonListener();

        jFrame = new JFrame();
        jFrame.setSize(200,100);
        JPanel jPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        JLabel jTitleLabel = new JLabel("Choose type of account");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        jPanel.add(jTitleLabel,c);

        JButton botButton = new JButton("Script.Bot");
        botButton.setActionCommand("Script.Bot");
        botButton.addActionListener(acctButtonListener);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;

        jPanel.add(botButton,c);

        JButton muleButton = new JButton("Mule");
        muleButton.setActionCommand("Mule");
        muleButton.addActionListener(acctButtonListener);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;


        jPanel.add(muleButton,c);

        jFrame.add(jPanel);
        jFrame.setVisible(true);

        // Need to not run any code in the script until the GUI is closed out
        synchronized (Gui.class){

            try {
                Gui.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    public static Gui getInstance(){
        if(gui == null){
            gui = new Gui();
        }
        return gui;
    }

    public boolean isMule(){ return !bot; } // bot flag reversed

    public boolean isBot() { return bot; } // is bot or not flag .. default false

    private class AccountButtonListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

            if(e.getActionCommand() == "Script.Bot"){
                bot = true;
                synchronized (Gui.class) {
                    jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING)); // close window
                    Gui.class.notify();
                }
            }
            else {
                bot = false;
                synchronized (Gui.class) {
                    jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING)); // close window
                    Gui.class.notify();
                }
            }
        }
    }

}
