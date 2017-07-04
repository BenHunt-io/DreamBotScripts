package Thebot;

import javafx.scene.control.*;
import oracle.jrockit.jfr.JFR;
import org.dreambot.api.Client;
import org.dreambot.api.input.event.impl.mouse.impl.click.ClickEvent;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.Button;
import java.awt.TextField;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Created by Ben on 6/25/2017.
 */

@ScriptManifest(category = Category.WOODCUTTING, name = "Mulitbox", author = "Computor", version = 2.0)
public class MyBot extends AbstractScript implements ActionListener {

    private List<Player> playerList;
    Player leadPlayer;
    Player localPlayer;

    CombatListener combatListener;

    PreferenceChangeListener pcl; // listens for changes made in Preferences

    private JButton rangeButton; // Click to wield specified range weapon
    private TextField setRangeField; // Field that specifies the range weapon
    private Button submitRangeButton;

    private JButton meleeButton;
    private Button submitMeleeButton;
    private TextField setMeleeField;

    private JButton mageButton;
    private Button submitMageButton;
    private TextField setMageField;

    private JButton foodButton;
    private Button submitFoodButton;
    private TextField setFoodField;

    private Thread attackListenerThread;
    private Preferences preferences;

    private String followerAction = new String();

    Frame theFrame;


    ArrayList<NPC> goblinList;

    private Character monster;

    Client client;
    private JFrame myJFrame;
    private JFrame startJFrame;

