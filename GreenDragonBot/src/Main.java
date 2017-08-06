import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.dreambot.api.methods.container.impl.bank.BankType;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.walking.pathfinding.impl.obstacle.impl.PassableObstacle;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.CustomWebPath;
import org.dreambot.api.methods.world.Location;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.WorldType;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.*;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.imageio.ImageIO;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Created by Ben on 7/21/2017.
 */
@ScriptManifest(category = Category.MONEYMAKING, name = "GreenDragonKilla", author = "Foundry", version = 11.3)
public class Main extends AbstractScript{


    private Player localPlayer;



    private long startTime; // End script if something is stuck.

    ////////// Banking //////////
    private Entity bankBooth;
    private Random randNumber;

    ///////// Inventory/Equipment /////////
    private Item gamesNecklace;
    private Equipment equipment;
    private List<Item> startingEquipment;
    private boolean lootBagIsFull;
    private boolean lootBagisEmpty;

    //////// MISC /////////////////
    private GameObject caveExit;
    PriceLookup priceLookup;
    private ScriptManager scriptManager; // To manage when the script stops..

    //////// GUI //////////////////
    StrobeRectangle strobeRectangle;
    private static String status;
    private double profitPerHour;
    private int totalProfit;
    Timer profitTimer;
    Timer calcTimeTimer;
    int totalTime;
    int seconds,minutes,hours;
    String formattedTime;
    DecimalFormat decimalFormat;

    Image almondImage;
    URL almondUrl;
    InputStream almondIS;

    private Character currentTarget;



    //////// Anti PK / Combat ///////
    private Object combatLock;
    private Thread pkThread;
    private PkWatcher pkWatcher; // Runnable
    private boolean diedAndRestarting;
    private InterruptFlag interruptFlag;



    @Override
    public void onStart() {

        // Bank all except varrock tab/games necklace
        initVars();
        super.onStart();
    }

    @Override
    public int onLoop() {



        try {
            banking();
            walkingToDragons();
            combatStage();

        }catch(InterruptedException e){
            log("Restarting Script because of interruption " + e.getMessage());
            synchronized (combatLock){
                log("in synchronized interrupted excetpion lock");
                if(isDead()){
                   diedAndRestarting();
                }
            }
            return 0;
        }
        catch (DeadException deadE){
            log("caught dead exception");
            diedAndRestarting();

        }

        return 0;
    }

    @Override
    public void onPaint(Graphics g) {

        strobeRectangle.changeStrobeColor();
        g.setColor(strobeRectangle.strobeRectColor);
        g.fillRect(6, 345, 508, 131);
        g.setColor(Color.WHITE);
        g.drawString("Current Status: " + status , 10, 360);

        g.drawString("Profit/Hr: " + profitPerHour, 10, 375);

        g.drawString("Total Profit: " + totalProfit, 10 , 390);

        g.drawString("Run Time: " + formattedTime, 10, 405);

        g.setColor(strobeRectangle.strobeRectColor.darker());
        g.setColor(new Color(255,255,255, 120));
        g.fillRect(5,408,230, 17);
        g.setColor(new Color(strobeRectangle.r, 0, 0, 255));
        g.drawString("Exclusive Version: ~~ Almond Butter ~~ ",10,420);

        // TODO make more effecient.. Like current Target
        g.drawImage(almondImage, currentTarget.getBoundingBox().x, currentTarget.getBoundingBox().y,
                (img,infoflags,x,y,width,height) -> false);

        super.onPaint(g);
    }

