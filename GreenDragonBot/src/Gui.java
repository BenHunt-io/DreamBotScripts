import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.dreambot.api.Client;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManager;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/13/2017.
 */
// c b a c a a b b


public class Gui  {

    private ScriptManager scriptManager;
    private Object guiLock = new Object();
    private JFrame jframe;



    // Fields that will be saved into
    private double eatPercentage;
    private double drinkPercentage;
    private Potion potion;
    private Food food;
    private boolean lootClues;
    private int minLootGP;
    private int foodWithdrawAmt;
    private int potWithdrawAmt;

    // Components user interacts with to save options
    private JComboBox<String> foods;
    private JComboBox<String> potions;
    private JCheckBox lootClueBox;
    private JTextField minLootField;
    private JScrollBar eatScrollBar;
    private JScrollBar drinkScrollBar;
    private JTextField foodAmtField;
    private JTextField potAmtField;

    private JLabel drinkLabel;
    private JLabel eatLabel;


    public Gui() {
    }
    /**
     * Displays the entire gui
     */
    public void displayGui() throws InterruptedException  {
        // this = Instrinsic class lock (Gui's lock)
        jframe = new JFrame();
        jframe.setLayout(new FlowLayout());
        jframe.setSize(new Dimension(425,560));

        JPanel optionsPanel = new JPanel();

        attachMainPanel(jframe); // Attachs the mainPanel where all the components are to the Jframe

        jframe.pack();
        jframe.setVisible(true);

        synchronized (this) {
            log("Before wait ");
            this.wait();
            log("After wait");
        }

    }