    @Override
    public void onStart() {

        preferences = Preferences.userNodeForPackage(MyBot.class);
        combatListener = new CombatListener();
        MultiBoxGui multiBoxGui = new MultiBoxGui(preferences);


        log(" " + preferences.getBoolean("JFrameInstanceOpen", false));

        preferences.putBoolean("JFrameInstanceOpen", false);
        if(!preferences.getBoolean("JFrameInstanceOpen", false)) {
            log("test");
            multiBoxGui.initStartFrame(getClient());
            //initStartFrame();

            synchronized (MyBot.class) {
                try {
                    MyBot.class.wait();
                    multiBoxGui.displayMultiBoxGui();
                    initFrame();
                    initLayout();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        localPlayer = getLocalPlayer();
        goblinList = new ArrayList<>();


        // Get our bots as player objects.
        Players players = getPlayers();
        playerList = players.all();
        for(int i = 0; i < playerList.size(); i++){
            log(playerList.get(i).getName());
            if(playerList.get(i).getName().toString().equals(preferences.get("LeadUser", "NoUser"))){
                log("test");
                leadPlayer = playerList.get(i);
            }
        }




        super.onStart();
    }

    @Override
    public int onLoop() {
//        myJFrame.setBounds(100  , 700,100,100);
//        myJFrame.setLocationRelativeTo(theFrame);
//        myJFrame.setAlwaysOnTop(true);

        //log("loop " + Thread.currentThread());
        Object lockObject = new Object();

        if(attackListenerThread == null) {
            attackListenerThread = new Thread(new AttackListener());
            attackListenerThread.start();
            log("Started a new thread");
        }


        if(leadPlayer!= null) { // Player has to be there or program will just essentially end.
            log(leadPlayer.getName());
            leadPlayer.interact("Follow");

            while (!leadPlayer.isInCombat()) {
                try {
                    log("leadPlayer is not in combat");
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return 0;
                }
            }




            goblinList = (ArrayList<NPC>) getNpcs().all();
            NPC currentTarget = null;
            if (leadPlayer.isInCombat()) {
                for (int i = 0; i < goblinList.size(); i++) {
                    if (leadPlayer.isInteracting(goblinList.get(i))) {
                        goblinList.get(i).interact("Attack");
                        currentTarget = goblinList.get(i);
                    }
                }
            }
            while (leadPlayer.isInCombat()) {
                try {
                    if(!localPlayer.isInteracting(currentTarget)) {
                        currentTarget.interact("Attack");
                        Thread.sleep(500);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }







        return 0;


    }

    @Override
    public void onExit() {
        //attackListenerThread.interrupt(); // end the thread?=
        attackListenerThread.interrupt();
        preferences.removePreferenceChangeListener(pcl); // Removes the listener
        log("test onEXIT() called" );
        super.onExit();
    }

    @Override
    public void onPaint(Graphics graphics) {
//        graphics.drawRect(100,100,200,200);
//        graphics.setColor(new Color(50,50,50,100));
//        log(" test" +  graphics.getClipBounds().toString());
        super.onPaint(graphics);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log("Was clicekd  " + Thread.currentThread());

        Inventory followerInventory = getInventory();



        log(e.getActionCommand());
        switch(e.getActionCommand()) {

            // Equipping different weapons to attack with / using items. Like eating food
            case Constants.RANGE_ATTACK:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.RANGE_ATTACK);

                break;
            case Constants.MELEE_ATTACK:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.MELEE_ATTACK);
                break;
            case Constants.EAT_FOOD:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.EAT_FOOD);
                break;




                // Changing Text of different combat buttons
            case Constants.RANGE_WEAPON:
                log("Setting Range Weapon TExt");
                rangeButton.setText("Equip: " + setRangeField.getText().toString());
                preferences.put(Constants.RANGE_WEAPON, setRangeField.getText().toString());
                break;
            case Constants.MELEE_WEAPON:
                log("Setting Melee Weapon Text");
                meleeButton.setText("Equip: " + setMeleeField.getText().toString());
                preferences.put(Constants.MELEE_WEAPON, setMeleeField.getText().toString());
                break;
            case Constants.MAGE_WEAPON:
                log("Setting Mage Weapon Text");
                mageButton.setText("Equip: " + setMageField.getText().toString());

                break;
            case Constants.FOOD_ITEM:
                log( "Setting Food Button Text");
                foodButton.setText("Equip " + setFoodField.getText().toString());
                preferences.put(Constants.FOOD_ITEM, setFoodField.getText().toString());
                break;

        }

    }



    class MyJFrame extends JFrame{

        @Override
        public void paint(Graphics g) {

            g.setColor(Color.white);
            g.drawString("Attack", 100, 100);
            super.paint(g);
        }
    }




    public void initLayout(){

        BorderLayout borderLayout =  new BorderLayout();


        Container pane = myJFrame.getContentPane();
        pane.setLayout(new GridBagLayout());
        pane.setBackground(new Color(69, 60, 51, 255));


        GridBagConstraints c = new GridBagConstraints(); // Constrainst for Gridbaglayout
        Insets leftRightInsets = new Insets(0,5,0,5); // basically margins



        URL url = getClass().getResource("/Drawables/range_icon_button.png");
        ImageIcon imageIcon = new ImageIcon(url);
        // Initialize Buttons, add actionCommand to differentiate buttons when clicked
        if(imageIcon== null) log("image is null");
        else log(imageIcon.getIconHeight() + " and " + imageIcon.getIconWidth() + "");
        rangeButton = new JButton(imageIcon);
        rangeButton.setText("Use Range");
        rangeButton.setFont(Font.getFont(Font.SANS_SERIF));
        rangeButton.setBackground(new Color(0,0,0, 255));
        rangeButton.setActionCommand(Constants.RANGE_ATTACK);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5; // relative to the highest weight x in that column
        c.weighty = 0.5; // Same for weighty, but for rows.
       // c.insets = leftRightInsets;



        pane.add(rangeButton, c);


        setRangeField = new TextField("Range Weapon");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        pane.add(setRangeField, c);

        // Change the weapon to switch to once the rangeButton is clicked
        // Submit the user entered field to make the change
        Button submitRangeButton = new Button("Submit");
        submitRangeButton.setActionCommand(Constants.RANGE_WEAPON);
        submitRangeButton.addActionListener(this);

        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;

        pane.add(submitRangeButton, c);




        url = getClass().getResource("/Drawables/thesword.png");
        imageIcon = new ImageIcon(url);
        meleeButton = new JButton(imageIcon);
        meleeButton.setText("Use Melee");
        meleeButton.setBackground(new Color(0,0,0, 255));
        meleeButton.setActionCommand(Constants.MELEE_ATTACK);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;

        JButton myButton;


        pane.add(meleeButton, c);


        setMeleeField = new TextField("Melee Weapon");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        pane.add(setMeleeField, c);

        // Change the weapon to switch to once the rangeButton is clicked
        // Submit the user entered field to make the change
        submitMeleeButton = new Button("Submit");
        submitMeleeButton.setActionCommand(Constants.MELEE_WEAPON);
        submitMeleeButton.addActionListener(this);

        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;

        pane.add(submitMeleeButton, c);



        url = getClass().getResource("/Drawables/mage_icon_btn.png");
        imageIcon = new ImageIcon(url);
        mageButton = new JButton(imageIcon);
        mageButton.setText("Use Mage");
        mageButton.setBackground(new Color(0,0,0, 255));
        mageButton.setActionCommand(Constants.MAGE_ATTACK);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;

        pane.add(mageButton,c);



        setMageField = new TextField("Mage Weapon");
        setMageField.setName("test");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        pane.add(setMageField, c);

        // Change the weapon to switch to once the rangeButton is clicked
        // Submit the user entered field to make the change
        submitMageButton = new Button("Submit");
        submitMageButton.setActionCommand(Constants.MAGE_WEAPON);
        submitMageButton.addActionListener(this);

        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;

        pane.add(submitMageButton, c);




        url = getClass().getResource("/Drawables/food_icon_button.png");
        imageIcon = new ImageIcon(url);
        foodButton = new JButton(imageIcon);
        foodButton.setText("Eat Food");
        foodButton.setBackground(new Color(0,0,0, 255));
        foodButton.setActionCommand(Constants.EAT_FOOD);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.7;


        pane.add(foodButton,c);

        setFoodField = new TextField("Enter Food");
        setFoodField.setName("test");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.LINE_START;
        pane.add(setFoodField, c);

        // Change the weapon to switch to once the rangeButton is clicked
        // Submit the user entered field to make the change
        submitFoodButton = new Button("Submit");
        submitFoodButton.setActionCommand(Constants.FOOD_ITEM);
        submitFoodButton.addActionListener(this);

        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.LINE_END;

        pane.add(submitFoodButton, c);












        //pane.setLayout(gridBagLayout);

        //pane.doLayout();

//        myJFrame.add(rangeButton);
//        myJFrame.add(meleeButton);
        // log(getClient().getCanvas().toString());

        rangeButton.addActionListener(this);
        meleeButton.addActionListener(this);
        mageButton.addActionListener(this);
        foodButton.addActionListener(this);

        myJFrame.setVisible(true); // Need to set visible after adding components




        return;

    }

    public class AttackListener implements Runnable {


        Object lock = new Object(); // lock used to only run the switch code when the preference file has changed


        // Listens for changes made to Preferences. I.E. User clicks Equip Shortbow, writes to preferences, reads for
        // that change and equips the newly written item
        @Override
        public void run() {
//            preferences.addPreferenceChangeListener(new PreferenceChangeListener() {
//                @Override
//                public synchronized void preferenceChange(PreferenceChangeEvent evt) {
//                    if (evt.getKey().contains(Constants.ACTION)) {
//
//                        followerAction = evt.getNewValue();
//                        synchronized (lock) { // Code can proceed
//                            log("About to notify() " + evt.getNewValue());
//                            lock.notify();
//                        }
//                    }
//                }
//            });

            pcl = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (evt.getKey().contains(Constants.ACTION)) {

                        followerAction = evt.getNewValue();
                        synchronized (lock) { // Code can proceed
                            log("About to notify() " + evt.getNewValue());
                            lock.notify();
                        }
                    }
                }
            };

            preferences.addPreferenceChangeListener(pcl);

            while(!Thread.currentThread().isInterrupted()) {

                log("test before lock");

                synchronized (lock) {
                    log("Before waiting");
                    try {
                        lock.wait();  // Don't run this code until the preference of interest has been receieved
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Inventory followerInventory = getInventory(); // Get Current inventory
                    log("After unlocked");
                    switch (followerAction) {
                        case Constants.RANGE_ATTACK:
                            // Interact with an item in your inventory based on item ID and string representing the interaction
                            log("test Beforeee");
                            if(followerInventory.contains(preferences.get(Constants.RANGE_WEAPON, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.RANGE_WEAPON, "N/A"), "Wield");
                                //log("testttt");
                            }
                            log("testttt");
                            break;
                        case Constants.MELEE_ATTACK:
                            log("test Beforeee");
                            // Interact with an item in your inventory based on item ID and string representing the interaction
                            if(followerInventory.contains(preferences.get(Constants.MELEE_WEAPON, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.MELEE_WEAPON, "N/A"), "Wield");
                                //log("testttt");
                            }
                            break;
                        case Constants.EAT_FOOD:
                            log("test Beforeee");
                            if(followerInventory.contains(preferences.get(Constants.FOOD_ITEM, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.FOOD_ITEM, "N/A"), "Eat");
                                //log("testttt");
                            }


                    }
                    // Equipping weapons de-follows the lead user, need to re follow
                    if(leadPlayer!= null) {
                        if (!leadPlayer.isInCombat()) {

                            leadPlayer.interact("Follow");
                        }
                    }
                }

            }
        }
    }

    public void initFrame(){
        myJFrame = new MyJFrame();
        // Set title of Jframe, with whatever lead user was chosen
        myJFrame.setTitle("Multiboxing Control Center, Lead User: " + preferences.get("LeadUser", "No Lead User Chosen"));
        // So we only have 1 Jframe instance open across multiple running scripts
        myJFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log("Window Closing");
                preferences.putBoolean("JFrameInstanceOpen", false);
                super.windowClosed(e);
            }
            @Override
            public void windowOpened(WindowEvent e) {
                log("Window Opened");
                preferences.putBoolean("JFrameInstanceOpen", true);
                super.windowOpened(e);
            }
        });

