/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awtswing;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Ben
 */
public class MultiBoxGui{
    
    private JFrame myFrame; // Main frame for the window
    // outermost layout placed in frame, other layouts placed in this
    private FlowLayout flowLayout; 
    // GridBagLayout panel will go inside flowlayout, will store left/top/bottom panels
    private Panel panel; 
    
    
    /**
     * Sets up the GUI. The JFrame, myFrame, has a flowLayout
     * set to it and then a panel is added which contains a GridBagLayout.
     * Everything in the GUI will go inside the panel with the GridBagLayout
     * 
     */
    public MultiBoxGui(){
        myFrame = new MyWindow();
        flowLayout = new FlowLayout();
        myFrame.setLayout(flowLayout);
        panel = new Panel();
        panel.setLayout(new GridBagLayout());
        myFrame.add(panel);
        
        
        myFrame.setLocationRelativeTo(null); // null = Center of client
        myFrame.setBounds(500,250,575,495); // Window size
        Container frameContainer = myFrame.getContentPane();
        frameContainer.setBackground(new Color(83,75,51,255)); // Light Brownish
        
        
        initializeMenu(myFrame);
        initializeLeftPanel(panel);
        initializeRightPanel(panel);
        initializeBottomPanel(panel);
//       
        
        myFrame.setVisible(true);
    }
    
    public class MyWindow extends JFrame{
        
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
        
        
        
        Label meleeLabel = new Label("Bronze sword");
        meleeLabel.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        
        rightPanel.add(meleeLabel,c);
        
        
        
        
        URL url = AWTSwing.class.getResource("/awtswing/Drawables/thesword.png");
        ImageIcon icon = new ImageIcon(url);
        
        URL url2 = AWTSwing.class.getResource("/awtswing/Drawables/thesword_clicked.png");
        ImageIcon icon2 = new ImageIcon(url2);
        
        JButton meleeButton = new JButton();
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
        
        
        
        Label rangeLabel = new Label("Willow shortbow");
        rangeLabel.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        
        rightPanel.add(rangeLabel,c);
        
        
        
        JButton rangeButton = new JButton();
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
        
        
        Label mageLabel = new Label("Staff of Air");
        mageLabel.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        
        rightPanel.add(mageLabel,c);
        
        
        JButton mageButton = new JButton();
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
        
        
        
        
        
        Label eatLabel = new Label("Trout");
        eatLabel.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10,0,0,0);
        rightPanel.add(eatLabel,c);
        
        
        JButton eatButton = new JButton();
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
        
        Label drinkLabel = new Label("Strength Potion");
        drinkLabel.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(10,0,0,0);
        
        rightPanel.add(drinkLabel,c);
        
        
        JButton drinkButton = new JButton();
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
        JList potionList = new JList(potionArray);
        potionList.setVisibleRowCount(4);
        
        JScrollPane potionScrollPane = new JScrollPane(potionList);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.gridheight = 2;
        c.insets = new Insets(10,0,0,0);
       
        
        rightPanel.add(potionScrollPane,c);
        
        
        
        Label specialAtkLabel = new Label("Special Attack");
        specialAtkLabel.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight= 1;
        
        rightPanel.add(specialAtkLabel,c);
        
        
        URL specialAtkUrl = AWTSwing.class.getResource("/awtswing/Drawables/special_attack_bar.png");
        ImageIcon specialAttackIcon = new ImageIcon(specialAtkUrl);
        
       
        JButton specialAttackBtn = new JButton(specialAttackIcon);
        specialAttackBtn.setBorder(BorderFactory.createEmptyBorder());
        specialAttackBtn.setContentAreaFilled(false);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 3;
        
        rightPanel.add(specialAttackBtn,c);
        
        
        
        
        URL tradeButtonUrl = AWTSwing.class.getResource("/awtswing/Drawables/trade_button.png");
        ImageIcon tradeButtonIcon = new ImageIcon(tradeButtonUrl);
        JButton tradeButton = new JButton(tradeButtonIcon);
        tradeButton.setBorder(BorderFactory.createEmptyBorder());
        tradeButton.setContentAreaFilled(false);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.gridheight = 3; // takes up two rows in height
        
        
        rightPanel.add(tradeButton,c);
        
        
        
        
        
        Label abandonShip = new Label("Abandon Ship");
        abandonShip.setForeground(Color.WHITE);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        
        
        rightPanel.add(abandonShip,c);
        
        
        
        
        
        URL abandonShipUrl = AWTSwing.class.getResource("/awtswing/Drawables/abandon_ship.png");
        ImageIcon abandonShipIcon = new ImageIcon(abandonShipUrl);
        JButton abandonShipButton = new JButton(abandonShipIcon);
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