    public void initVars(){

        combatLock = new Object();
        equipment = new Equipment();
        if(!getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())){
            equipment.putEquipment("Ammo", getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()).getID());
            equipment.putEquipment("AmmoCount", getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()).getAmount());
            log(equipment.getStartingAmmoCount() + " starting amtttttt" );
        }
        startingEquipment = getCurrentEquipment();
        for(Item item: startingEquipment){
            log(item.getName());
        }
        strobeRectangle = new StrobeRectangle();
        randNumber = new Random(System.currentTimeMillis()); // random small amount of sleep to use
        localPlayer = getLocalPlayer();

        lootBagIsFull = false; // starts off empty..
        lootBagisEmpty = true; // starts off empty..

        profitPerHour = 0;
        totalProfit = 0;

        startTime = System.currentTimeMillis() / 1000;

        profitTimer = new Timer();
        profitTimer.scheduleAtFixedRate(new ProfitRunnable(), 0 , 5000);

        interruptFlag = new InterruptFlag(false);
        scriptManager = getClient().getInstance().getScriptManager();

        totalTime = 0;
        seconds = minutes = hours = 0;
        calcTimeTimer = new Timer();
        calcTimeTimer.schedule(new TimeTask(),0,1000);

        decimalFormat = new DecimalFormat("#.##");

        try {
            almondUrl = new URL("http://imgur.com/odtSeTV.png");
            almondIS = almondUrl.openStream();
            almondImage = ImageIO.read(almondUrl);

        } catch (Exception e) {
            log("luiill"+ e);
        }

        addWebNodes(); // for path to walk back if teleblocked
    }

    public void banking() throws InterruptedException, DeadException{

        boolean bankGlory = false;
        int gloryWithdrawled = -1;
        int currentAmmoAmt = 0;
        int ammoAmtToWithdrawal = 0;

        log("banking");
        status = "Banking";




        // if -1 then user started with no ammo.. Probably using melee
        if(equipment.getAmmo() != -1 && !getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())){
            currentAmmoAmt = getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()).getAmount();
            ammoAmtToWithdrawal = equipment.getStartingAmmoCount() - currentAmmoAmt;
        }

        bankBooth = getBank().getClosestBank(BankType.BOOTH);


        log("Before opening and shit");

        openBank();
        getBank().depositAllExcept(item -> item.getName().contains("Games")
                || item.getID() == Constants.LOBSTER || item.getID() == Constants.LOOTING_BAG ||
                item.getName().contains("Chaos") || item.getName().contains("Air"));
        eatBackToFull();
        bankGlory = checkGlory();
        openBank();

        log("AFter opening and shit");

        depositBagContents();




        // See if ran out of lobster/games necklace if not withdrawal some
        if(getBank().count(Constants.LOBSTER) > 20) {
            int lobstersInInv = getInventory().count(Constants.LOBSTER);
            if(15 - lobstersInInv > 0){
                getBank().withdraw(Constants.LOBSTER, 15 - lobstersInInv);
            }
            else if(lobstersInInv - 15 > 0){
                getBank().deposit(Constants.LOBSTER, lobstersInInv - 15);
            }
        }
        else{
            log("Ran out of lobster");
            stop();
            throw new InterruptedException();
        }

        if(!getInventory().contains(item -> item.getName().contains("Games")) &&
                getBank().contains(item -> item.getName().contains("Games"))) {


              for(int i = 0; i<Constants.GAME_CHARGES; i++){
                  if(getBank().contains(Constants.GAMES_NECKLACE[i])){
                      getBank().withdraw(Constants.GAMES_NECKLACE[i]);
                      int id = Constants.GAMES_NECKLACE[i];
                      sleepUntil(()-> getInventory().contains(id), 5000);
                      break;
                  }
              }

        }
        else if (!getInventory().contains(item -> item.getName().contains("Games")) &&
                    !getBank().contains(item -> item.getName().contains("Games"))) {
            log("Ran out of games necklaces");
            stop();
            throw new InterruptedException();
        }


        if(bankGlory){
            getBank().deposit(Constants.UNCHARGED_GLORY);
            for(int i = 0; i<Constants.GLORY_CHARGES; i++){
                if(getBank().contains(Constants.GLORY[i])){
                    getBank().withdraw(Constants.GLORY[i]);
                    gloryWithdrawled = Constants.GLORY[i];
                    break;
                }
                else{
                    log("Ran out of glories");
                    stop();
                    throw new InterruptedException();
                }
            }
        }


        log("Current Ammo amt: 1" + currentAmmoAmt + "  2: " + equipment.getAmmo() + " 3: " + equipment.getStartingAmmoCount());
        if(equipment.getAmmo() != -1 && currentAmmoAmt < equipment.getStartingAmmoCount()) {
            if (getBank().contains(equipment.getAmmo()))
                getBank().withdraw(equipment.getAmmo(), ammoAmtToWithdrawal);
            else {
                log("out of Ammo in bank");
                throw new InterruptedException();
            }
        }



        // Withdraw looting bag
        if(getBank().contains(Constants.LOOTING_BAG)){
            getBank().withdraw(Constants.LOOTING_BAG);
        }


        try {
            Thread.sleep(randNumber.nextInt(1000) + 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getBank().close(); // close bank


        // put new glory on if we had to bank the uncharged one
        if(bankGlory){
            getInventory().get(gloryWithdrawled).interact("Wear");
            sleepUntil(() -> !getInventory().contains(item -> item.getName().contains("glory")), randNumber.nextInt(7500) + 5000);
        }

        // putting the ammo back on
        if(equipment.getAmmo() != -1 && getInventory().contains(equipment.getAmmo())){
            getInventory().get(equipment.getAmmo()).interact("Wield");
            sleepUntil(() -> !getInventory().contains(equipment.getAmmo()), randNumber.nextInt(7500) + 5000);
        }


        if(!scriptManager.isRunning())
            throw new InterruptedException();



    }


    public void walkingToDragons() throws InterruptedException {

        localPlayer = getLocalPlayer(); // have to redo it every time hop worlds..

        status = "Walking to Dragons";
        // shouldn't be null
        log("test1");
        for(int i = 0; i < Constants.GAME_CHARGES; i++){
            if(getInventory().contains(Constants.GAMES_NECKLACE[i])){
                log("test");
                gamesNecklace =  getInventory().get(Constants.GAMES_NECKLACE[i]); // shouldn't be null
                break;
            }
        }

        gamesNecklace.interact("Rub");

        if(!sleepUntil(() -> getDialogues().inDialogue(), randNumber.nextInt(15000) + 10000)){
            stop();
            return; // timeout after 10 sec
        }
        getDialogues().chooseOption(3); // tele to corp
        log("test Aftet tele");

        if(!sleepUntil(() -> Constants.CORPORAL_BEAST.contains(localPlayer), randNumber.nextInt(15000) + 10000)){
            log(localPlayer.getTile().getX() + " "+ localPlayer.getTile().getY()  + " " );
            stop();
            log("test after stopping script");
            return; // timeout after 10 sec
        }
        log("in corp");
        sleep(randNumber.nextInt(3000) + 2000);


        caveExit = getGameObjects().closest(Constants.CAVE_EXIT);
        caveExit.interact("Exit");

        if(!sleepUntil( () -> getDialogues().inDialogue(), randNumber.nextInt(15000) + 10000)){
            stop();
            return; // timeout after 10 sec

        }
        getDialogues().chooseOption(1);

        sleepUntil(() -> !Constants.CORPORAL_BEAST.contains(localPlayer), randNumber.nextInt(15000) + 10000);

        pkWatcher = new PkWatcher(localPlayer,getClient().getMethodContext(), combatLock, interruptFlag);
        pkWatcher.setStatus(status);
        pkThread = new Thread(pkWatcher);
        pkThread.start();

        // TODO See if current walking method is effecient or not
        // sleep till walk to middle of GraveYard
//        while (!Constants.GRAVEYARD_AREA.contains(localPlayer)) {
//            if(!scriptManager.isRunning()){
//                throw new InterruptedException();
//            }
//            if(interruptFlag.interrupted()){
//                throw new InterruptedException();
//            }
//            try {
//                log("walking to Graveyard  " + getWalking().shouldWalk(5));
//                if(getWalking().shouldWalk(5)){
//                    log("after should walk and before walk");
//                    getWalking().walk(Constants.GRAVEYARD_AREA.getRandomTile());
//                    log("after walk before sleep");
//                        sleepUntil(() -> getWalking().getDestination() != null, randNumber.nextInt(3000) + 3500);
//                }
//                Thread.sleep(randNumber.nextInt(300) + 150);
//            } catch (InterruptedException e) {
//                log("" + e);
//            }
//            catch (NullPointerException npe){
//                log("Ignore Null Pointer while walking" + npe);
//            }
//            catch (Exception e){
//                log(e + "error");
//            }
//        }

        // sleep till walk to middle of Green Drag Area
        Tile randTile = Constants.GREEN_DRAG_AREA.getRandomTile();
        while (!Constants.GREEN_DRAG_AREA.contains(localPlayer)) {
            if(!scriptManager.isRunning()){
                throw new InterruptedException();
            }
            if(interruptFlag.interrupted()){
                throw new InterruptedException();
            }
            try {
                log("walking to Dragons");
                if(getWalking().shouldWalk(5)){
                    getWalking().walk(randTile);
                    if(getWalking().getDestination() != null) {
                        sleepUntil(() -> getWalking().getDestinationDistance() > 5, randNumber.nextInt(7000) + 5500);
                    }
                }
                Thread.sleep(randNumber.nextInt(300) + 150);
            } catch (InterruptedException e) {
                log("" + e);
            }
            catch (NullPointerException npe){
                log("Ignore Null Pointer while walking" + npe);
            }
        }



//        try {
//            PassableObstacle passableObstacle = new PassableObstacle("Cave exit", "Exit", new Tile(2964, 4382, 2),
//                    new Tile(2963, 4382, 2), new Tile(3206, 3681, 0));
//
//            passableObstacle.traverse(getClient().getMethodContext()); // exit corp cave
//            Thread.sleep(5000);
//        }catch(Exception e){
//            log(e + " ");
//        }






    }

    /**
     * Represents the combat stage of this bot where the bot is attacking dragons.
     * Each section is synchronized with combatLock which is also used in the PkWatcher Runnable (Thread).
     * If Pker is detected the normal combat should be put on pause and the escape should start.
     * @throws InterruptedException
     */

    public void combatStage() throws InterruptedException, DeadException {

        status = "Attacking Dragons";

        pkWatcher.setStatus(status);

        double totalHP = getSkills().getRealLevel(Skill.HITPOINTS);
        Item food;
        Tile lootTile = null;
        GroundItem[] groundItems;
        currentTarget = null;

        while (getInventory().contains(Constants.LOBSTER)) {



            // Use timerTask for timeouts
            while (!localPlayer.isInteractedWith() && getInventory().contains(Constants.LOBSTER) && !needAmmo()) {
                synchronized (combatLock) {
                    log("Not in combat yet");
                    if(isDead()){ // code could be paused at the combatlock, bc trying to tele. Might die trying
                        throw new DeadException();
                    }
                    if (interruptFlag.interrupted() || !scriptManager.isRunning()) {
                        InterruptedException interruptedException = new InterruptedException("Not in Combat");
                        throw interruptedException;

                    }
                    currentTarget = getNpcs().closest(npc -> !npc.isInCombat() && npc.getName().equals("Green dragon")
                                && !localPlayer.isInteractedWith());
                    final Character tempCurrentTarget = currentTarget;
                    if (currentTarget != null) {
                        log("before sleep checking if target is null");
                        currentTarget.interact("Attack");
                        sleepUntil(() -> localPlayer.isInteractedWith() || tempCurrentTarget.isInCombat(), randNumber.nextInt(7500) + 5000);
                    } else {
                        getWalking().walk(Constants.GREEN_DRAG_AREA.getRandomTile()); // If target is null walk around area to find drag
                        try {
                            log("before sleep after walk");
                            sleepUntil(() -> getWalking().getDestination() != null, randNumber.nextInt(7000) + 5500);
                        }catch(NullPointerException npe) {
                            log("Ignore NPE while waking" + npe);
                        }
                    }

                        Thread.sleep(randNumber.nextInt(350) + 200);
                }


            }


                while (localPlayer.isInteractedWith() && getInventory().contains(Constants.LOBSTER)) {
//                    if(equipment.getAmmo() != -1 && getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())){
//
//                    }

                    synchronized (combatLock) {
                        if(isDead()){
                            throw new DeadException(); // if dead it will restart all the scripts.
                        }
                        if (interruptFlag.interrupted()) {
                            log("thread was interrupted while interactedwith");
                            throw new InterruptedException();
                        }
                        if(!scriptManager.isRunning())
                            throw new InterruptedException();

                        currentTarget = getNpcs().closest(npc -> npc.isInteracting(localPlayer) && npc.isInCombat());
                        if ( currentTarget != null && !localPlayer.isInteracting(currentTarget)) {
                            currentTarget.interact("Attack");
                            Character tempTarget = currentTarget;
                            sleepUntil(() -> localPlayer.isInteracting(tempTarget), randNumber.nextInt(1500) + 1000);
                        }

                        if (getSkills().getBoostedLevels(Skill.HITPOINTS) / totalHP <= .65) {
                            final int tempBoostedHP = getSkills().getBoostedLevels(Skill.HITPOINTS);
                            log("should eat");
                            try {
                                food = getInventory().get(Constants.LOBSTER);
                                food.interact("Eat");
                            } catch (Exception e) {
                                log("" + e);
                            }
                            log(tempBoostedHP + "/" + getSkills().getBoostedLevels(Skill.HITPOINTS) + "");
                            sleepUntil(() -> tempBoostedHP < getSkills().getBoostedLevels(Skill.HITPOINTS), randNumber.nextInt(7500) + 5000);
                            log(tempBoostedHP + "/" + getSkills().getBoostedLevels(Skill.HITPOINTS) + "");
                        }
                            Thread.sleep(100);

                    }

            }




            log("Before looting");
            // LOOTING
            // shouldn't loot if ran out of food/ammo
            synchronized (combatLock) {
                if(isDead()){
                    throw new DeadException();
                }
                if (interruptFlag.interrupted() || !scriptManager.isRunning()) {
                    throw new InterruptedException();
                }
                if (getInventory().contains(Constants.LOBSTER) && !needAmmo()) {
                    // Try to loot
                    final Character tempCurrentTarget = currentTarget;
                    if(tempCurrentTarget != null) {
                        sleepUntil(() -> !tempCurrentTarget.exists(), randNumber.nextInt(5500) + 4500);
                        try {
                            lootTile = getGroundItems().closest(groundItem -> groundItem.getName().equals("Dragon bones")).getTile();
                            groundItems = getGroundItems().getGroundItems(lootTile);

                            for (GroundItem groundItem : groundItems) {
                                if(interruptFlag.interrupted()){
                                    throw new InterruptedException();
                                }
                                log("ground item " + groundItem.getName() + " Price: " + PriceLookup.getPrice(groundItem.getID()));
                                if (shouldLoot(groundItem)) {
                                    if(getInventory().isFull() && getInventory().contains(Constants.LOBSTER)){
                                        getInventory().get(Constants.LOBSTER).interact("Eat");
                                    }
                                    groundItem.interact("Take");
                                    sleepUntil(() -> !groundItem.exists(), randNumber.nextInt(7500) + 5000);

                                }
                            }
                            if (getInventory().contains(equipment.getAmmo())) {
                                getInventory().get(equipment.getAmmo()).interact("Wield");
                            }

                        } catch (NullPointerException npe) {
                            log("NPE while looting" + npe);
                        }
                    }
                }
            }


            log("before looting bag");
            if (getInventory().contains(Constants.LOOTING_BAG)) {
                useLootingBag();
                lootBagisEmpty = false;
            }

            if(isDead()){
                throw new DeadException(); // if dead it will restart all the scripts.
            }

            }

            log("Before tele'ing back");
            synchronized (combatLock) {
                if(isDead()){
                    throw new DeadException(); // if dead it will restart all the scripts.
                }
                if (interruptFlag.interrupted()) {
                    throw new InterruptedException();
                }
                if(!scriptManager.isRunning()){
                    throw new InterruptedException();
                }

                pkThread.interrupt(); // Don't need to scan for players after teleporting

                while(scriptManager.isRunning() && interruptFlag.interrupted())
                // Tele to edge
                getEquipment().open();
                // Failsafe.. It will keep on trying to open inventory until it times out
                while(sleepUntil(() -> !getEquipment().open(), randNumber.nextInt(7500) + 5000)){

                };
                getEquipment().getItemInSlot((EquipmentSlot.AMULET).getSlot()).interact("Edgeville");

                // Wait till player arrives in Edge then open back up inventory.. reset!
                sleepUntil(() -> Constants.EDGE_TELE_AREA.contains(localPlayer), randNumber.nextInt(7500) + 5000);
                getTabs().open(Tab.INVENTORY);
            }

        }


    // If something is taking longer than 10-15 seconds, there's a bug.. should stop script
    public boolean isTimedOut(double startTime){
        if((startTime - System.currentTimeMillis()) > randNumber.nextInt(15000) + 10000){
            return  true;
        }
        else
            return false;
    }






    public ArrayList<Item> getCurrentEquipment(){

        java.util.List<Item> equipmentListAll = getEquipment().all();
        ArrayList<Item> equipmentList = new ArrayList<>();
        for(Item item: equipmentListAll){
            if(item != null){
                equipmentList.add(item);
            }
        }

        return equipmentList;
    }


    public boolean needAmmo(){
        if (equipment.getAmmo() != -1 && getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())) {
            return true; // out of ammo
        }
        else return false; // Either has ammo left or not using ammo
    }



    @Override
    public void onExit() {
        log("On Exit");
        pkThread.interrupt();
        profitTimer.cancel();
        super.onExit();
    }


    public void useLootingBag(){

        // Dialog option 3 for 'All"
        if(getInventory().count(Constants.DRAGON_BONES) > 5 || getInventory().count(Constants.GREEN_DHIDE) > 5
                && !localPlayer.isInteractedWith() && !lootBagIsFull

            || (getInventory().isFull() && !lootBagIsFull && getInventory().contains(Constants.DRAGON_BONES))){
                depositIntoBag(Constants.DRAGON_BONES);
                if(!lootBagIsFull){
                    depositIntoBag(Constants.GREEN_DHIDE);
                }
        }

    }

    public boolean isLootingBagFull(){
       WidgetChild lootBagTextWidget = getWidgets().getWidgetChild(162,43,0);
        if(lootBagTextWidget.getText().contains("The bag's")){
            return true;
        }
        else
            return false;
    }
    public void depositIntoBag(int item){
        if(getInventory().contains(item)) {
            getInventory().get(item).useOn(Constants.LOOTING_BAG);
            sleepUntil(() -> getDialogues().inDialogue(), randNumber.nextInt(7500) + 5000);
            getDialogues().chooseOption(getDialogues().getOptionIndexContaining("All"));
            lootBagIsFull = isLootingBagFull(); // Update whether looting bag is full based on game text
        }
    }

    // Deposit Looting bag stuff
    public void depositBagContents() throws InterruptedException {

        if(getInventory().contains(Constants.LOOTING_BAG)){
            log("yes contains bag");
            if(!lootBagisEmpty){
                getInventory().get(Constants.LOOTING_BAG).interact("View");
                sleepUntil( () -> !getWidgets().getWidgetChild(15,10,0).getItem().getName().equals(""),
                        randNumber.nextInt(7500) + 5000);
                // Deposit all
                sleepUntil(() -> getWidgets().getWidgetChild(15,5).interact(), randNumber.nextInt(7500) + 5000);
                // Sleep until the first slot of loot bag has a name of "" which means the lootbag is empty.
                sleepUntil(() -> getWidgets().getWidgetChild(15,10,0).getItem().getName().equals("") , randNumber.nextInt(7500) + 5000);
                // Close loot bag window
                getWidgets().getWidgetChild(15,7).interact(); // close loot bag window
                lootBagisEmpty = true;
            }
        }
    }


    public boolean isDead(){
        if(Constants.LUMBRIDGE_SPAWN.contains(localPlayer))
            return true;
        else
            return false;
    }

    public void diedAndRestarting(){


        lootBagisEmpty = true;

        sleepUntil(() -> getInventory().contains(item -> item.getName().contains("glory")), 5000);
        getInventory().get(item -> item != null && item.getName().contains("glory")).interact("Rub");
        sleepUntil(() -> getDialogues().inDialogue(), randNumber.nextInt(7500) + 5000);
        getDialogues().chooseOption(1); // Teleport back to edgeville

        // Wait till player arrives in Edge then open back up inventory.. reset!
        sleepUntil(() -> Constants.EDGE_TELE_AREA.contains(localPlayer), randNumber.nextInt(7500) + 5000);
        sleep(3000);
        bankBooth = getBank().getClosestBank(BankType.BOOTH);


        while(!getBank().isOpen() && !Thread.interrupted()){


            bankBooth.interact("Bank");
            try {
                Thread.sleep(randNumber.nextInt(3500)  + 2500);
            } catch (InterruptedException e) {
                log("caught e while banking");
            }
        }

        getBank().depositAllItems(); // deposit all items.
        sleepUntil(() -> getInventory().isEmpty(), randNumber.nextInt(7500) + 5000);

        int fullSlotCount = 0;
        for(int i = 0; i < startingEquipment.size(); i++) {
            int tempCount = fullSlotCount;

            if (getBank().contains(startingEquipment.get(i).getID())) { // range ammo (need to account for the starting ammount amt)
                if(startingEquipment.get(i).getID() == equipment.getAmmo()){
                    getBank().withdraw(equipment.getAmmo(),equipment.getStartingAmmoCount());
                }
                else { // normal item
                    getBank().withdraw(startingEquipment.get(i).getID());
                    sleepUntil(() -> getInventory().fullSlotCount() > tempCount, randNumber.nextInt(7500) + 5000);
                }
            fullSlotCount++;
            }
            else {
                // should end script if out of equipment after dying
                stop();
                log("Out of starting equipment");
                return;
            }
        }

        getBank().close();
        sleepUntil(() -> !getBank().isOpen(), randNumber.nextInt(7500) + 5000);
        for(int i = 0; i < startingEquipment.size(); i++){
            int tempCount = fullSlotCount;
            getInventory().get(startingEquipment.get(i).getID()).interact();
            sleepUntil(()-> getInventory().fullSlotCount() < tempCount, randNumber.nextInt(7500) + 5000);
            fullSlotCount --;

        }

        worldHop(); // Change worlds

        return; // Done re equiping armor and should restart script..


    }



    // What items are valid to loot
    public boolean shouldLoot(GroundItem groundItem){
        if(PriceLookup.getPrice(groundItem.getID()) > 1000){
            totalProfit += PriceLookup.getPrice(groundItem.getID());
            return true;
        }
        if(groundItem.getID() == equipment.getAmmo())
            return true;
        if (PriceLookup.getPrice(groundItem.getID())*groundItem.getAmount() > 1000) {
            totalProfit += PriceLookup.getPrice(groundItem.getID());
            return true;
        }
        if(groundItem.getName().contains("Ensouled")){
            totalProfit += 9500; // need to change doesn't detect dragon head
            return true;
        }
        if(groundItem.getName().equals("Looting bag")){
            return true;
        }

        return false;
    }

    public void worldHop(){
        sleep(randNumber.nextInt(5000) + 3000); // bc can't hop right after combat
        while(!getWorldHopper().hopWorld(new Worlds().getRandomWorld(world -> world.isMembers() && !world.isHighRisk()
            && !world.isDeadmanMode() && !world.isPVP()))){
            log("Hopping");
        }
        sleep(randNumber.nextInt(5000)+ 3500);
        getTabs().open(Tab.INVENTORY);
    }


    public class ProfitRunnable extends TimerTask{

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis() / 1000;
            //log(currentTime + " " + startTime);
            //log(totalProfit + "  " + (currentTime - startTime) + " " + (((double)(currentTime - startTime)) / 3600));
            profitPerHour = totalProfit / ((double)(currentTime - startTime) / 3600);


        }
    }

    public void eatBackToFull(){
        log("In eat back to full");
        if(getSkills().getBoostedLevels(Skill.HITPOINTS) < getSkills().getRealLevel(Skill.HITPOINTS)){
            log("Before starting food count");
            int startingFoodCount = getInventory().count(Constants.LOBSTER);
            log("After starting food count");
            int amountToEat = 0;
            double currentHP = getSkills().getBoostedLevels(Skill.HITPOINTS);
            double realHP = getSkills().getRealLevel(Skill.HITPOINTS);
            amountToEat = (int) Math.ceil((realHP - currentHP)/12);
            if(startingFoodCount < amountToEat) {
                log("withdrawing");
                getBank().withdraw(Constants.LOBSTER, amountToEat);
                log("DOne withdrawing");
            }
            getBank().close();

            int foodCountBeforeEat = getInventory().count(Constants.LOBSTER);
            while(getInventory().count(Constants.LOBSTER)!= (foodCountBeforeEat - amountToEat)){
                getInventory().get(Constants.LOBSTER).interact();
                int currentFoodCount = getInventory().count(Constants.LOBSTER);
                sleepUntil(()-> getInventory().count(Constants.LOBSTER) == (currentFoodCount - 1),
                        randNumber.nextInt(2500) + 1500);
                sleep(randNumber.nextInt(600) + 500); // Account for tick

            }
        }
        if(getBank().isOpen()){
            getBank().close();
        }
    }

    public void openBank(){
        while(!getBank().isOpen() && scriptManager.isRunning()){
            log("Opening bank");
            bankBooth.interact("Bank");
            try {
                Thread.sleep(randNumber.nextInt(2250) + 1500);
            } catch (InterruptedException e) {
                log(""+e);
            }
        }
    }

    public class TimeTask extends TimerTask{

        @Override
        public void run() {
            totalTime++;
            hours = totalTime / 3600;
            minutes = (totalTime - (hours * 3600)) / 60;
            seconds = ((totalTime % 3600) %60);
            int[] timeArray = {hours,minutes,seconds};
            StringBuffer stringBuffer = new StringBuffer();
            for(int i = 0; i < 3; i++){
                if(timeArray[i] <= 9 && timeArray[i] > 0){
                    stringBuffer.append("0" + timeArray[i]);
                }
                else if(timeArray[i] == 0){
                    stringBuffer.append("00");
                }
                else if(timeArray[i] > 9){
                    stringBuffer.append(timeArray[i]);
                }

                if(i != 2){ // (it's seconds) shouldn't put colon after
                    stringBuffer.append(":");
                }
            }
            formattedTime = stringBuffer.toString();
            if(!scriptManager.isRunning()){
                calcTimeTimer.cancel();
            }
        }
    }

    public class PotionTask extends TimerTask{

        String potion;

        public PotionTask(String potion){
            this.potion = potion;
        }

        @Override
        public void run() {
            if(shouldDrink(potion)){

            }
        }
    }

    public boolean shouldDrink(String potion){

        for(int i = 0; i < Constants.NUM_POTS_SUPPORTED; i++){
            getSkills().getBoostedLevels(Skill.HITPOINTS)
        }

        if(getInventory().contains(potion) && )

        return false;
    }


    public void addWebNodes(){
        WebFinder webFinder = getWalking().getWebPathFinder();
        CustomWebPath dragPath  = new CustomWebPath(new Tile(3157,3702), new Tile(3149,3703), new Tile(3136,3702));
        dragPath.connectToEnd(3114);
        CustomWebPath treePath = new CustomWebPath(new Tile(3132,3694), new Tile(3130, 3681), new Tile(3127,3670), new Tile(3124,3662));
        treePath.connectToStart(dragPath.getEnd().getIndex(getClient().getMethodContext()));
        treePath.connectToEnd(3093);
        log(dragPath.getEnd().getIndex(getClient().getMethodContext()) + " ");
        webFinder.addCustomWebPath(dragPath);
        webFinder.addCustomWebPath(treePath);
    }

    public boolean checkGlory(){
        // Open Equipment, Dequip glory if uncharged, open back up inventory.
        if(!getEquipment().isSlotEmpty(EquipmentSlot.AMULET.getSlot())){
            if(getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot()).getID() == Constants.UNCHARGED_GLORY) {
                getEquipment().open();
                getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot()).interact("Remove");
                getTabs().open(Tab.INVENTORY);
                return true;
            }
        }
        return false;
    }

    static public void setStatus(String newStatus){
        status = newStatus;
    }


}