        // myJFrame.setUndecorated(true);
        //myJFrame.setBounds(100  , 700,100,100);
        log(" " + getClient().getInstance().getApplet().getX() + getClient().getInstance().getApplet().getY());
        //myJFrame.setLocationRelativeTo(getClient().getInstance().getCanvas().getParent());
        myJFrame.setBounds(100  , 700,500,175);
        //myJFrame.setBackground(new Color(0,0,0,255));
        myJFrame.setAlwaysOnTop(true);


        Frame[] frames = Frame.getFrames();
        for(Frame frame : frames){
            if(frame.getTitle().contains("DreamBot")){
                theFrame = frame;
            }
        }

        myJFrame.setLocationRelativeTo(theFrame);

       // myJFrame.setBackground(new Color(69, 60, 51, 255));
    }




    public void initStartFrame(){


        preferences.putBoolean("JFrameInstanceOpen", true);


        startJFrame = new JFrame("Specify Lead Player");
        startJFrame.setBounds(0,0, 285 ,75);
        startJFrame.setAlwaysOnTop(true);
        startJFrame.setLocationRelativeTo(getClient().getInstance().getCanvas()); // Make the jframe start in the middle of the client

        startJFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log("Window Closed");
                preferences.putBoolean("JFrameInstanceOpen", false);
                super.windowClosing(e);
            }

            @Override
            public void windowOpened(WindowEvent e) {
                preferences.putBoolean("JFrameInstanceOpen", true);
                log("Window Opened");
                super.windowOpened(e);
            }
        });



        Container pane = startJFrame.getContentPane(); // The ContentPane of the Jframe
        pane.setBackground(new Color(69, 60, 51, 255));

        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();



        TextField setLeadUser = new TextField();
        setLeadUser.setName("Set Lead User");
        c.ipadx = 75; // Make the Textfield longer by giving it extra padding
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,14,0,7);

        pane.add(setLeadUser, c);

        JButton submitButton = new JButton("Submit");
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0,7,0,14);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.put("LeadUser", setLeadUser.getText().toString());
                //preferences.putBoolean("JFrameInstanceOpen", true);
                log("Button was clicked");
                synchronized (MyBot.class){ // The object with the lock is the class itself.
                    MyBot.class.notify(); // Release the lock on "this" which is referring to the class itself. Could be another object
                    // This is used so that only the first Jframe is shown until the button is clicked, then the Notify
                    // will wake up the wait() method from the main Thread and the other Jframe will be executed

                    // Closes the starting Jframe as if we pressed the X button. Dispatches that event programmatically
                    startJFrame.dispatchEvent(new WindowEvent(startJFrame, WindowEvent.WINDOW_CLOSING));
                }


            }
        });


        pane.add(submitButton,c);

        startJFrame.setVisible(true); // Have to set visible after components are added
    }


}