    /**
     * Main Panel that has everything on it. It's a fixed size about 500x500px
     * @param jFrame
     */
    public void attachMainPanel(JFrame jFrame){

        Color lightGray = new Color(188,188,188, 255);
        Color borderBlue = new Color(29,114,249);
        Color transparent = new Color(255,255,255,0);

        //Font optionsFont = loadFont();
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        Border dashedborder = BorderFactory.createDashedBorder(Color.darkGray);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(dashedborder, "Options"));
        optionsPanel.setMinimumSize(new Dimension(310,250));



        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(400,400));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(borderBlue));
        GridBagConstraints c; // Constraints for the items

        ImageIcon gdkImageIcon = new ImageIcon();
        try {
            URL foundryGDKURL = new URL("http://imgur.com/KQDKBQL.png");
            Image foundryGDKImage = ImageIO.read(foundryGDKURL);
            gdkImageIcon.setImage(foundryGDKImage);
        } catch (MalformedURLException e) {
            log("URL Failed" + e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel foundryGDKLogo = new JLabel(gdkImageIcon);

        foundryGDKLogo.setBackground(lightGray);
        foundryGDKLogo.setForeground(Color.BLACK);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = .25;
        c.weightx = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.PAGE_START;
        mainPanel.add(foundryGDKLogo,c);

        String[] potionList = Constants.POTION_LIST;
        potions = new JComboBox<>(potionList);
        potions.setFont(Font.getFont(Font.SANS_SERIF));

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(5,10, 0, 0);

        optionsPanel.add(potions,c);


        String[] foodList = Constants.FOOD_LIST;
        foods = new JComboBox<>(foodList);


        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(5,10, 0, 0);

        optionsPanel.add(foods,c);


        // Potion amt to withdrawl
        JPanel potAmtPanel = new JPanel();
        potAmtPanel.setBackground(transparent);

        JLabel potAmtLabel = new JLabel("Potion Count");
        potAmtField = new JTextField(3);
        potAmtField.setText("2");


        potAmtPanel.add(potAmtLabel);
        potAmtPanel.add(potAmtField);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;

        optionsPanel.add(potAmtPanel,c);

        // Food amt to withdrawl
        JPanel foodAmtPanel = new JPanel();
        foodAmtPanel.setBackground(transparent);

        JLabel foodAmtLabel = new JLabel("Food Count");
        foodAmtField = new JTextField(3);
        foodAmtField.setText("20");


        foodAmtPanel.add(foodAmtLabel);
        foodAmtPanel.add(foodAmtField);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;

        optionsPanel.add(foodAmtPanel,c);



        // Loot clue checkbox
        lootClueBox = new JCheckBox();

        JLabel lootClueLabel = new JLabel("Loot Clues");

        JPanel checkBoxPanel = new JPanel(new FlowLayout());
        checkBoxPanel.setBackground(transparent);
        checkBoxPanel.add(lootClueLabel);
        checkBoxPanel.add(lootClueBox);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;

        optionsPanel.add(checkBoxPanel,c);




        //
        minLootField = new JTextField(5);
        minLootField.setText("1000");
        JLabel minLootLabel = new JLabel("Min Loot [GP]");

        JPanel minLootPanel = new JPanel(new FlowLayout());
        minLootPanel.setBackground(transparent);
        minLootPanel.add(minLootLabel);
        minLootPanel.add(minLootField);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;

        optionsPanel.add(minLootPanel,c);

        // User selects % at which to eat. JScroll bar and Jlabel is used
        eatLabel = new JLabel("Eat Percentage 0%");

        eatScrollBar = new JScrollBar(Adjustable.HORIZONTAL);
        eatScrollBar.setVisibleAmount(5);
        eatScrollBar.setMaximum(105);
        eatScrollBar.setPreferredSize(new Dimension(150,17));
        eatScrollBar.addAdjustmentListener(new EatAdjustmentListener());

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel foodPanel = new JPanel();
        foodPanel.add(eatLabel);
        foodPanel.add(eatScrollBar);
        foodPanel.setBackground(transparent);

        optionsPanel.add(foodPanel,c);

        // User selects % at which to drink
        drinkLabel = new JLabel("Drink percentage 0%");

        drinkScrollBar = new JScrollBar(Adjustable.HORIZONTAL);
        drinkScrollBar.setVisibleAmount(5);
        drinkScrollBar.setMaximum(105);
        drinkScrollBar.setPreferredSize(new Dimension(150,17));
        drinkScrollBar.addAdjustmentListener(new DrinkAdjustmentListener());

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel drinkPanel = new JPanel();
        drinkPanel.add(drinkLabel);
        drinkPanel.add(drinkScrollBar);
        drinkPanel.setBackground(transparent);


        optionsPanel.add(drinkPanel,c);




        // Start button and start panel (colored light gray)
        JButton startButton = new JButton("Start");

        startButton.addActionListener(e -> {
            synchronized (this){
                log("Start button was clicked ");
                saveOptions(); // saves all the user selections in GUI
                this.notify();
                log("After notify");
                jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING));
            }
        });
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;

        JPanel startPanel = new JPanel(new GridBagLayout());
        startPanel.setForeground(lightGray);
        startPanel.add(startButton,c);

        c = new GridBagConstraints();
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;

        mainPanel.add(startPanel,c);

        // Options panel constraints
        optionsPanel.setBackground(transparent);
        c = new GridBagConstraints();
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.CENTER;

        mainPanel.add(optionsPanel,c);

        jFrame.add(mainPanel);

    }


    /**
     * Loads and returns the font
     * @return
     */
    public Font loadFont(){
        // This font is < 35Kb.
        URL fontUrl = null;
        try {
            fontUrl = new URL("https://nofile.io/f/S8oY0LrbIx4/nidsans-webfont.ttf");
            HttpURLConnection httpURLConnection = (HttpURLConnection) fontUrl.openConnection();
            Font font = Font.createFont(Font.TRUETYPE_FONT, httpURLConnection.getInputStream());
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font;
        } catch (MalformedURLException e) {
           log(" " + e);
        } catch (FontFormatException e) {
            log(" " + e);
            e.printStackTrace();
        } catch (IOException e) {
            log(" " + e);
            e.printStackTrace();
        }


        return null;
    }


    /**
     * Cache all the values from the GUI
     */
    public void saveOptions(){
        // Get the potion from the GUI
        String potionName = (String) potions.getSelectedItem();
        log(potionName);
        for(int i = 0; i<Constants.POTIONS.length; i++){
            if(potionName.equals(Constants.POTIONS[i].getPotion())){
                this.potion = Constants.POTIONS[i];
            }
        }

        String foodName = (String) foods.getSelectedItem();
        log(foodName);
        for(int i = 0; i<Constants.FOODS.length; i++){
            if(foodName.equals(Constants.FOODS[i].getName())){
                this.food = Constants.FOODS[i];
            }
        }


        if(lootClueBox.isSelected())
            this.lootClues = true;

        if(!minLootField.getText().equals("")) {
            try {
                minLootGP = Integer.parseInt(minLootField.getText());
            }catch(NumberFormatException nfe){
                log("Number Format wrong.. Need to enter a number");
            }
            log("min " + minLootGP);
        }

        eatPercentage = ((double)eatScrollBar.getValue())/100;
        drinkPercentage =((double)drinkScrollBar.getValue())/100;

        if(!potAmtField.getText().equals("")) {
            try {
                potWithdrawAmt = Integer.parseInt(potAmtField.getText());
            }catch(NumberFormatException nfe){
                log("Number Format wrong.. Need to enter a number");
            }
        }

        if(!foodAmtField.getText().equals("")) {
            try {
                foodWithdrawAmt = Integer.parseInt(foodAmtField.getText());
            }catch(NumberFormatException nfe){
                log("Number Format wrong.. Need to enter a number");
            }
        }


    }

    public int getMinLootGP(){ return minLootGP;}
    public Food getFood(){ return food;}
    public Potion getPotion(){ return potion;}
    public boolean lootClue(){ return lootClues;}
    public double getEatPercentage(){ return eatPercentage;}
    public double getDrinkPercentage(){ return drinkPercentage;}
    public int getFoodWithdrawAmt() {return foodWithdrawAmt;}
    public int getPotWithdrawAmt() {return potWithdrawAmt;}


    public class DrinkAdjustmentListener implements AdjustmentListener{

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            drinkLabel.setText("Drink Percentage " + e.getValue() + "%");
            jframe.repaint();
        }
    }

    public class EatAdjustmentListener implements AdjustmentListener{

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            eatLabel.setText("Eat Percentage " + e.getValue() + "%");
            jframe.repaint();
        }
    }


}
