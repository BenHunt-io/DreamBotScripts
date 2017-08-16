import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.container.impl.bank.BankType;
import org.dreambot.api.methods.container.impl.equipment.*;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.CustomWebPath;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.*;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.dreambot.api.wrappers.widgets.Menu;

/**
 * Created by Ben on 7/21/2017.
 */
@ScriptManifest(category = Category.MONEYMAKING, name = "GreenDragonKilla", author = "Foundry", version = 14.5)
public class Main extends AbstractScript{


    private Player localPlayer;



    private long startTime; // End script if something is stuck.

    ////////// Banking //////////
    private Entity bankBooth;
    private NPC banker;
    private Random randNumber;

    ///////// Inventory/Equipment /////////
    private Item gamesNecklace;
    private Equipment equipment;
    private List<Item> startingEquipment;
    private boolean lootBagIsFull;
    private boolean lootBagisEmpty;
    private Timer potionTimer;

    //////// MISC /////////////////
    private GameObject caveExit;
    PriceLookup priceLookup;
    private ScriptManager scriptManager; // To manage when the script stops..
    private Mouse mouse; // Used for right clicking the amulet Widget
    private Menu menu; // Used for the menu when teleporting

    //////// Paint //////////////////
    StrobeRectangle strobeRectangle;
    private static String subStatus; // more specific.. Anti-ban.ect..
    private static String status; // General.. Banking/AttackingDragons/WalkingToDragons
    private double profitPerHour;
    private int totalProfit;
    Timer profitTimer;
    Timer calcTimeTimer;
    Timer xpTimer;
    int totalTime;
    int seconds,minutes,hours;
    String formattedTime;
    DecimalFormat decimalFormat;
    private int startAttackXP;
    private int startStrengthXP;
    private int startDefenceXP;
    private int startRangeXP;
    private int startHpXP;
    private ArrayList<TrainedSkills> activeSkills;
    TrainedSkills[] combatSkills;
    ////////GUI//////////////////
    Gui gui;

    Image backgroundGuiImage;
    URL backgroundGuiUrl;
    InputStream backgroundGuiIS;

    private Character currentTarget;



    //////// Anti PK / Combat ///////
    private Object combatLock;
    private Thread pkThread;
    private PkWatcher pkWatcher; // Runnable
    private boolean diedAndRestarting;
    private InterruptFlag interruptFlag;

    //////// Anti ban ////////////////
    private Timer antiBanTimer;
    AntiBan[] antiBans;
    private boolean antiBanWaiting;


    //////// Concurrency /////////////
    private ScheduledExecutorService scheduledES;
    private ScheduledFuture<?> scheduledFutureAB;




    @Override
    public void onStart() {

        gui = quickStartGui(); // starts the gui and returns a reference to the GUI class

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
            log("Restarting/Stopping Script from  " + e.getMessage());
            sleep(randNumber.nextInt(2000)+ 1250);
            synchronized (combatLock){
                log("in synchronized interrupted exception lock");
                if(isDead()){
                   diedAndRestarting();
                }
            }
            return 0;
        }
        catch (DeadException deadE){
            log("caught dead exception: " + deadE.getMessage());
            diedAndRestarting();

        }

