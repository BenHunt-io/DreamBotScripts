import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.scene.effect.ImageInput;
import org.dreambot.api.javafx.*;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

/**
 * Created by Ben on 7/17/2017.
 */


@ScriptManifest(category = Category.MONEYMAKING, name = "RIDE OR DYE", author = "TheAppFoundry", version = 1.0)
public class Main extends AbstractScript {

    public String status;


    private Player localPlayer;

    ////// NPC's /////////
    private NPC aggie;
    private NPC banker;

    ////////////Walking related//////////

    private Area aggiesHouse;
    private Area draynorBank;


    //////// GUI ///////////

    private Image app_foundry_logo = null;
    private URL logoUrl;
    private InputStream logoIS;

    private URL logoTextUrl;
    private InputStream logoTextIS;
    private Image app_foundry_text = null;

    private Image scape_or_dye_logo = null;
    private URL scape_or_dye_URL;
    private InputStream scape_or_dye_IS;



    StrobeRectangle strobeRectangle;
    private double distance;


    // profit related
    private int profitPerDye;
    private int totalProfit = 0;
    private int profitPerHour = 0;
    private long startTime;
    private int newDyeTime;
    private int dyeCount = 0;
    private PriceLookup priceLookup;

    /////// WIDGETS ///////////
    Widgets widgets;
    WidgetChild clickToContinue;
    WidgetChild[] windowTwoContinues;


    @Override
    public void onStart() {


        initVars();

        initializeAreas();
        walkToStartingPos();


        super.onStart();
    }

    @Override
    public int onLoop() {

        bank();
        walkToAggies();
        makeDyes();
        walkToStartingPos();

        return 0;
    }


    @Override
    public void onPaint(Graphics g) {

        g.setColor(Constants.GUI_BACKGROUND);
        g.fillRect(6, 345, 508, 131);

        strobeRectangle.changeStrobeColor();
        g.setColor(strobeRectangle.strobeRectColor);
        g.drawRect(6, 345, 508, 131);

        g.setColor(Color.white);
        g.drawString("Current Status: " + status, 10, 360);
        g.drawString("Distance away from Map Marker: " + distance, 10, 375);
        g.drawString("Total Dyes Made: " + dyeCount, 10, 390);
        g.drawString("Profit made: " + totalProfit, 10, 405);
        profitPerHour(); // calculates profit/hour after each dye is made
        g.drawString("GP/Hour " + profitPerHour, 160, 405);


        g.setColor(strobeRectangle.strobeRectColor);
        g.fillOval(10, 411,56,56);

        g.drawString(strobeRectangle.strobeRectColor.getRed() + " " + strobeRectangle.strobeRectColor.getGreen() + "  " +
                strobeRectangle.strobeRectColor.getBlue(), 10, 10);


        g.drawImage(app_foundry_logo, 10, 410, (img,infoflags,x,y,width,height) -> false );
        g.drawImage(app_foundry_text,70, 410, (img,infoflags,x,y,width,height) -> false );
        g.drawImage(scape_or_dye_logo,6, 270, (img,infoflags,x,y,width,height) -> false );

        g.drawRect(aggiesHouse.getBoundingBox().x, aggiesHouse.getBoundingBox().y, 500, 500
        );


    }


    public void initializeAreas() {
        aggiesHouse = new Area(3083, 3259, 3088, 3256);
        draynorBank = new Area(3097, 3246, 3092, 3240);
    }

