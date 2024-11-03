
import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.wrappers.interactive.Player;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.List;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 7/3/2017.
 */
public class MultiBoxGui extends MethodContext{


    private JFrame startJFrame; // first window that pops up to the user to select a lead user

    private JFrame myFrame; // Main frame for the window
    // outermost layout placed in frame, other layouts placed in this
    private FlowLayout flowLayout;
    // GridBagLayout panel will go inside flowlayout, will store left/top/bottom panels
    private Panel mainPanel; // Panel for main tab
    private Panel settingsPanel; // Pain for settings Panel

    private Preferences preferences;

    private JTabbedPane jTabbedPane;

    private MethodContext methodContext;


    ////////// Right Panel /////////////////

    public Label meleeLabel;
    public JButton meleeButton;

    public Label rangeLabel;
    public JButton rangeButton;

    public Label mageLabel;
    public JButton mageButton;

    public Label manualCastLabel;
    public JButton manualCastButton;

    public Label eatLabel;
    public JButton eatButton;


    public Label drinkLabel;
    public JButton drinkButton;

    public JList castSpellList;
    public JScrollPane castSpellScrollPane;

    JButton tradeButton;

    public DefaultListModel potionListModel;
    public JList potionList;

    public Label abandonShip;
    public JButton abandonShipButton;

    public Label specialAtkLabel;
    public JButton specialAttackBtn;

    ////////// Left Panel /////////////////////

    public DefaultListModel accountsLM;
    public JList multiboxedAccounts;

    public JList groundItemList;
    public DefaultListModel groundItemLM;
    public JScrollPane groundItemScroll;
    public Label recentKillLbl;

    ////////// Settings Panel /////////////////

    public JLabel setMeleeWeaponLbl;
    public JTextField meleeField;
    public JButton saveMeleeWeapon;

    public JLabel setRangeWeaponLbl;
    public JTextField RangeField;
    public JButton saveRangeWeapon;

    public JLabel setMageWeaponLbl;
    public JTextField MageField;
    public JButton saveMageWeapon;

    public JLabel setFoodLbl;
    public JTextField FoodField;
    public JButton saveFood;

    public JLabel setPotionLbl;
    public JTextField PotionField;
    public JButton savePotion;

    public JLabel setSpellLbl;
    public JTextField spellField;
    public JButton saveSpell;

    public Checkbox autoCast;
    public JButton saveAutoCast;
    public JButton deleteSpell;
    public JList spellList;
    public DefaultListModel spellListModel;
    public JScrollPane spellScrollPane;


    /////////// Bottom Panel //////////////////////////

    // World Hopper
    public JList worldHopperList;
    public DefaultListModel worldHopLM;
    public JLabel worldHopLabel;
    public JScrollPane scrollPaneHopper;
    public JButton hopWorldBtn;
    public JButton refreshWorldBtn;

    public JButton logout;
    public JButton logoutAll;


    ////////// Init Start Frame ////////////////////////

    public Label selectLeadUserLbl;
    public JList leadUserList;

    JButton refreshButton;
    JButton submitButton;



