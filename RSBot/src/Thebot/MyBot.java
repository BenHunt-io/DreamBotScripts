package Thebot;

import com.sun.corba.se.pept.transport.ListenerThread;
import javafx.scene.control.*;
import oracle.jrockit.jfr.JFR;
import org.dreambot.api.Client;
import org.dreambot.api.input.event.impl.mouse.impl.click.ClickEvent;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.hotkeys.PlayerAttackOption;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.magic.cost.Rune;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.chatbox.ChatboxChannel;
import org.dreambot.api.wrappers.widgets.chatbox.ChatboxMessage;
import org.dreambot.api.wrappers.widgets.message.Message;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

@ScriptManifest(category = Category.WOODCUTTING, name = "Mulitbox", author = "Computor", version = 4.0)
public class MyBot extends AbstractScript implements ActionListener {


    private List<Player> playerList;
    Player leadPlayer;
    Player localPlayer;
    private NPC currentTarget;
    Tile lootTile; // Tile that NPC dies on, where the loot would be.

    CombatListener combatListener;
    private MultiBoxGui multiBoxGui;
    public List<World> f2pWorlds;

    PreferenceChangeListener pcl; // listens for changes made in Preferences
    private ListListeners listListeners;
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

    ///////// SQLite DataBase //////////////////////
    DatabaseHelper dbHelper;

    ///////////// Paint ///////////////
    public int r,g,b = 0;
    public boolean increasing = true;
    public Integer onPaintCount = 0;

    public int x = 10;
    public int y = 10;
    public int width = 150;
    public int height = 20;


    //////////////////////


    private Character monster;

    Client client;
    private JFrame myJFrame;
    private JFrame startJFrame;