    public void walkToStartingPos() {
        getWalking().walk(draynorBank.getRandomTile()); // Walk to Draynor Bank

        status = "Walking to the bank";
        while (!draynorBank.contains(localPlayer)) {
            try {
                distance = getClient().getDestination().distance();
                if (distance < 3)
                    getWalking().walk(draynorBank.getRandomTile());
                Thread.sleep(100);
            } catch (NullPointerException NPE) {
                log("NPE: " + NPE);
                getWalking().walk(draynorBank.getRandomTile()); // if NPE is thrown, at minimap destination, contin. walk
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void initVars() {

        startTime = System.currentTimeMillis()/1000; // time since Jan 1, 1970 in seconds

        try {
            logoUrl = getClass().getResource("/Drawables/app_foundry_fitted_logo.png");
            logoIS = logoUrl.openStream();
            app_foundry_logo = ImageIO.read(logoUrl);

            logoTextUrl = getClass().getResource("/Drawables/app_foundry_logo.png");
            logoTextIS = logoTextUrl.openStream();
            app_foundry_text = ImageIO.read(logoTextIS);

            scape_or_dye_URL = getClass().getResource("/Drawables/scape_or_dye_logo.png");
            scape_or_dye_IS = scape_or_dye_URL.openStream();
            scape_or_dye_logo = ImageIO.read(scape_or_dye_IS);



        } catch (Exception e) {
            log(""+ e);
        }


        int OnionPrice = PriceLookup.getPrice(Constants.ONIONS);
        int yellowDyePrice = PriceLookup.getPrice(Constants.YELLOW_DYE);
        profitPerDye = yellowDyePrice - ((OnionPrice * 2) + Constants.GP_COST);
        totalProfit = 0;



        widgets = getWidgets();
        strobeRectangle = new StrobeRectangle();
        localPlayer = getLocalPlayer();
        status = new String("No current status");

    }

    public void bank() {
        while (localPlayer.isMoving()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        banker = getNpcs().closest("Banker");
        banker.interact("Bank");

        while (!getBank().isOpen()) {
            status = "Walking to Banker";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        status = "Banking";

        if(getInventory().contains(Constants.COINS) || getInventory().contains(Constants.ONIONS))
            getBank().depositAllExcept(Constants.COINS, Constants.ONIONS); // deposit all except Onions and Gold
        else
            getBank().depositAllItems();
        if (getBank().contains(Constants.COINS)) {
            getBank().withdrawAll(Constants.COINS);
        }
        if(getInventory().contains(Constants.COINS)){
            if(getInventory().count(Constants.COINS) < 135) stop();  // not enough coins
        }
        else {
            log("Stopping");
            stop();   // stop script if user has no coins in bank or inventory
        }

        if (getBank().contains(Constants.ONIONS)) {
            getBank().withdrawAll(Constants.ONIONS);
        } else {
            stop(); // out of onions stop script
        }


    }


    public void walkToAggies() {


        getWalking().walk(aggiesHouse.getRandomTile()); // Walk to Aggies

        status = "Walking to Aggie's house to make Dyes";
        while (!aggiesHouse.contains(localPlayer)) {
            try {
                distance = getClient().getDestination().distance();
                if (distance < 3) {
                    getWalking().walk(aggiesHouse.getRandomTile());
                    Thread.sleep(new Random(System.currentTimeMillis()).nextInt(1500)+500);
                    log("Walking random tile");
                }
                Thread.sleep(100);
            } catch (NullPointerException NPE) {
                log("NPE: " + NPE);
                getWalking().walk(aggiesHouse.getRandomTile()); // if NPE is thrown, at minimap destination, contin. walk
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void makeDyes() {

        int timer = 0;
        int onionCount = getInventory().count(Constants.ONIONS);

        log("making dyes");
        status = "Making dyes";

        aggie = getNpcs().closest(Constants.AGGIE);
        log("after getting Aggie");
        while (onionCount >= 2) {
            log("making dyes inside while");
            getInventory().get(Constants.ONIONS).useOn(aggie);
            while (!getDialogues().inDialogue()) {

                try {
                    Thread.sleep(100);
                    timer += 100;
                    if(timer > 2000){
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timer = 0;


            if (getDialogues().inDialogue()) {
                log("in dialog");
                while (clickToContinue == null) {
                    clickToContinue = widgets.getWidgetChild(217, 2); // Continue rectangle
                    sleep(100);
                    log("stuck in loop");
                }
                if(clickToContinue != null) {
                    clickToContinue.interact("Continue");
                    dyeCount++;
                    totalProfit += profitPerDye;
                    // Sleep until the 2 onions to make the dye dissapear
                    log("Before Onion Count check");
                    while(onionCount != getInventory().count(Constants.ONIONS) + 2){
                        try {
                            Thread.sleep(100);
                            if(timer > 2000){ // Times out
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timer += 100;
                    }
                    onionCount = getInventory().count(Constants.ONIONS); // update onion inventory count
                    getDialogues().spaceToContinue(); // Get out of the next screen
                    log("After onion check");
                    try {
                        Thread.sleep(new Random(1000).nextInt(500) + 250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }




        }

    }


    public void profitPerHour(){

        double elapsedTime = (System.currentTimeMillis()/1000) - startTime;

        //log(" " + elapsedTime + " " + elapsedTime / Constants.SECONDS_IN_HOUR);
        profitPerHour = (int)((double)totalProfit / (elapsedTime / Constants.SECONDS_IN_HOUR));

    }

    @Override
    public void onExit() {

        try {
            logoIS.close();
            logoTextIS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onExit();
    }
}