    /**
     * Sets up the GUI. The JFrame, myFrame, has a flowLayout
     * set to it and then a panel is added which contains a GridBagLayout.
     * Everything in the GUI will go inside the panel with the GridBagLayout
     * Also passes in preferences reference that was created in the main class
     */
    public MultiBoxGui(Preferences preferences){

        this.preferences = preferences; // saves the preferences reference so we can write and read


        myFrame = new JFrame();
        flowLayout = new FlowLayout();
        myFrame.setLayout(flowLayout);
        mainPanel = new Panel();
        mainPanel.setLayout(new GridBagLayout());
        settingsPanel = new Panel();
        settingsPanel.setLayout(new GridBagLayout());


        myFrame.setLocationRelativeTo(null); // null = Center of client
        myFrame.setBounds(500,250,575,575); // Window size
        Container frameContainer = myFrame.getContentPane();
        frameContainer.setBackground(new Color(239,239,239,255)); // OffWhite

        // Record when window closes and opens
        myFrame.addWindowListener(new WindowAdapter() {
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




    }


    /**
     * Starts first frame that pops up to the user. User selects Lead User before going to the main GUI. have to pass
     * in Client so we can set locationRelativeTo the client.
     */
    public void initStartFrame(Client client, Players players, MethodContext methodContext) {


        this.methodContext = methodContext;

        startJFrame = new JFrame("Specify Lead Player");
        startJFrame.setBounds(0, 0, 180, 315);
        startJFrame.setAlwaysOnTop(true);
        startJFrame.setLocationRelativeTo(client.getInstance().getCanvas()); // Make the jframe start in the middle of the client

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



        selectLeadUserLbl = new Label("Select Lead User");
        selectLeadUserLbl.setForeground(Color.white);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        pane.add(selectLeadUserLbl, c);


        DefaultListModel leadUserLM = new DefaultListModel();
        leadUserList = new JList(leadUserLM);

        //leadUserList.setPreferredSize(new Dimension(100,200));
        leadUserList.setVisibleRowCount(4);

        JScrollPane leadUserScrollPane = new JScrollPane(leadUserList);
        leadUserScrollPane.setPreferredSize(new Dimension(100, 200));



        // Insert all found players into table
        if(players != null) {
            for (int i = 0; i < players.all().size(); i++){
                leadUserLM.insertElementAt(players.all().get(i).getName(),i);
            }
        }

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;


        pane.add(leadUserScrollPane, c);

        refreshButton = new JButton("Refresh");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;

        pane.add(refreshButton,c);


        submitButton = new JButton("Submit");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.put("LeadUser", leadUserList.getSelectedValue().toString());
                //preferences.putBoolean("JFrameInstanceOpen", true);
                log("Button was clicked: " + leadUserList.getSelectedValue().toString());
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

        // Refresh list of players to select Lead User from
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log("Refresh Button Clicked");
                if (methodContext.getPlayers() != null) {
                    leadUserLM.removeAllElements();
                    List<Player> playerList = methodContext.getPlayers().all();
                    int counter = 0;
                    for (Player player : playerList) {
                        log("adding players on refresh: " + playerList.size());
                        leadUserLM.insertElementAt(player.getName(), counter);
                        counter++;
                    }
                }
            }
        });



        startJFrame.setVisible(true); // Have to set visible after components are added
    }

    public void displayMultiBoxGui(){

        initializeMenu(myFrame);
        initializeLeftPanel(mainPanel);
        initializeRightPanel(mainPanel);
        initializeBottomPanel(mainPanel);

        initializeSettingsPanel(settingsPanel);
        initializeTabs();
        myFrame.add(jTabbedPane);



        myFrame.setVisible(true);
    }


    // Initializes Tabs look and feel. Adds
    public void initializeTabs(){
        // Set UI managers default tab selected colors. In the method installDefaults in the BasicTabbedPaneUI, it will
        // get the color for selected that we put here
        UIManager.put("TabbedPane.selected", Color.DARK_GRAY);
        // Default tabs at the top, no scroll
        jTabbedPane = new JTabbedPane();

        jTabbedPane.addTab("Main", mainPanel);
        jTabbedPane.addTab("Settings", settingsPanel);
        jTabbedPane.setBackground(new Color(83,75,51,255));
        jTabbedPane.setBorder(BorderFactory.createEmptyBorder());
        jTabbedPane.setForeground(Color.WHITE);


    }

    public void initializeMenu(JFrame myFrame){

//        Menu main = new Menu("Main");
//        Menu settings = new Menu("Settings");
//
//        MenuBar menuBar = new MenuBar();
//        menuBar.add(main);
//        menuBar.add(settings);
//
//        myFrame.setMenuBar(menuBar);
    }

    public void initializeLeftPanel(Panel mainPanel){

        GridBagConstraints c; // to set constraints of stuff


        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(69,62,42,255)); //69, 62, 42


        // Label
        Label multiboxedAccountsLabel = new Label("Multiboxed Accounts");
        multiboxedAccountsLabel.setForeground(Color.WHITE);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridwidth = 2;

        leftPanel.add(multiboxedAccountsLabel,c);


        // Checkbox to whether all accounts are selected or not.
        CheckboxGroup t = new CheckboxGroup();
        Checkbox all = new Checkbox("All", true, t);
        all.setForeground(Color.WHITE);
        Checkbox individual = new Checkbox("Individual", false, t);
        individual.setForeground(Color.white);

        Panel checkboxPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 0,0));
