package Thebot;

import org.dreambot.api.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.prefs.Preferences;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 7/3/2017.
 */
public class MultiBoxGui {


    private JFrame startJFrame; // first window that pops up to the user to select a lead user

    private JFrame myFrame; // Main frame for the window
    // outermost layout placed in frame, other layouts placed in this
    private FlowLayout flowLayout;
    // GridBagLayout panel will go inside flowlayout, will store left/top/bottom panels
    private Panel panel;

    private Preferences preferences;

    ////////// Right Panel /////////////////

    public Label meleeLabel;
    public JButton meleeButton;

    public Label rangeLabel;
    public JButton rangeButton;

    public Label mageLabel;
    public JButton mageButton;

    public Label eatLabel;
    public JButton eatButton;

    public Label drinkLabel;
    public JButton drinkButton;
    public JList potionList;

    public Label specialAtkLabel;
    public JButton specialAttackBtn;

    public JButton tradeButton;

    public Label abandonShip;
    public JButton abandonShipButton;


    ////////// Left Panel /////////////////////




    /**
     * Sets up the GUI. The JFrame, myFrame, has a flowLayout
     * set to it and then a panel is added which contains a GridBagLayout.
     * Everything in the GUI will go inside the panel with the GridBagLayout
     * Also passes in preferences reference that was created in the main class
     */
    public MultiBoxGui(Preferences preferences){
        myFrame = new JFrame();
        flowLayout = new FlowLayout();
        myFrame.setLayout(flowLayout);
        panel = new Panel();
        panel.setLayout(new GridBagLayout());
        myFrame.add(panel);


        myFrame.setLocationRelativeTo(null); // null = Center of client
        myFrame.setBounds(500,250,511,518); // Window size
        Container frameContainer = myFrame.getContentPane();
        frameContainer.setBackground(new Color(83,75,51,255)); // Light Brownish

        this.preferences = preferences; // saves the preferences reference so we can write and read
    }