        return 0;
    }

    @Override
    public void onPaint(Graphics g) {


        strobeRectangle.changeStrobeColor();
        g.setColor(strobeRectangle.strobeRectColor);
        g.fillRect(6, 345, 508, 131);

        // TODO make more effecient.. Like current Target
        g.drawImage(backgroundGuiImage,0,210, (img,infoflags,x,y,width,height) -> false);

        g.setColor(Color.WHITE);
        g.drawString("Status: " + status , 20, 370);

        g.drawString("Profit/Hr: " + (int)profitPerHour, 20, 402);

        g.drawString("Total Profit: " + totalProfit, 20 , 435);

        g.drawString("Run Time:   " + formattedTime, 170, 337);

        g.setColor(strobeRectangle.strobeRectColor.darker());
        g.setColor(new Color(255,255,255, 120));
        g.fillRect(18,452,230, 17);
        g.setColor(new Color(strobeRectangle.r, 0, 0, 255));
        g.drawString("Exclusive Version: ~~ Almond Butter ~~ ",20,465);

        g.setColor(Color.white);
        if(!activeSkills.isEmpty()){
            int x = 197, y = 390;
            for(TrainedSkills activeSkill: activeSkills){
                g.drawString("XP Gained in " + activeSkill.getSkill().toString().toLowerCase() + " :" + activeSkill.getGainedXP(),x,y);
                y+=15;
            }
        }

        g.setColor(new Color(168,0,2,100));
        g.fillRect(0,310, 130, 17);
        g.setColor(Color.YELLOW);
        g.drawString(subStatus , 10,322);






        super.onPaint(g);
    }

    public void initVars(){

        mouse = getMouse();
        menu = new Menu(getClient());

        combatLock = new Object();
        equipment = null; // If user doesn't start with ammo in slot, it will be null.
        if(!getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())){
            equipment = new Equipment(getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()).getID(),
                                    getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()).getAmount());
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
            backgroundGuiUrl = new URL("http://imgur.com/BCqbg0V.png");
            backgroundGuiIS = backgroundGuiUrl.openStream();
            backgroundGuiImage = ImageIO.read(backgroundGuiUrl);

        } catch (Exception e) {
            log("luiill"+ e);
        }

        addWebNodes(); // for path to walk back if teleblocked

        initStartingXP();
        xpTimer = new Timer();
        xpTimer.schedule(new XPTask(),randNumber.nextInt(2000) + 1000,5000);

//        antiBanTimer = new Timer();
//        antiBanTimer.schedule(new antiBanTask(), randNumber.nextInt(5000) + 3000, 7500);
        initAntiBan();