//        checkboxPanel.setBackground(new Color(255,255,255,0));

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridwidth = 2;

        checkboxPanel.add(all);
        checkboxPanel.add(individual);

        leftPanel.add(checkboxPanel, c);


        final int size = 4;
        String[] accounts = {"Archer5252", "Zezima", "DudeKilla", "PkMasta"};
        accountsLM =  new DefaultListModel();
        multiboxedAccounts = new JList(accountsLM);
        JScrollPane accountsScrollPane = new JScrollPane(multiboxedAccounts);
        accountsScrollPane.setPreferredSize(new Dimension(105,140));
        for(int i = 0; i<size; i++){
            accountsLM.insertElementAt(accounts[i],i);
        }


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(0,5,5,5);
        c.gridwidth = 2;

        leftPanel.add(accountsScrollPane, c);



        JButton refresh = new JButton("Refresh");
        JButton delete = new JButton("Delete");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridwidth = 1;

        leftPanel.add(refresh,c);

        c.gridx = 1;

        leftPanel.add(delete,c);



        recentKillLbl = new Label("Recent Kill By: ");
        recentKillLbl.setForeground(Color.WHITE);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;

        leftPanel.add(recentKillLbl,c);


        String[] inventoryItems = {"Bronze arrow", "Bronze sword", "RunePlatebody", "sword",
                "Bones", "Gold", "Cowhide"};

        groundItemLM = new DefaultListModel();

        groundItemList = new JList(groundItemLM);
        groundItemList.setVisibleRowCount(5);

        // Make the list scrollable by putting it in a JScrollPane
        groundItemScroll = new JScrollPane(groundItemList);

        groundItemList.setPreferredSize(new Dimension(105,95));
        groundItemScroll.setPreferredSize(new Dimension(105,95));


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.insets = new Insets(0,0,15,0);

        leftPanel.add(groundItemScroll,c);


        // Constraints for the whole left panel
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(15,0,0,15);
        mainPanel.add(leftPanel, c);


    }

    public void initializeRightPanel(Panel mainPanel){


        GridBagConstraints c;

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());;
        rightPanel.setBackground(new Color(69,62,42,255));



        meleeLabel = new Label("Melee Weapon");
        meleeLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;

        rightPanel.add(meleeLabel,c);




        URL url = getClass().getResource("/Drawables/thesword.png");
        ImageIcon icon = new ImageIcon(url);

        URL url2 = getClass().getResource("/Drawables/thesword_clicked.png");
        ImageIcon icon2 = new ImageIcon(url2);

        meleeButton = new JButton();
        meleeButton.setIcon(icon);
        // Create empty border, so it's just the picture
        meleeButton.setBorder(BorderFactory.createEmptyBorder());
        // Transparent Background, to hide default square button background
        meleeButton.setContentAreaFilled(false);

        meleeButton.setPressedIcon(icon2);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
//        c.weightx =1.0;
//        c.weighty =1.0;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        rightPanel.add(meleeButton,c);




        url = getClass().getResource("/Drawables/range_icon_button.png");
        icon = new ImageIcon(url);


        url2 = getClass().getResource("/Drawables/range_icon_button_clicked.png");
        icon2 = new ImageIcon(url2);

        rangeLabel = new Label("Range Weapon");
        rangeLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;

        rightPanel.add(rangeLabel,c);



        rangeButton = new JButton();
        rangeButton.setIcon(icon);
        // Create empty border, so it's just the picture
        rangeButton.setBorder(BorderFactory.createEmptyBorder());
        // Transparent Background, to hide default square button background
        rangeButton.setContentAreaFilled(false);

        rangeButton.setPressedIcon(icon2);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
