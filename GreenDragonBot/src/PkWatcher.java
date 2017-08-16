import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.WorldType;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.MethodProvider.sleep;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;

/**
 * Created by Ben on 7/26/2017.
 */
public class PkWatcher implements Runnable {

    private Player localPlayer;
    private MethodContext context;
    private String status;
    private List<Player> playersList;
    private HashMap<String,Boolean> threatMap;
    private Object combatLock;

    private Thread mainThread;
    private Thread pkWatcherThread;
    private InterruptFlag interruptFlag;
    private ScriptManager scriptManager;

    private WidgetChild wildernessWidget;


    private Random rand;

    public PkWatcher(Player localPlayer, MethodContext context, Object combatLock, InterruptFlag interruptFlag) {

        this.localPlayer = localPlayer;
        this.context = context;
        this.combatLock = combatLock;
        this.mainThread = mainThread;
        this.pkWatcherThread = Thread.currentThread();
        this.interruptFlag = interruptFlag;

        wildernessWidget = context.getWidgets().getWidget(90).getChild(46);
        threatMap = new HashMap<>();

        rand = new Random(System.currentTimeMillis());
        scriptManager = context.getClient().getInstance().getScriptManager();

//        deathWatchThread = new Thread(new DeathWatcher());
//        deathWatchThread.start();


    }