//        antiBanWaiting = false; // Used to see if a antiban task is currently waiting on a lock.

        scheduledES = Executors.newScheduledThreadPool(3);
        scheduledFutureAB = scheduledES.scheduleWithFixedDelay(new antiBanRunnable(),randNumber.nextInt(2000) + 1000
                ,randNumber.nextInt(2500) + 7500, TimeUnit.MILLISECONDS);


    }

    /**
     *
     * @throws InterruptedException - Usually used to stop/restart the script in various places.
     * @throws DeadException - Will execute code upon death, then restart script
     * @throws Exception - Currently to check for exceptions at the bank. If exception is found, end script.
     */
    public void banking() throws InterruptedException, DeadException{

        // Clear the interrupt flag in case one was thrown. Interrupt flag is to stop/start the script
        // If it already restarted, it shouldn't be set to true.
        interruptFlag.interrupted();


        synchronized (combatLock) {

            if (!scriptManager.isRunning()) {
                throw new InterruptedException();
            }
            if(isDead()){
                throw new DeadException(" caught dead while trying to bank");
            }

            boolean bankGlory;
            int gloryWithdrawled = -1;

            log("banking");
            status = "Banking";

            bankBooth = getBank().getClosestBank(BankType.BOOTH);

            log("Before opening and shit");
            if(!getInventory().isEmpty()) // don't need to open it the first time if there is nothing to deposit
                openBank();
            getBank().depositAllExcept(item -> item.getName().contains("Games")
                    || item.getID() == Constants.LOBSTER || item.getID() == Constants.LOOTING_BAG );
            eatBackToFull();
            bankGlory = checkGlory(); // returns whether or not should bank glory (also dequips)
            openBank();

            log("After final bank open");

            depositBagContents();



            if (!getInventory().contains(item -> item != null && item.getName().contains("Games")) &&
                    getBank().contains(item -> item != null && item.getName().contains("Games"))) {


                for (int i = 0; i < Constants.GAME_CHARGES; i++) {
                    if (getBank().contains(Constants.GAMES_NECKLACE[i])) {
                        getBank().withdraw(Constants.GAMES_NECKLACE[i]);
                        int id = Constants.GAMES_NECKLACE[i];
                        sleepUntil(() -> getInventory().contains(id), 5000);
                        break;
                    }
                }

            } else if (!getInventory().contains(item -> item.getName().contains("Games")) &&
                    !getBank().contains(item -> item.getName().contains("Games"))) {
                log("Ran out of games necklaces");
                stop();
                throw new InterruptedException();
            }



            // See if ran out of lobster if not withdrawal some
            // TODO Add user ability to change food amount
            int foodWithdrawAmt = gui.getFoodWithdrawAmt();
            if (getBank().count(gui.getFood().getId()) > foodWithdrawAmt) {
                int foodInInv = getInventory().count(gui.getFood().getId());
                if (foodWithdrawAmt - foodInInv > 0) {
                    getBank().withdraw(gui.getFood().getId(), foodWithdrawAmt - foodInInv);
                } else if (foodInInv - foodWithdrawAmt > 0) {
                    getBank().deposit(gui.getFood().getId(), foodInInv - foodWithdrawAmt);
                }
            } else {
                log("Ran out of lobster");
                stop();
                throw new InterruptedException();
            }

            // Withdraw potions in descending order of doses
            for(int i = 0; i<4; i++){
                if(getBank().contains(gui.getPotion().getPotionIds()[i])){
                    getBank().withdraw(gui.getPotion().getPotionIds()[i], gui.getPotWithdrawAmt());
                    log(gui.getPotWithdrawAmt() + " Pot withdraw");
                    break;
                }
            }



            if (bankGlory) {
                getBank().deposit(Constants.UNCHARGED_GLORY);
                for (int i = 0; i < Constants.GLORY_CHARGES; i++) {
                    if (getBank().contains(Constants.GLORY[i])) {
                        getBank().withdraw(Constants.GLORY[i]);
                        gloryWithdrawled = Constants.GLORY[i];
                        sleepUntil(() -> getInventory().contains(item -> item != null && item.getName().contains("glory")),
                                randNumber.nextInt(5000) + 3000);
                        break;
                    }
                }
                if (!getInventory().contains(item -> item != null && item.getName().contains("glory"))) {
                    log("Ran out of glories");
                    stop();
                    throw new InterruptedException();
                }
            }



            if(equipment != null) {
                log("Current Ammo amt: 1:  " + equipment.getCurrentAmmoCount(getClient().getMethodContext()) + "  2: " +
                        equipment.getAmmoStartCount() + " 3: ");
            }

            if (equipment != null && equipment.getCurrentAmmoCount(getClient().getMethodContext()) < equipment.getAmmoStartCount()) {
                if (getBank().contains(equipment.getAmmoId()))
                    getBank().withdraw(equipment.getAmmoId(), equipment.getWithdrawlAmt());
                else {
                    log("out of Ammo in bank");
                    stop();
                    throw new InterruptedException();
                }
            }


            // Withdraw looting bag
            if (getBank().contains(Constants.LOOTING_BAG)) {
                getBank().withdraw(Constants.LOOTING_BAG);
            }


            try {
                Thread.sleep(randNumber.nextInt(1000) + 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getBank().close(); // close bank


            // put new glory on if we had to bank the uncharged one
            if (bankGlory) {
                getInventory().get(gloryWithdrawled).interact("Wear");
                sleepUntil(() -> !getInventory().contains(item -> item.getName().contains("glory")), randNumber.nextInt(7500) + 5000);
            }

            // putting the ammo back on
            if (equipment != null && getInventory().contains(equipment.getAmmoId())) {
                getInventory().get(equipment.getAmmoId()).interact("Wield");
                sleepUntil(() -> !getInventory().contains(equipment.getAmmoId()), randNumber.nextInt(7500) + 5000);
            }


            if (!scriptManager.isRunning())
                throw new InterruptedException();


        }

        // Anti ban can happen here

    }


    public void walkingToDragons() throws InterruptedException {

        synchronized (combatLock) {
            localPlayer = getLocalPlayer(); // have to redo it every time hop worlds..

            status = "Walking to Dragons";


            for (int i = 0; i < Constants.GAME_CHARGES; i++) {
                if (getInventory().contains(Constants.GAMES_NECKLACE[i])) {
                    gamesNecklace = getInventory().get(Constants.GAMES_NECKLACE[i]); // shouldn't be null
                    break;
                }
            }

            if (gamesNecklace == null || !getInventory().contains(gamesNecklace)) {
                stop();
                throw new InterruptedException(" in Walking to dragons don't have gnecklace in inv");
            }


            while(scriptManager.isRunning()){
                gamesNecklace.interact("Rub");
                sleepUntil(() -> getDialogues().inDialogue(),randNumber.nextInt(5000) + 2500);
                getDialogues().chooseOption(3); // tele to corp
                if(sleepUntil(() -> getLocalPlayer().getAnimation() == 714,randNumber.nextInt(5000) + 2500)){
                    log("test Aftet tele");
                    break;
                }
            }


            if (!sleepUntil(() -> Constants.CORPORAL_BEAST.contains(localPlayer), randNumber.nextInt(15000) + 10000)) {
                log(localPlayer.getTile().getX() + " " + localPlayer.getTile().getY() + " ");
                stop();
                log("test after stopping script");
                throw new InterruptedException("Not in corp cave"); // timeout after 10 sec
            }
            log("in corp");
            sleep(randNumber.nextInt(3000) + 2000);





            // Exiting corp cave
            while(scriptManager.isRunning() && !getDialogues().inDialogue() && Constants.CORPORAL_BEAST.contains(localPlayer)) {
                caveExit = getGameObjects().closest(Constants.CAVE_EXIT);
                if(caveExit != null) {
                    log(caveExit.getName());
                    caveExit.interact();
                }
                sleepUntil(()-> getDialogues().inDialogue(), randNumber.nextInt(5000) + 5000);
                if(getDialogues().inDialogue())
                    getDialogues().chooseOption(1);
                sleepUntil(() -> !Constants.CORPORAL_BEAST.contains(localPlayer), randNumber.nextInt(2000) + 4000);
                log("Retrying exiting corp cave");
            }


            pkWatcher = new PkWatcher(localPlayer, getClient().getMethodContext(), combatLock, interruptFlag);
            pkThread = new Thread(pkWatcher);
            pkThread.start();


            // sleep till walk to middle of Green Drag Area
            Tile randTile = Constants.GREEN_DRAG_AREA.getRandomTile();
            while (!Constants.GREEN_DRAG_AREA.contains(localPlayer)) {
                if (!scriptManager.isRunning() || interruptFlag.interrupted()) {
                    throw new InterruptedException();
                }
                if (isDead()) {
                    throw new InterruptedException();
                }
                try {
                    log("walking to Dragons");
                    if (getWalking().shouldWalk(5)) {
                        getWalking().walk(randTile);
                        if (getWalking().getDestination() != null) {
                            sleepUntil(() -> getWalking().getDestinationDistance() > 5 ||
                                    Constants.GREEN_DRAG_AREA.contains(localPlayer), randNumber.nextInt(7000) + 5500);
                        }
                    }
                    Thread.sleep(randNumber.nextInt(300) + 150);
                } catch (InterruptedException e) {
                    log("" + e);
                } catch (NullPointerException npe) {
                    log("Ignore Null Pointer while walking" + npe);
                }
            }
        }




    }

    /**
     * Represents the combat stage of this bot where the bot is attacking dragons.
     * Each section is synchronized with combatLock which is also used in the PkWatcher Runnable (Thread).
     * If Pker is detected the normal combat should be put on pause and the escape should start.
     * @throws InterruptedException
     */

    public void combatStage() throws InterruptedException, DeadException {

        status = "Attacking Dragons";

        potionTimer = new Timer();
        potionTimer.schedule(new PotionTask(gui.getPotion()), 0 , 5000);


        double totalHP = getSkills().getRealLevel(Skill.HITPOINTS);
        Item food;
        Tile lootTile;
        GroundItem[] groundItems;
        currentTarget = null;

        while (getInventory().contains(Constants.LOBSTER)) {


            while (!localPlayer.isInteractedWith() && getInventory().contains(Constants.LOBSTER) && !needAmmo()) {

                synchronized (combatLock) {

                    log("before looting bag");
                    if (getInventory().contains(Constants.LOOTING_BAG)) {
                        useLootingBag();
                    }

                    log("Not in combat yet");
                    if(isDead()){ // code could be paused at the combatlock, bc trying to tele. Might die trying
                        throw new DeadException("Caught dead in NotInteractedWith block");
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
                        try {
                            sleepUntil(() -> localPlayer.isInteractedWith() || tempCurrentTarget.isInCombat(), randNumber.nextInt(7500) + 5000);
                        }catch(ConcurrentModificationException cme){
                            log(cme + " ");
                        }
                    } else if(getWalking().shouldWalk(5)) {
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

                Character tempTarget;
                while (localPlayer.isInteractedWith() && getInventory().contains(Constants.LOBSTER) && !needAmmo()) {

                    // So that if there is a new dragon to attack, we still can check to see if killed old one
                    tempTarget = currentTarget;

                    synchronized (combatLock) {
                        if(isDead()){
                            throw new DeadException(" Caught dead in InteractedWith Block"); // if dead it will restart all the scripts.
                        }
                        if (interruptFlag.interrupted() || !scriptManager.isRunning()) {
                            throw new InterruptedException("interactedWith block");
                        }

                        currentTarget = getNpcs().closest(npc -> npc.isInteracting(localPlayer) && npc.isInCombat());
                        if ( currentTarget != null && !localPlayer.isInteracting(currentTarget)) {
                            currentTarget.interact("Attack");
                            sleepUntil(() -> localPlayer.isInteracting(currentTarget), randNumber.nextInt(1500) + 1000);
                        }

                        if(currentTarget != null && currentTarget.getHitSplats()[0] > 0 && localPlayer.isInteracting(currentTarget)){
                            activeSkills = getTrainedSkills(combatSkills);
                        }

                        if (getSkills().getBoostedLevels(Skill.HITPOINTS) / totalHP <= gui.getEatPercentage()) {
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

                    if(tempTarget != null){
                        log(tempTarget.getHealthPercent() + " ");
                        log(tempTarget.exists() + " ");
                    }
                    Character target = tempTarget; // just for making an "Effectively final variable for lambda"
                    if(tempTarget != null && ((tempTarget.isHealthBarVisible() && tempTarget.getHealthPercent() == 0)
                            || !tempTarget.exists())){
                        sleepUntil(() -> !target.exists(), randNumber.nextInt(5500) + 4500);
                        log("dragon is dead");
                        sleepUntil(() -> getGroundItems().closest("Dragon bones") != null, randNumber.nextInt(500) + 500);
                        tempTarget = null;
                        break;
                    }


            }




            log("Before looting");
            // LOOTING
            // shouldn't loot if ran out of food/ammo
            synchronized (combatLock) {
                setSubStatus("Looting");
                if(isDead()){
                    throw new DeadException("Caught dead trying to loot");
                }
                if (interruptFlag.interrupted() || !scriptManager.isRunning()) {
                    throw new InterruptedException();
                }
                if (getInventory().contains(Constants.LOBSTER) && !needAmmo()) {

                    if(currentTarget != null && currentTarget.exists()){
                        sleepUntil(() -> !currentTarget.exists(), randNumber.nextInt(5500) + 4500);
                        sleepUntil(() -> getGroundItems().closest("Dragon bones") != null, randNumber.nextInt(500) + 500);
                    }
                    // Try to loot
                        try {
                            lootTile = getGroundItems().closest(groundItem -> groundItem.getName().equals("Dragon bones")).getTile();
                            groundItems = getGroundItems().getGroundItems(lootTile);

                            for (GroundItem groundItem : groundItems) {
                                if(interruptFlag.interrupted()){
                                    setSubStatus("");
                                    throw new InterruptedException(); // In case getting killed while looting
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
                            if (equipment != null && getInventory().contains(equipment.getAmmoId())) {
                                getInventory().get(equipment.getAmmoId()).interact("Wield");
                            }

                        } catch (NullPointerException npe) {
                            log("NPE while looting" + npe);
                        }
                    }

                setSubStatus("");
                }

            }


            log("Before tele'ing back");
            synchronized (combatLock) {
                if (interruptFlag.interrupted()) {
                    throw new InterruptedException();
                }
                if(!scriptManager.isRunning()){
                    throw new InterruptedException();
                }

                equipmentTeleport();

                if(isDead()){
                    throw new DeadException("Caught dead before teleing back - normal flow"); // if dead it will restart all the scripts.
                }
                // Wait till player arrives in Edge then open back up inventory.. reset!
                if(!sleepUntil(() -> Constants.EDGE_TELE_AREA.contains(localPlayer), randNumber.nextInt(7500) + 5000)){
                    stop();
                    getTabs().logout();
                    throw new InterruptedException(" Used up all tele's and didn't tele to right place");
                };
                getTabs().open(Tab.INVENTORY);
            }



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
        if (equipment != null && getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())) {
            return true; // out of ammo
        }
        else return false; // Either has ammo left or not using ammo
    }



    @Override
    public void onExit() {
        log("On Exit");
        if(profitTimer != null)
            profitTimer.cancel();
        if(potionTimer != null)
            potionTimer.cancel();
        if(pkThread != null)
            pkThread.interrupt();
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
            lootBagisEmpty = false;
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


        while(!getBank().isOpen() && !Thread.interrupted() && scriptManager.isRunning()){


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
                if(startingEquipment.get(i).getID() == equipment.getAmmoId()){
                    getBank().withdraw(equipment.getAmmoId(),equipment.getAmmoStartCount());
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
        if(PriceLookup.getPrice(groundItem.getID()) > gui.getMinLootGP()){
            totalProfit += PriceLookup.getPrice(groundItem.getID());
            return true;
        }
        if(equipment != null && groundItem.getID() == equipment.getAmmoId())
            return true;
        // Takes into account if there is stackable items
        if (PriceLookup.getPrice(groundItem.getID())*groundItem.getAmount() > gui.getMinLootGP()) {
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
        if(gui.lootClue() && groundItem.getName().contains("Clue")){
            return true;
        }

        return false;
    }

    public void worldHop(){
        setSubStatus("Hopping Worlds");
        // TODO find a better way to wait until out of combat
        sleep(randNumber.nextInt(5000) + 3000); // bc can't hop right after combat
        while(!getWorldHopper().hopWorld(new Worlds().getRandomWorld(world -> world.isMembers() && !world.isHighRisk()
            && !world.isDeadmanMode() && !world.isPVP() && !world.isLastManStanding() && !world.isTournamentWorld()))){
            log("Hopping");
        }
        log("After hopping");
        sleep(randNumber.nextInt(5000)+ 3500);
        setSubStatus("");
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
        if(!getBank().isOpen()){
            openBank();

        }
        setSubStatus("Eating back to full health");
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
            sleepUntil(() -> getInventory().get(Constants.LOBSTER) != null, randNumber.nextInt(7500) + 5000);;

            int foodCountBeforeEat = getInventory().count(Constants.LOBSTER);
            while(getInventory().count(Constants.LOBSTER)!= (foodCountBeforeEat - amountToEat) && getInventory().contains(Constants.LOBSTER)){
                if(getInventory().contains(Constants.LOBSTER)) {
                    getInventory().get(Constants.LOBSTER).interact();
                    int currentFoodCount = getInventory().count(Constants.LOBSTER);
                    sleepUntil(()-> getInventory().count(Constants.LOBSTER) == (currentFoodCount - 1),
                            randNumber.nextInt(1750) + 1500);
                }
                sleep(randNumber.nextInt(800) + 650); // Account for tick

            }
        }
        setSubStatus("");
        if(getBank().isOpen()){
            getBank().close();
        }
    }

    public void openBank(){
        while (!getBank().isOpen() && scriptManager.isRunning()) {
            log("Opening bank");
            if(bankBooth == null){
                bankBooth = getBank().getClosestBank(BankType.BOOTH);
                bankBooth.interact("Bank");
            } else
                bankBooth.interact("Bank");
            try {
                Thread.sleep(randNumber.nextInt(2250) + 1500);
            } catch (InterruptedException e) {
                log("" + e);
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

        Potion potion;

        public PotionTask(Potion potion){
            this.potion = potion;
        }

        @Override
        public void run() {
            //log("Checking shouldDrink");
            if(shouldDrink(potion)){
                synchronized (combatLock){
                    log("inside drink lock");
                    getInventory().get(item -> item != null && item.getName().contains(potion.getPotion())).interact();
                    sleepUntil(() -> getInventory().contains(229), randNumber.nextInt(5000) + 3000);
                    if(getInventory().contains(229))
                        getInventory().get(229).interact("Drop");
                }
            }

            if(!status.equals("Attacking Dragons")|| !scriptManager.isRunning() ||
            !getInventory().contains(item -> item != null && item.getName().contains("Combat"))){

                potionTimer.cancel(); // should not be running if not attacking dragons
            }

        }
    }


    public class XPTask extends TimerTask{

        @Override
        public void run() {
            log("xptask");
            activeSkills = getTrainedSkills(combatSkills);
            if(!activeSkills.isEmpty()) {
                for (TrainedSkills activeSkill : activeSkills) {
                    activeSkill.setCurrentXP(getSkills().getExperience(activeSkill.getSkill()));
                }
            }
            if(!scriptManager.isRunning())
                xpTimer.cancel();
        }
    }


    public class antiBanRunnable implements Runnable{

        @Override
        public void run() {
            log("anti ban");
            if(!scriptManager.isRunning()) {
                log("Cancelling");
                scheduledFutureAB.cancel(true);
            }
            if (!((randNumber.nextInt(99) + 1)  > 80)) {
                log("return from antiban");
                return;
            }

            sleep(randNumber.nextInt(2000), 3000);
            log("Before synch antiban");
            synchronized (combatLock) {
                log("running antiban");
                int antiBanIndex = 0; // default camera rotate
                double randomNum = Math.random();
                if(randomNum > .2 && randomNum < .35)
                    antiBanIndex = 1; // skills
                else if(randomNum > .45 && randomNum < .5)
                    antiBanIndex = 2; // friends
                antiBans[antiBanIndex].runAntiBan(randNumber);
                subStatus = "";
            }
        }
    }


    public boolean shouldDrink(Potion potion){

        int realLevel = getSkills().getRealLevel(potion.getSkill());
        int currentBoost = getSkills().getBoostedLevels(potion.getSkill());
        int totalBoost = potion.getTotalBoosted(realLevel);

        if(((double)(currentBoost - realLevel)) / totalBoost < gui.getDrinkPercentage() &&
                getInventory().contains(item -> item != null &&  item.getName().contains(potion.getPotion()))) {
            //log("yes should drink");
            return true; // less than 50% of original boost

        }
        else
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
                getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot()).interact(); // removes it
                sleepUntil(() -> getEquipment().isSlotEmpty(EquipmentSlot.AMULET.getSlot()),
                        randNumber.nextInt(2500) + 1000);
                getTabs().open(Tab.INVENTORY);
                return true;
            }
        }
        return false;
    }

    public ArrayList<TrainedSkills> getTrainedSkills(TrainedSkills[] combatSkills){

        ArrayList<TrainedSkills> tempActiveSkills = new ArrayList<>();
        for(TrainedSkills combatSkill: combatSkills){
            if(getSkills().getExperience(combatSkill.getSkill()) > combatSkill.getStartingXP()){
                tempActiveSkills.add(combatSkill);
            }
        }

        return tempActiveSkills;
    }

    public void initStartingXP(){
        combatSkills = new TrainedSkills[5];
        combatSkills[0] = new TrainedSkills(Skill.HITPOINTS,getSkills().getExperience(Skill.HITPOINTS));
        combatSkills[1] = new TrainedSkills(Skill.ATTACK,getSkills().getExperience(Skill.ATTACK));
        combatSkills[2] = new TrainedSkills(Skill.STRENGTH,getSkills().getExperience(Skill.STRENGTH));
        combatSkills[3] = new TrainedSkills(Skill.DEFENCE,getSkills().getExperience(Skill.DEFENCE));
        combatSkills[4] = new TrainedSkills(Skill.RANGED,getSkills().getExperience(Skill.RANGED));

    }
    static public void setStatus(String newStatus){
        synchronized (status) {
            status = newStatus;
        }
    }

    static public String getStatus(){
        synchronized (status) {
            return status;
        }
    }

    public void initAntiBan(){


        antiBans = new AntiBan[3];

        antiBans[0] = (r) -> {
            subStatus = "Anti-Ban [Cam]";
            getCamera().rotateTo(r.nextInt(2400) + 0, r.nextInt(getClient().getLowestPitch()));
        };
        antiBans[1] = (r) -> {
            subStatus = "Anti-Ban [Skills]";
            getSkills().hoverSkill(activeSkills.get(r.nextInt(activeSkills.size() - 1) + 0).getSkill());
            sleep(randNumber.nextInt(2000) + 1000);
            getTabs().open(Tab.INVENTORY);
    };

        antiBans[2] = (r) -> {
            subStatus = "Anti-Ban [Friends]";
            getTabs().open(Tab.FRIENDS);
            sleep(r.nextInt(2250) + 1250);
            getTabs().open(Tab.INVENTORY);
        };
    }


    public static void setSubStatus(String newStatus){
        subStatus = newStatus;
    }



    /**
     * Teleports to Edgeville using a glory. Uses the Widget and Mouse classes
     */
    public void equipmentTeleport(){
        setStatus("Teleporting Back");
        // Tele to edge
        getEquipment().open();
        // Failsafe.. It will keep on trying to open inventory until it times out
        // Probably not necessary.
        sleepUntil(() -> getEquipment().open(), randNumber.nextInt(7500) + 5000);

        while(scriptManager.isRunning() && getEquipment().getIdForSlot(EquipmentSlot.AMULET.getSlot()) >= Constants.GLORY[0]){
            log("inside tele");
            WidgetChild gloryWidget = getWidgets().getWidget(387).getChild(8).getChild(2);
            Rectangle EdgevilleRect = gloryWidget.getRectangle();
            EdgevilleRect.height -= 10;
            mouse.click(EdgevilleRect, true);
            sleepUntil(() -> menu.isMenuVisible(), randNumber.nextInt(500) + 500);
            mouse.click(menu.getIndexRectangle( menu.getIndex("Edgeville")));
            if(sleepUntil(() -> localPlayer.getAnimation() == 714, randNumber.nextInt(500) + 1000)) {
                if(sleepUntil(() -> Constants.EDGE_TELE_AREA.contains(localPlayer), randNumber.nextInt(2500)+5000))
                    return;
            }
        }
    }

    public Gui quickStartGui(){
        Gui gui = new Gui();
        try {
            gui.displayGui();
        } catch (InterruptedException e) {
            log("GUI was interrupted " + e);
        }
        return gui;
    }




}