//        c.weightx =1.0;
//        c.weighty =1.0;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        rightPanel.add(rangeButton,c);



        url = getClass().getResource("/Drawables/mage_icon_btn.png");
        icon = new ImageIcon(url);

        url2 = getClass().getResource("/Drawables/mage_icon_btn_clicked.png");
        icon2 = new ImageIcon(url2);

        mageLabel = new Label("Mage Weapon");
        mageLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;

        rightPanel.add(mageLabel,c);


        mageButton = new JButton();
        mageButton.setIcon(icon);
        // Create empty border, so it's just the picture
        mageButton.setBorder(BorderFactory.createEmptyBorder());
        // Transparent Background, to hide default square button background
        mageButton.setContentAreaFilled(false);

        mageButton.setPressedIcon(icon2);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
//        c.weightx =1.0;
//        c.weighty =1.0;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        rightPanel.add(mageButton,c);




        url = getClass().getResource("/Drawables/spell_icon.png");
        icon = new ImageIcon(url);

        url2 = getClass().getResource("/Drawables/spell_icon_clicked.png");
        icon2 = new ImageIcon(url2);

        manualCastLabel = new Label("Cast: ");
        manualCastLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10,0,0,0);
        rightPanel.add(manualCastLabel,c);


        manualCastButton = new JButton();
        manualCastButton.setIcon(icon);
        // Create empty border, so it's just the picture
        manualCastButton.setBorder(BorderFactory.createEmptyBorder());
        // Transparent Background, to hide default square button background
        manualCastButton.setContentAreaFilled(false);

        manualCastButton.setPressedIcon(icon2);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;

        rightPanel.add( manualCastButton,c);



        url = getClass().getResource("/Drawables/food_icon_button.png");
        icon = new ImageIcon(url);

        url2 = getClass().getResource("/Drawables/food_icon_button_clicked.png");
        icon2 = new ImageIcon(url2);


        eatLabel = new Label("Food");
        eatLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(10,0,0,0);
        rightPanel.add(eatLabel,c);


        eatButton = new JButton();
        eatButton.setIcon(icon);
        // Create empty border, so it's just the picture
        eatButton.setBorder(BorderFactory.createEmptyBorder());
        // Transparent Background, to hide default square button background
        eatButton.setContentAreaFilled(false);

        eatButton.setPressedIcon(icon2);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;

        rightPanel.add(eatButton,c);


        url = getClass().getResource("/Drawables/potion_icon.png");
        icon = new ImageIcon(url);

        url2 = getClass().getResource("/Drawables/potion_icon_clicked.png");
        icon2 = new ImageIcon(url2);

        drinkLabel = new Label("Strength Potion");
        drinkLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.insets = new Insets(10,0,0,0);

        rightPanel.add(drinkLabel,c);


        drinkButton = new JButton();
        drinkButton.setIcon(icon);
        // Create empty border, so it's just the picture
        drinkButton.setBorder(BorderFactory.createEmptyBorder());
        // Transparent Background, to hide default square button background
        drinkButton.setContentAreaFilled(false);

        drinkButton.setPressedIcon(icon2);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 3;