    /**
     * Starts first frame that pops up to the user. User selects Lead User before going to the main GUI. have to pass
     * in Client so we can set locationRelativeTo the client.
     */
    public void initStartFrame(Client client) {


        preferences.putBoolean("JFrameInstanceOpen", true);


        startJFrame = new JFrame("Specify Lead Player");
        startJFrame.setBounds(0, 0, 285, 75);
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

    public void displayMultiBoxGui(){
        initializeMenu(myFrame);
        initializeLeftPanel(panel);
        initializeRightPanel(panel);
        initializeBottomPanel(panel);


        myFrame.setVisible(true);
    }

    public void initializeMenu(JFrame myFrame){

        Menu main = new Menu("Main");
        Menu settings = new Menu("Settings");

        MenuBar menuBar = new MenuBar();
        menuBar.add(main);
        menuBar.add(settings);

        myFrame.setMenuBar(menuBar);
    }

    public void initializeLeftPanel(Panel panel){

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
        List multiboxedAccounts = new List();
        for(int i = 0; i<size; i++){
            multiboxedAccounts.add(accounts[i]);
        }


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(0,5,5,5);
        c.gridwidth = 2;

        leftPanel.add(multiboxedAccounts, c);



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



        Label inventoryListLabel = new Label("Recent Kill By: Zezima");
        inventoryListLabel.setForeground(Color.WHITE);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;

        leftPanel.add(inventoryListLabel,c);


        String[] inventoryItems = {"Bronze arrow", "Bronze sword", "RunePlatebody", "sword",
                "Bones", "Gold", "Cowhide"};

        JList inventoryList = new JList(inventoryItems);
        inventoryList.setVisibleRowCount(5);

        // Make the list scrollable by putting it in a JScrollPane
        JScrollPane inventoryScroll = new JScrollPane(inventoryList);


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.insets = new Insets(0,0,15,0);

        leftPanel.add(inventoryScroll,c);


        // Constraints for the whole left panel
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(15,0,0,15);
        panel.add(leftPanel, c);


    }

    public void initializeRightPanel(Panel panel){


        GridBagConstraints c;

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());;
        rightPanel.setBackground(new Color(69,62,42,255));



        meleeLabel = new Label("Bronze sword");
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



        rangeLabel = new Label("Willow shortbow");
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


        mageLabel = new Label("Staff of Air");
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





        eatLabel = new Label("Trout");
        eatLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
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
        c.gridx = 0;
        c.gridy = 3;
//        c.weightx =1.0;
//        c.weighty =1.0;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        rightPanel.add(eatButton,c);

        drinkLabel = new Label("Strength Potion");
        drinkLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 1;
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
        c.gridx = 1;
        c.gridy = 3;

//        c.weightx =1.0;
//        c.weighty =1.0;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        rightPanel.add(drinkButton,c);


        String[] potionArray = {"Strength Potion", "Attack Potion", "Defence Potion"
                , "Range Potion", "Mage Potion"};
        potionList = new JList(potionArray);
        potionList.setVisibleRowCount(4);

        JScrollPane potionScrollPane = new JScrollPane(potionList);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.gridheight = 2;
        c.insets = new Insets(10,0,0,0);


        rightPanel.add(potionScrollPane,c);



        specialAtkLabel = new Label("Special Attack");
        specialAtkLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight= 1;

        rightPanel.add(specialAtkLabel,c);


        URL specialAtkUrl = getClass().getResource("/Drawables/special_attack_bar.png");
        ImageIcon specialAttackIcon = new ImageIcon(specialAtkUrl);


        specialAttackBtn = new JButton(specialAttackIcon);
        specialAttackBtn.setBorder(BorderFactory.createEmptyBorder());
        specialAttackBtn.setContentAreaFilled(false);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 3;

        rightPanel.add(specialAttackBtn,c);




        URL tradeButtonUrl = getClass().getResource("/Drawables/trade_button.png");
        ImageIcon tradeButtonIcon = new ImageIcon(tradeButtonUrl);
        tradeButton = new JButton(tradeButtonIcon);
        tradeButton.setBorder(BorderFactory.createEmptyBorder());
        tradeButton.setContentAreaFilled(false);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.gridheight = 3; // takes up two rows in height


        rightPanel.add(tradeButton,c);





        abandonShip = new Label("Abandon Ship");
        abandonShip.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;


        rightPanel.add(abandonShip,c);



        System.out.println("test");
        URL abandonShipUrl = getClass().getResource("/Drawables/abandon_ship.png");
        ImageIcon abandonShipIcon = new ImageIcon(abandonShipUrl);
        abandonShipButton = new JButton(abandonShipIcon);
        abandonShipButton.setBorder(BorderFactory.createEmptyBorder());
        abandonShipButton.setContentAreaFilled(false);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 5;
        c.gridheight = 2; // takes up two rows in height


        rightPanel.add(abandonShipButton,c);










        // Entire right panel constraints
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(15,0,0,0);


        panel.add(rightPanel, c);

    }

    public static void initializeBottomPanel(Panel panel){


        GridBagConstraints c;

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBackground(new Color(69,62,42,255));
        Dimension dimensions = new Dimension(477,150);
        bottomPanel.setPreferredSize(dimensions);




        Label worldHopLabel = new Label("Mass World Hopper");
        worldHopLabel.setForeground(Color.WHITE);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.PAGE_END;


        bottomPanel.add(worldHopLabel,c);





        String[] worlds = {"World 342 F2P 1520", "World 333 MEM 1063",
                "World 342 F2P 1520", "World 333 MEM 1063","World 342 F2P 1520",
                "World 333 MEM 1063"};
        JList worldHopperList = new JList(worlds);
        worldHopperList.setVisibleRowCount(4);
        JScrollPane scrollPaneHopper = new JScrollPane(worldHopperList);

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

        JButton hopWorldBtn = new JButton("Hop");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridheight = 1;
        c.gridwidth =2;
        bottomPanel.add(hopWorldBtn,c);



        JButton logout = new JButton("Logout");

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridheight = 3;
        bottomPanel.add(logout,c);


        JButton logoutAll = new JButton("Logout All");

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

        panel.add(bottomPanel,c);

    }
}