    @Override
    public void onStart() {
        log("On Start Called");

        dbHelper = new DatabaseHelper();
        dbHelper.onCreateDB();

        preferences = Preferences.userNodeForPackage(MyBot.class);
        combatListener = new CombatListener();
        multiBoxGui = new MultiBoxGui(preferences);


        log(" " + preferences.getBoolean("JFrameInstanceOpen", false));

        preferences.putBoolean("JFrameInstanceOpen", false);
        if(!preferences.getBoolean("JFrameInstanceOpen", false)) {
            log("test");

            multiBoxGui.initStartFrame(getClient(),getPlayers(), this);
            listListeners = new ListListeners(multiBoxGui, preferences);
            multiBoxGui.leadUserList.addListSelectionListener(listListeners.new LeadUserListener());


            Character character;
            Character character1;
            synchronized (MyBot.class) {
                try {
                    MyBot.class.wait();
                    multiBoxGui.displayMultiBoxGui();
                    initializeListeners(); // For the gui
                    populateWorldHopper();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }



        localPlayer = getLocalPlayer();


//        // Simply Pause the main thread if the user is the lead user.  So only thing updating is the paint
//        if(localPlayer == leadPlayer)
//            synchronized (MyBot.class){
//                try {
//                    MyBot.class.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }


        // Get our bots as player objects.
        Players players = getPlayers();
        playerList = players.all();
        String leadUser = preferences.get("LeadUser","N/A");
        for(int i = 0; i < playerList.size(); i++){
            log(playerList.get(i).getName());
            if(playerList.get(i).getName().toString().equals(leadUser)){
                log("Matching Lead User Found");
                leadPlayer = playerList.get(i);
            }
        }





        super.onStart();
    }

    @Override
    public int onLoop() {

        //log("loop " + Thread.currentThread());
        Object combatLock = new Object();

        log("new Loop");
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
                        //log("leadPlayer is not in combat");
                        if(localPlayer.isStandingStill())
                            leadPlayer.interact("Follow");
                        Thread.sleep(100);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            }

            log("In Combat Now");




            if(leadPlayer.isInCombat()){
                currentTarget = (NPC)leadPlayer.getInteractingCharacter();
                currentTarget.interact("Attack");
            }



            // Once a kill is detected, handle kill in this thread
            Thread handleKillThread;

            lootTile = null;
        // Playesr can run 2 squares every tick. (0.6 seconds) Need to update target's tile atleast every 300ms
            while (leadPlayer.isInCombat()) {
                try {
                    if(!localPlayer.isInteracting(currentTarget))
                        currentTarget.interact("Attack");

                    log(currentTarget.getBoundingBox().getX() + " " + currentTarget.getBoundingBox().getY());
                    currentTarget = (NPC)leadPlayer.getInteractingCharacter();
                    if(currentTarget != null)
                        lootTile = currentTarget.getTile();
                    Thread.sleep(200);

                    if(!currentTarget.exists() || !leadPlayer.isInCombat()) {
                        handleKillThread = new Thread(new handleKillRunnable(currentTarget, lootTile));
                        handleKillThread.start();
                    }
                    log("Getting Tile");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }



            localPlayer.isInCombat();

        return 0;
        }



        public class handleKillRunnable implements Runnable {

            public NPC deadTarget;
            public Tile lootTile;

            public handleKillRunnable(NPC deadTarget, Tile lootTile){
                this.deadTarget = deadTarget;
                this.lootTile = lootTile;
            }
            @Override
            public void run() {

                log("before Check to see if killed");
                int time = 0;
                // Wait till NPC's animation stops. It may not be dead, so stop after 5 seconds
                while(deadTarget != null && time < 5){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time++;

                }

                // Target is null, so most likely the target is dead. See if tile contains drop.
                if(getGroundItems().getGroundItems(lootTile) != null){
                    GroundItem[] groundItems = getGroundItems().getGroundItems(lootTile);
                    log("I killed it");
                    // Display recent kill in List
                    multiBoxGui.groundItemLM.removeAllElements();
                    for(int i = 0; i < groundItems.length; i++){
                        multiBoxGui.groundItemLM.insertElementAt(groundItems[i].getName(), multiBoxGui.groundItemLM.size());
                    }
                    multiBoxGui.recentKillLbl.setText("Recent Kill By: " + localPlayer.getName());
                    Keyboard keyboard = getKeyboard();
                    // Last argument is speed in the typing
                    keyboard.type("I Killed it");
                }
            }
        }

    @Override
    public void onExit() {
        //attackListenerThread.interrupt(); // end the thread?=
        attackListenerThread.interrupt();
        preferences.removePreferenceChangeListener(pcl); // Removes the listener
        log("test onEXIT() called" );
        super.onExit();
    }

    public void populateWorldHopper(){

        Worlds worlds = getWorlds();
        f2pWorlds = worlds.f2p();
        for(int i = 0; i<f2pWorlds.size(); i++){
            multiBoxGui.worldHopLM.insertElementAt(f2pWorlds.get(i),multiBoxGui.worldHopLM.size());
        }
    }

    @Override
    public void onPaint(Graphics graphics) {
       // log("OnPaint Called");

        onPaintCount++;

        if(r < 255 && g < 255 && b < 255 && increasing){
            r++;g++;b++;
            if(r == 255) increasing = false;
        }
        else{
            r--;g--;b--;
            if(r == 0) increasing = true;
        }
        if(5+10 == 20){
            log("test");
        }

        y = 10;
        int stringY = 25;
        for(int i = 0; i<10; i++) {
            int calculatedWidth = width * getSkills().getBoostedLevels(Skill.HITPOINTS) / getSkills().getRealLevel(Skill.HITPOINTS);
            // set color is like a pen that can change colors
            graphics.setColor(new Color(87,201,38, 80));
            graphics.fillRect(x, y, calculatedWidth , height);
            graphics.drawRect(x, y, width, height);
            graphics.setColor(Color.white);
            String player1 = new String(localPlayer.getName() + "  " +getSkills().getBoostedLevels(Skill.HITPOINTS) + "/" + getSkills().getRealLevel(Skill.HITPOINTS));
            graphics.drawString(player1, x+5, stringY);
            graphics.drawString(onPaintCount.toString(), 10, 45);

            graphics.setColor(new Color(255, 25, 23, 50));
            graphics.fillRect(calculatedWidth + x,y,width - calculatedWidth,20);
//            graphics.drawRect(calculatedWidth + x,y,width - calculatedWidth,20);

            graphics.setColor(new Color(87,201,38, 80));
            graphics.drawRect(x, y, width, height);

            y += 25;
            stringY+= 25;
        }
        if(localPlayer.isInteracting(currentTarget))
            graphics.drawRect((int)currentTarget.getBoundingBox().getX(),(int)currentTarget.getBoundingBox().getY(),
                    (int)currentTarget.getBoundingBox().getWidth(),(int)currentTarget.getBoundingBox().getHeight());




//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log("Was clicekd  " + Thread.currentThread());

        Inventory followerInventory = getInventory();



        log(e.getActionCommand());
        switch(e.getActionCommand()) {

            // Equipping different weapons to attack with / using items. Like eating food
            case Constants.EQUIP_MELEE:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.EQUIP_MELEE);

                break;
            case Constants.EQUIP_RANGE:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.EQUIP_RANGE);
                break;
            case Constants.EQUIP_MAGE:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.EQUIP_MAGE);
                break;
            case Constants.EAT_FOOD:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.EAT_FOOD);
                break;
            case Constants.DRINK_POTIONS:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.DRINK_POTIONS);
                break;
            case Constants.CAST_SPELL:
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                preferences.put(Constants.ACTION, Constants.CAST_SPELL);
                break;
            case Constants.WORLD:
                preferences.put(Constants.ACTION, Constants.WORLD);
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                break;
            case Constants.LOGOUT_ALL:
                preferences.put(Constants.ACTION, Constants.LOGOUT_ALL);
                log("buttonId/actionEventID: " + e.getID() + " " + e.getActionCommand());
                break;



                // Changing Text of different combat buttons
            case Constants.RANGE_WEAPON:
                log("Setting Range Weapon TExt");
                multiBoxGui.rangeLabel.setText("Equip: " + multiBoxGui.RangeField.getText().toString());
                preferences.put(Constants.RANGE_WEAPON, multiBoxGui.RangeField.getText().toString());
                break;
            case Constants.MELEE_WEAPON:
                log("Setting Melee Weapon Text");
                multiBoxGui.meleeLabel.setText("Equip: " + multiBoxGui.meleeField.getText().toString());
                preferences.put(Constants.MELEE_WEAPON, multiBoxGui.meleeField.getText().toString());
                break;
            case Constants.MAGE_WEAPON:
                log("Setting Mage Weapon Text");
                multiBoxGui.mageLabel.setText("Equip: " + multiBoxGui.MageField.getText().toString());
                preferences.put(Constants.MAGE_WEAPON, multiBoxGui.MageField.getText().toString());
                break;
            case Constants.FOOD_ITEM:
                log( "Setting Food Button Text");
                multiBoxGui.eatLabel.setText("Equip " + multiBoxGui.FoodField.getText().toString());
                preferences.put(Constants.FOOD_ITEM, multiBoxGui.FoodField.getText().toString());
                break;
                // Inserts a new potion into the list -
            case Constants.POTION_ITEMS:
                log("test in potions");
                multiBoxGui.potionListModel.insertElementAt(multiBoxGui.PotionField.getText().toString(), multiBoxGui.potionListModel.size());
                break;
            case Constants.SPELL:
                log("test in potions");
                multiBoxGui.spellListModel.insertElementAt(multiBoxGui.spellField.getText().toString(), multiBoxGui.spellListModel.size());
                break;

        }

    }





    public void initLayout(){
//
//        Container pane = myJFrame.getContentPane();
//        pane.setLayout(new GridBagLayout());
//        pane.setBackground(new Color(69, 60, 51, 255));
//
//
//        GridBagConstraints c = new GridBagConstraints(); // Constrainst for Gridbaglayout
//        Insets leftRightInsets = new Insets(0,5,0,5); // basically margins
//
//
//
//        URL url = getClass().getResource("/Drawables/range_icon_button.png");
//        ImageIcon imageIcon = new ImageIcon(url);
//        // Initialize Buttons, add actionCommand to differentiate buttons when clicked
//        if(imageIcon== null) log("image is null");
//        else log(imageIcon.getIconHeight() + " and " + imageIcon.getIconWidth() + "");
//        rangeButton = new JButton(imageIcon);
//        rangeButton.setText("Use Range");
//        rangeButton.setFont(Font.getFont(Font.SANS_SERIF));
//        rangeButton.setBackground(new Color(0,0,0, 255));
//        rangeButton.setActionCommand(Constants.RANGE_ATTACK);
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weightx = 0.5; // relative to the highest weight x in that column
//        c.weighty = 0.5; // Same for weighty, but for rows.
//       // c.insets = leftRightInsets;
//
//
//
//        pane.add(rangeButton, c);
//
//
//        setRangeField = new TextField("Range Weapon");
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_START;
//        pane.add(setRangeField, c);
//
//        // Change the weapon to switch to once the rangeButton is clicked
//        // Submit the user entered field to make the change
//        Button submitRangeButton = new Button("Submit");
//        submitRangeButton.setActionCommand(Constants.RANGE_WEAPON);
//        submitRangeButton.addActionListener(this);
//
//        c.gridx = 0;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_END;
//
//        pane.add(submitRangeButton, c);
//
//
//
//
//        url = getClass().getResource("/Drawables/thesword.png");
//        imageIcon = new ImageIcon(url);
//        meleeButton = new JButton(imageIcon);
//        meleeButton.setText("Use Melee");
//        meleeButton.setBackground(new Color(0,0,0, 255));
//        meleeButton.setActionCommand(Constants.MELEE_ATTACK);
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 1;
//        c.gridy = 0;
//        c.weightx = 0.5;
//        c.weighty = 0.5;
//
//        JButton myButton;
//
//
//        pane.add(meleeButton, c);
//
//
//        setMeleeField = new TextField("Melee Weapon");
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 1;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_START;
//        pane.add(setMeleeField, c);
//
//        // Change the weapon to switch to once the rangeButton is clicked
//        // Submit the user entered field to make the change
//        submitMeleeButton = new Button("Submit");
//        submitMeleeButton.setActionCommand(Constants.MELEE_WEAPON);
//        submitMeleeButton.addActionListener(this);
//
//        c.gridx = 1;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_END;
//
//        pane.add(submitMeleeButton, c);
//
//
//
//        url = getClass().getResource("/Drawables/mage_icon_btn.png");
//        imageIcon = new ImageIcon(url);
//        mageButton = new JButton(imageIcon);
//        mageButton.setText("Use Mage");
//        mageButton.setBackground(new Color(0,0,0, 255));
//        mageButton.setActionCommand(Constants.MAGE_ATTACK);
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 2;
//        c.gridy = 0;
//        c.weightx = 0.5;
//        c.weighty = 0.5;
//
//        pane.add(mageButton,c);
//
//
//
//        setMageField = new TextField("Mage Weapon");
//        setMageField.setName("test");
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 2;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_START;
//        pane.add(setMageField, c);
//
//        // Change the weapon to switch to once the rangeButton is clicked
//        // Submit the user entered field to make the change
//        submitMageButton = new Button("Submit");
//        submitMageButton.setActionCommand(Constants.MAGE_WEAPON);
//        submitMageButton.addActionListener(this);
//
//        c.gridx = 2;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_END;
//
//        pane.add(submitMageButton, c);
//
//
//
//
//        url = getClass().getResource("/Drawables/food_icon_button.png");
//        imageIcon = new ImageIcon(url);
//        foodButton = new JButton(imageIcon);
//        foodButton.setText("Eat Food");
//        foodButton.setBackground(new Color(0,0,0, 255));
//        foodButton.setActionCommand(Constants.EAT_FOOD);
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 0;
//        c.gridy = 2;
//        c.weightx = 0.7;
//
//
//        pane.add(foodButton,c);
//
//        setFoodField = new TextField("Enter Food");
//        setFoodField.setName("test");
//        c.fill = GridBagConstraints.NONE;
//        c.gridx = 0;
//        c.gridy = 3;
//        c.anchor = GridBagConstraints.LINE_START;
//        pane.add(setFoodField, c);
//
//        // Change the weapon to switch to once the rangeButton is clicked
//        // Submit the user entered field to make the change
//        submitFoodButton = new Button("Submit");
//        submitFoodButton.setActionCommand(Constants.FOOD_ITEM);
//        submitFoodButton.addActionListener(this);
//
//        c.gridx = 0;
//        c.gridy = 3;
//        c.anchor = GridBagConstraints.LINE_END;
//
//        pane.add(submitFoodButton, c);
//
//
//        rangeButton.addActionListener(this);
//        meleeButton.addActionListener(this);
//        mageButton.addActionListener(this);
//        foodButton.addActionListener(this);
//
//        myJFrame.setVisible(true); // Need to set visible after adding components
//
//
//
//
//        return;

    }

    public void initializeListeners(){

        // Main Tab
        multiBoxGui.meleeButton.setActionCommand(Constants.EQUIP_MELEE);
        multiBoxGui.rangeButton.setActionCommand(Constants.EQUIP_RANGE);
        multiBoxGui.mageButton.setActionCommand(Constants.EQUIP_MAGE);
        multiBoxGui.eatButton.setActionCommand(Constants.EAT_FOOD);
        multiBoxGui.drinkButton.setActionCommand(Constants.DRINK_POTIONS);
        multiBoxGui.manualCastButton.setActionCommand(Constants.CAST_SPELL);
        multiBoxGui.hopWorldBtn.setActionCommand(Constants.WORLD);
        multiBoxGui.logoutAll.setActionCommand(Constants.LOGOUT_ALL);

        // Settings Tab
        multiBoxGui.saveMeleeWeapon.setActionCommand(Constants.MELEE_WEAPON);
        multiBoxGui.saveRangeWeapon.setActionCommand(Constants.RANGE_WEAPON);
        multiBoxGui.saveMageWeapon.setActionCommand(Constants.MAGE_WEAPON);
        multiBoxGui.saveFood.setActionCommand(Constants.FOOD_ITEM);
        multiBoxGui.savePotion.setActionCommand(Constants.POTION_ITEMS);
        multiBoxGui.saveSpell.setActionCommand(Constants.SPELL);


        // Adding Listeners for the main Tab buttons
        multiBoxGui.meleeButton.addActionListener(this);
        multiBoxGui.rangeButton.addActionListener(this);
        multiBoxGui.mageButton.addActionListener(this);
        multiBoxGui.eatButton.addActionListener(this);
        multiBoxGui.manualCastButton.addActionListener(this);
        multiBoxGui.hopWorldBtn.addActionListener(this );
        multiBoxGui.logoutAll.addActionListener(this );

        // Adding Listeners for the save buttons in settings
        multiBoxGui.saveMeleeWeapon.addActionListener(this);
        multiBoxGui.saveRangeWeapon.addActionListener(this);
        multiBoxGui.saveMageWeapon.addActionListener(this);
        multiBoxGui.saveFood.addActionListener(this);
        multiBoxGui.savePotion.addActionListener(this);
        multiBoxGui.saveSpell.addActionListener(this);


        // Adding Listeners for selecting spells/pots in the main tab lists
        multiBoxGui.castSpellList.addListSelectionListener(listListeners.new SpellListListener());
        multiBoxGui.potionList.addListSelectionListener(listListeners.new PotionListListener());
        multiBoxGui.worldHopperList.addListSelectionListener(listListeners.new WorldHopperListener());

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
                        case Constants.EQUIP_MELEE:
                            // Interact with an item in your inventory based on item ID and string representing the interaction
                            log("test Beforeee");
                            if(followerInventory.contains(preferences.get(Constants.MELEE_WEAPON, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.MELEE_WEAPON, "N/A"), "Wield");
                            }
                            log("testttt");
                            break;
                        case Constants.EQUIP_RANGE:
                            log("test Beforeee");
                            // Interact with an item in your inventory based on item ID and string representing the interaction
                            if(followerInventory.contains(preferences.get(Constants.RANGE_WEAPON, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.RANGE_WEAPON, "N/A"), "Wield");
                            }
                            break;
                        case Constants.EQUIP_MAGE:
                            if(followerInventory.contains(preferences.get(Constants.MAGE_WEAPON, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.MAGE_WEAPON, "N/A"), "Wield");
                            }
                                break;
                        case Constants.EAT_FOOD:
                            log("test Beforeee");
                            if(followerInventory.contains(preferences.get(Constants.FOOD_ITEM, "N/A"))) {
                                followerInventory.interact(preferences.get(Constants.FOOD_ITEM, "N/A"), "Eat");
                            }
                            break;

                        case Constants.WORLD:
                            log("test Beforeee in Worlds: ");
                            World world = f2pWorlds.get(preferences.getInt(Constants.WORLD, 0));
                            WorldHopper worldHopper = getWorldHopper();
                            worldHopper.hopWorld(world);
                            break;
                        case Constants.CAST_SPELL:
                            log("test in cast spell");
                            Magic magic = getMagic();
                            // Checking all versions of the enum
                            for(Normal spell : Normal.values()) {
                                if (spell.toString().contains(preferences.get(Constants.SPELL, "N/A"))) {
                                    log("Can cast: " + spell.toString());
                                    if(magic.canCast(spell)){
                                        magic.castSpellOn(spell, currentTarget);
                                    }
                                }
                            }
                            break;
                        case Constants.LOGOUT_ALL:
                            log("Logging out");
                            getTabs().logout();
                            break;


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


}