//        c.weightx =1.0;
//        c.weighty =1.0;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        rightPanel.add(drinkButton,c);




        // List of spells to use in manual casting. Holds reference to spellList in settings
        // Potion List
        spellListModel = new DefaultListModel();

        castSpellList = new JList(spellListModel);
        castSpellList.setVisibleRowCount(4);
        castSpellList.setPreferredSize(new Dimension(125,75));

        castSpellScrollPane = new JScrollPane(castSpellList);
        castSpellScrollPane.setPreferredSize(new Dimension(125,75));

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridheight = 1;
        c.insets = new Insets(10,0,0,0);


        rightPanel.add(castSpellScrollPane,c);




        // Trade Button
        URL tradeButtonUrl = getClass().getResource("/Drawables/trade_button.png");
        ImageIcon tradeButtonIcon = new ImageIcon(tradeButtonUrl);
        tradeButton = new JButton(tradeButtonIcon);
        tradeButton.setBorder(BorderFactory.createEmptyBorder());
        tradeButton.setContentAreaFilled(false);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.gridheight = 1; // takes up two rows in height


        rightPanel.add(tradeButton,c);



        // Potion List
        potionListModel = new DefaultListModel();
        potionList = new JList(potionListModel);
        potionList.setVisibleRowCount(4);
        potionList.setPreferredSize(new Dimension(125,75));

        JScrollPane potionScrollPane = new JScrollPane(potionList);
        potionScrollPane.setPreferredSize(new Dimension(125,75));

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        c.gridheight = 1;
        c.insets = new Insets(10,0,0,0);


        rightPanel.add(potionScrollPane,c);





        abandonShip = new Label("Abandon Ship");
        abandonShip.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;


        rightPanel.add(abandonShip,c);

        System.out.println("test");
        URL abandonShipUrl = getClass().getResource("/Drawables/abandon_ship.png");
        ImageIcon abandonShipIcon = new ImageIcon(abandonShipUrl);
        abandonShipButton = new JButton(abandonShipIcon);
        abandonShipButton.setBorder(BorderFactory.createEmptyBorder());
        abandonShipButton.setContentAreaFilled(false);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 7;
        c.gridheight = 1; // takes up two rows in height

        rightPanel.add(abandonShipButton,c);



        // Special Attack Button
        specialAtkLabel = new Label("Special Attack");
        specialAtkLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight= 2;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(5,0,0,0);

        rightPanel.add(specialAtkLabel,c);


        URL specialAtkUrl = getClass().getResource("/Drawables/special_attack_bar.png");
        ImageIcon specialAttackIcon = new ImageIcon(specialAtkUrl);


        specialAttackBtn = new JButton(specialAttackIcon);
        specialAttackBtn.setBorder(BorderFactory.createEmptyBorder());
        specialAttackBtn.setContentAreaFilled(false);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.anchor = GridBagConstraints.CENTER;

        rightPanel.add(specialAttackBtn,c);










        // Entire right panel constraints
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(15,0,0,0);


        mainPanel.add(rightPanel, c);

    }

    public void initializeBottomPanel(Panel mainPanel){


        GridBagConstraints c;

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBackground(new Color(69,62,42,255));
        Dimension dimensions = new Dimension(477,150);
        bottomPanel.setPreferredSize(dimensions);




        worldHopLabel = new JLabel("Mass World Hopper");
        worldHopLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.PAGE_END;


        bottomPanel.add(worldHopLabel,c);





//        String[] worlds = {"World 342 F2P 1520", "World 333 MEM 1063",
//                "World 342 F2P 1520", "World 333 MEM 1063","World 342 F2P 1520",
//                "World 333 MEM 1063"};
        worldHopLM = new DefaultListModel();
        worldHopperList = new JList(worldHopLM);
        worldHopperList.setVisibleRowCount(4);

        scrollPaneHopper = new JScrollPane(worldHopperList);

        scrollPaneHopper.setPreferredSize(new Dimension(140,75));


        //scrollPaneHopper.add(worldHopper);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.PAGE_START;

        bottomPanel.add(scrollPaneHopper,c);

        hopWorldBtn = new JButton("Hop");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridheight = 1;
        c.gridwidth =1;
        bottomPanel.add(hopWorldBtn,c);

        refreshWorldBtn = new JButton("Refresh");

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        c.gridheight = 1;
        c.gridwidth =1;
        bottomPanel.add(refreshWorldBtn,c);


        // Refreshes list of f2p worlds
        refreshWorldBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Worlds worlds = getWorlds();
                for(int i = 0; i<worlds.f2p().size(); i++){
                    worldHopLM.insertElementAt(worlds.f2p().get(i),worldHopLM.size());
                }
            }
        });



        logout = new JButton("Logout");

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridheight = 3;
        bottomPanel.add(logout,c);


        logoutAll = new JButton("Logout All");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = .15;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        c.gridheight = 3;


        // c.anchor = GridBagConstraints.LINE_START;

        bottomPanel.add(logoutAll,c);



        // Entire bottom panel constraints
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        //c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(15,0,0,0);

        mainPanel.add(bottomPanel,c);

    }

    public void initializeSettingsPanel(Panel settingsPanel){

        GridBagConstraints c; // Constraints for the different components
        Dimension textboxDimensions = new Dimension(120,20);


        // Set the Melee Weapon
        setMeleeWeaponLbl = new JLabel("Set Melee Weapon");
        setMeleeWeaponLbl.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,10,10);

        settingsPanel.add(setMeleeWeaponLbl,c);


        meleeField = new JTextField();

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0,0,10,10);

        meleeField.setPreferredSize(textboxDimensions);

        settingsPanel.add(meleeField,c);


        saveMeleeWeapon = new JButton("Save");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(saveMeleeWeapon,c);



        // Set the range weapon
        setRangeWeaponLbl = new JLabel("Set Range Weapon");
        setRangeWeaponLbl.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0,0,10,10);

        settingsPanel.add(setRangeWeaponLbl,c);


        RangeField = new JTextField();

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(0,0,10,10);

        RangeField.setPreferredSize(textboxDimensions);

        settingsPanel.add(RangeField,c);


        saveRangeWeapon = new JButton("Save");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(saveRangeWeapon,c);



        // Set the Mage weapon
        setMageWeaponLbl = new JLabel("Set Mage Weapon");
        setMageWeaponLbl.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0,0,10,10);

        settingsPanel.add(setMageWeaponLbl,c);


        MageField = new JTextField();

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(0,0,10,10);

        MageField.setPreferredSize(textboxDimensions);

        settingsPanel.add(MageField,c);


        saveMageWeapon = new JButton("Save");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(saveMageWeapon,c);


        // Set Food
        setFoodLbl = new JLabel("Set Food Weapon");
        setFoodLbl.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0,0,10,10);

        settingsPanel.add(setFoodLbl,c);


        FoodField = new JTextField();

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(0,0,10,10);

        FoodField.setPreferredSize(textboxDimensions);

        settingsPanel.add(FoodField,c);


        saveFood = new JButton("Save");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 3;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(saveFood,c);


        // Set/Add potions
        setPotionLbl = new JLabel("Add Potion to List");
        setPotionLbl.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(0,0,10,10);

        settingsPanel.add(setPotionLbl,c);


        PotionField = new JTextField();

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.insets = new Insets(0,0,10,10);

        PotionField.setPreferredSize(textboxDimensions);

        settingsPanel.add(PotionField,c);

        savePotion= new JButton("Save");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(savePotion,c);



        // Add Spells to use
        setSpellLbl = new JLabel("Add Spell");
        setSpellLbl.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.insets = new Insets(0,0,10,10);

        settingsPanel.add(setSpellLbl,c);


        spellField = new JTextField();

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.insets = new Insets(0,0,10,10);

        spellField.setPreferredSize(textboxDimensions);

        settingsPanel.add(spellField,c);


        saveSpell= new JButton("Save");

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 5;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(saveSpell,c);




        // To Deal with autocasting/manual casting
        autoCast = new Checkbox("Auto Cast Enabled: ");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(autoCast,c);


        // Put list in scrollPane, then put that in the outer GridBagLayout pane like the other components
        // Potion List. SpellListModel is the same ListModel used in both settings and main tab. Doing this allows
        // the spells to updated on both simotaneously
        spellList = new JList(spellListModel);
        spellList.setVisibleRowCount(4);
        spellList.setPreferredSize(new Dimension(125,75));


        spellScrollPane = new JScrollPane(spellList);
        spellScrollPane.setPreferredSize(new Dimension(125,75));

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 7;
        c.gridheight = 1;
        c.insets = new Insets(10,0,10,0);


        settingsPanel.add(spellScrollPane,c);

        saveAutoCast = new JButton("Save Autocast Spell");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 8;
        c.insets = new Insets(0,0,5,0);

        settingsPanel.add(saveAutoCast,c);

        deleteSpell = new JButton("Delete Spell");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 9;
        c.insets = new Insets(0,0,10,0);

        settingsPanel.add(deleteSpell,c);

    }

    public void onExit(){
        if(myFrame!= null)
            myFrame.dispatchEvent((new WindowEvent(startJFrame, WindowEvent.WINDOW_CLOSING)));
        if(startJFrame != null)
            startJFrame.dispatchEvent(new WindowEvent(startJFrame, WindowEvent.WINDOW_CLOSING));
    }
}