    @Override
    public void run(){




        log("start of run");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // returns a list of players that can attack local player
        //playersList = (ArrayList)context.getPlayers().all(player -> player!= null && canAttack(player) && player.getName().equals(localPlayer.getName()));


        while(Main.getStatus().equals("Attacking Dragons") || Main.getStatus().equals("Walking to Dragons") && !Thread.interrupted() && scriptManager.isRunning()) {

            playersList = (context.getPlayers().all(player -> canAttack(player) && threatMap.getOrDefault(player.getName(), true)
                    && !player.getName().equals(localPlayer.getName())));

            // do nothing, let it sleep after the else
           // log("player list is empty");
            if (playersList.isEmpty()) {
                try {
                    Thread.sleep(rand.nextInt(500) + 300);
                    continue; // go to next interation see if there are any new pkers in the area23
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }


            // Skulled
            else {
                //log("Checking skulled " + playersList.size());
                for (int i = 0; i < playersList.size(); i++) {
                    if (playersList.get(i).isSkulled()) {
                        escapeTeleport();
                        return;
                    }
                }
            }

//            // Remove non-threats off list and add their safe status to threatMap for quick access
//            log("removing non threats off list " + playersList.size());
//            for (int i = 0; i < playersList.size(); i++) {
//                for (int j = 0; j < greenDragList.size(); j++) {
//                    if (playersList.get(i).isInteracting(greenDragList.get(i))) {
//                        threatMap.put(playersList.get(i).getName(), false); // Player is not a threat. Interacting with dragon
//                        playersList.remove(i);
//                        i--; // i will advance next iteration but everything is moved back since the element is removed. i-- to adjust
//                        break;
//                    }
//
//                }
//
//            }


            // Not skulled, can attack, not interacting with dragons..
            // Will wait to player interacts with localPlayer or another dragon.. Will timeout after ~7 seconds

            for (int i = 0; i < playersList.size(); i++) {
                Player player = playersList.get(i);
               // log("before sleeping to wait for interaction with Player: " + player.getName());
                if(MethodContext.sleepUntil(() -> player.isInteractedWith() || player.isInteracting(localPlayer), rand.nextInt(8000) + 6500)){
                    log("After waiting for player to interact with something");
                    if (player.isInteracting(localPlayer)) {
                        escapeTeleport();
                        return;
                    }
//                    else if (player.isInteractedWith()) { // Interacting with something other than me, not a threat
//                        threatMap.put(player.getName(), false);
//
//                    }
                }

            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

        }



    }


    public boolean canAttack(Player player){



        int wildernessLevel = Integer.parseInt(wildernessWidget.getText().substring(wildernessWidget.getText().indexOf(" ")+1, wildernessWidget.getText().length()));

        int myCombatLevel = localPlayer.getLevel();
        int enemyCombatLvl = player.getLevel();

        int minLevelCanAttack = myCombatLevel-wildernessLevel;
        int maxLevelCanAttack = myCombatLevel+wildernessLevel;

        if(enemyCombatLvl >= minLevelCanAttack && enemyCombatLvl <= maxLevelCanAttack){
            return true;
        }
        else
            return false;
    }


    /**
     * Tries to teleport away, if it fails and is teleblocked.. it will try to escape run.. aka run away to the bank.
     *
     */
    public void escapeTeleport() {



        // For teleporting
        WidgetChild gloryWidget;
        Mouse mouse = context.getMouse();
        Menu gloryMenu = new Menu(context.getClient());

        log("Before escape tele interrupt");
        if (Main.getStatus().equals("Attacking Dragons") || Main.getStatus().equals("Walking to Dragons"))
            interruptFlag.interrupt(); // As to interrupt the flow of execution of the code.
        else
            return;
        synchronized (combatLock) {
            Main.setSubStatus("Anti-PK: Escaping");
            log("should teleport");
            // Tele to edge
            context.getEquipment().open();
            context.sleepUntil(() -> Tab.EQUIPMENT.isOpen(context.getClient()), rand.nextInt(7500) + 5000);
            while (scriptManager.isRunning() && context.getEquipment().getIdForSlot(EquipmentSlot.AMULET.getSlot()) >= Constants.GLORY[0]
                    && !Constants.EDGE_TELE_AREA.contains(localPlayer)) {
                log("inside tele");

                gloryWidget = context.getWidgets().getWidget(387).getChild(8).getChild(2);
                mouse.click(gloryWidget.getRectangle(), true);
                sleepUntil( () -> gloryMenu.isMenuVisible(), rand.nextInt(500) + 500);
                gloryMenu.clickAction("Edgeville");

                if (isTeleblocked()) {
                    log("is teleblocked");
                    if(escapeRun())
                        return; // made it out alive
                    if (isDead()) {
                        log("is teleblocked and dead");
                        return;
                    }

                }
                log("after sdkfksdmfklsdmf");
                if (context.sleepUntil(() -> localPlayer.getAnimation() == 714, rand.nextInt(500) + 500)) {
                    context.sleepUntil(() -> Constants.EDGE_TELE_AREA.contains(localPlayer), rand.nextInt(2500) + 5000);
                }
            }
                log("Before waiting till player arrives in edgeVille in EscapeTeleport");
                // Wait till player arrives in Edge then open back up inventory.. reset!
                if (!context.sleepUntil(() -> Constants.EDGE_TELE_AREA.contains(localPlayer), rand.nextInt(7500) + 5000)) {
                    scriptManager.stop();
                    context.getTabs().logout();
                    log(" Used up all tele's and didn't tele to right place");
                    return;
                }
                context.getTabs().open(Tab.INVENTORY);
                log("After waiting till player arrives in edge");
                context.sleep(rand.nextInt(2000) + 1500);
                worldHop();




        }
    }


    public boolean isDead() {
        if (Constants.LUMBRIDGE_SPAWN.contains(localPlayer))
            return true;
        else
            return false;
    }


    public boolean isTeleblocked(){
        log("checking teleblock" + context.getWidgets().getWidgetChild(162,43,0).getText());
        if(context.sleepUntil(() -> context.getWidgets().getWidgetChild(162,43,0).getText().contains("A teleport") ||
                context.getWidgets().getWidgetChild(162,43,0).getText().contains("A teleblock"), rand.nextInt(1500)+ 750)){
            log("checking teleblock" + context.getWidgets().getWidgetChild(162,43,0).getText());
            return true;
        }
        else
            return false;
    }


    public boolean escapeRun() {
        context.getTabs().open(Tab.INVENTORY);
        walkToEdge();
        crossDitch();
        walkToEdge();

        if(isDead())
            return false;
        else
            return true;

    }

    public void walkToEdge(){
        Tile tile = Constants.EDGEVILL_BANK.getRandomTile();
        while(!isDead() && scriptManager.isRunning() && !Constants.EDGEVILL_BANK.contains(localPlayer)){
            log("Running away");
            context.getWalking().walk(tile);
            context.sleep(2000);
            if (!context.getWalking().isRunEnabled() && context.getWalking().getRunEnergy() > 10) {
                context.getWalking().toggleRun();
            }
            if (context.getSkills().getBoostedLevels(Skill.HITPOINTS) / context.getSkills().getRealLevel(Skill.HITPOINTS) <= .65) {
                final int tempBoostedHP = context.getSkills().getBoostedLevels(Skill.HITPOINTS);
                log("should eat");
                if(context.getInventory().contains(Constants.LOBSTER)) {
                    Item food = context.getInventory().get(Constants.LOBSTER);
                    food.interact("Eat");
                }
                context.sleepUntil(() -> tempBoostedHP < context.getSkills().getBoostedLevels(Skill.HITPOINTS),  1000);
            }
            if(context.getGameObjects().closest(23271) != null && context.getGameObjects().closest(23271).isOnScreen() &&
                    context.getLocalPlayer().getTile().getY() > 3520){
                log("ditch exists. and on screen");
                break;
            }
            else
                log("Ditch does not exist");
        }
    }

    public void crossDitch(){
        while(scriptManager.isRunning() && localPlayer.getTile().getY() != 3520){
            context.getGameObjects().closest(23271).interact();
            context.sleepUntil(() -> localPlayer.getTile().getY() == 3520, 3000);

        }
    }


    public void worldHop(){
        Main.setStatus("Hopping Worlds");
        context.sleep(rand.nextInt(5000) + 3000); // bc can't hop right after combat
        while(!context.getWorldHopper().hopWorld(new Worlds().getRandomWorld(world -> world.isMembers() && !world.isHighRisk()
                && !world.isDeadmanMode() && !world.isPVP()))){
            log("Hopping");
        }
        log("after hop");
        context.sleep(rand.nextInt(5000)+ 3500);
        context.getTabs().open(Tab.INVENTORY);
    }

}
