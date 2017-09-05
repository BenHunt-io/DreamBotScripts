package Script;

import BotMuleAccounts.BotAccountList;
import BotMuleAccounts.BotNode;
import BotMuleCommunication.BotClient;
import BotMuleCommunication.BotRemoveClient;
import BotMuleCommunication.MuleRemoveServer;
import BotMuleCommunication.MuleServer;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ben on 8/22/2017.
 */
@ScriptManifest(category = Category.MONEYMAKING, name = "FoundrysSmelter", author = "Foundry", version = 3.0)
public class Main extends AbstractScript {

    private Bot smelterBot;
    private ScriptManager scriptManager;
    private Gui gui;



    ///////////Concurrency/Sockets//////////////
    private GuiInfo guiInfo;
    ScheduledExecutorService scheduledES;
    ScheduledFuture scheduledFuture;

    Timer botUpdater;
    MuleServer muleServer; // handles normal communication between bots
    MuleRemoveServer muleRemoveServer; // removes bots after they are finished running / script stops.



    @Override
    public void onStart() {
        super.onStart();

        gui = Gui.getInstance(); // instantiates the GUI

        MethodContext methodContext = getClient().getMethodContext();
        scriptManager = getClient().getInstance().getScriptManager();

        if(gui.isBot()) {

            guiInfo = GuiInfo.getInstance();
            GuiUpdater guiUpdater = new GuiUpdater(methodContext);
            scheduledES = Executors.newScheduledThreadPool(3);
            scheduledFuture = scheduledES.scheduleWithFixedDelay(guiUpdater, 0, 5000, TimeUnit.MILLISECONDS);
            smelterBot = new Bot(methodContext, guiInfo);

            botUpdater = new Timer();
            botUpdater.schedule(new BotClient(methodContext),0,10000);
        }

        if(gui.isMule()){
            log("Before mule handler server");
            muleServer = new MuleServer(scriptManager,methodContext);
            muleServer.start(); // Start the mule server
            muleRemoveServer = new MuleRemoveServer(scriptManager);
            muleRemoveServer.start();

        }

    }

    @Override
    public int onLoop() {

        if(gui.isBot()) {
            while (scriptManager.isRunning()) {
                if (smelterBot.validate()) {
                    smelterBot.execute();
                }
            }
        }

        if(gui.isMule()){
            BotAccountList botList = BotAccountList.getInstance();
            MuleScript muleScript = new MuleScript(getClient().getMethodContext(),getRandomManager());
            while(scriptManager.isRunning()){
                botList.displayList();
                muleScript.execute();
            }
        }


        return 0;
    }

    @Override
    public void onPaint(Graphics g) {
        super.onPaint(g);



        if(gui.isBot()) {
            g.setColor(Color.YELLOW);
            g.drawString(smelterBot.getCurrentStateName(), 10, 330);
            g.drawString("Smithing XP: " + guiInfo.getXPGained(), 10, 315);
            g.drawString("Bars Smelted: " + guiInfo.getBarsSmelted(), 10, 300);
            g.drawString(BotNode.getInstance().getBars() + " " + guiInfo.getBarsSmelted(), 10, 285);
        }

        if(gui.isMule()){
            g.setColor(Color.YELLOW);
            BotAccountList botList = BotAccountList.getInstance();
            BotNode link = botList.getHead();
            int y = 25;
            while(link != null){
                g.drawString("Bot: "+link.getBotName()+" World: " + link.getWorld()+ " Bars: "+ link.getBars()
                        + " Ore: " + link.getTinOreCount(), 10, y);
                link = link.getLink(); // move to the next node
                y+= 15;
            }
        }



    }

    @Override
    public void onExit() {
        if(scheduledFuture != null)
            scheduledFuture.cancel(true);
        if(botUpdater != null){
            botUpdater.cancel();
            new BotRemoveClient(getClient().getMethodContext()).start();
        }
        if(muleServer != null && muleServer.getServerSocket() != null){
            try {
                muleServer.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(muleRemoveServer != null && muleRemoveServer.getServerSocket() != null){
            try {
                muleRemoveServer.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onExit();
    }
}
