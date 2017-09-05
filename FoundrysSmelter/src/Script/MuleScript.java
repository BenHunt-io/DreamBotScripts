package Script;

import BotMuleAccounts.BotAccountList;
import BotMuleAccounts.BotNode;
import BotMuleAccounts.MuleAccount;
import Sleep.Condition;
import Sleep.SleepController;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.randoms.RandomManager;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.Random;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.MethodProvider.sleep;

/**
 * Created by Ben on 9/2/2017.
 */
public class MuleScript {

    private MethodContext mC;
    private ScriptManager scriptManager;
    private RandomManager randomManager;

    public MuleScript(MethodContext mC, RandomManager randomManager){
        this.randomManager = randomManager;
        this.mC = mC;
        scriptManager = mC.getClient().getInstance().getScriptManager();
    }

    public void execute(){

        BotAccountList botAccountList = BotAccountList.getInstance();

        while(scriptManager.isRunning()){
            BotNode mulableBot = botAccountList.getMulable();
            if(mulableBot != null){
                if(!mC.getClient().isLoggedIn()){
                    randomManager.enableSolver(RandomEvent.LOGIN);
                }
                hopWorlds(mulableBot.getWorld());
                muleOffBot(mulableBot);
                mC.getTabs().logout();
                randomManager.disableSolver(RandomEvent.LOGIN);

            }

            // Check if account is mulable every so often then log back in if there is a mulable account ready
            SleepController.sleep(10000);

        }
    }

    public void hopWorlds(int world){
        mC.getWorldHopper().hopWorld(world);
        SleepController.sleepUntil( () -> mC.getClient().getCurrentWorld() == world, 500, 15000);
    }


    public void muleOffBot(BotNode mulableBot){

        // Wait till the bot arrives in the bank. Time out after 2 minutes
        SleepController.sleepUntil(() -> Constants.FALADOR_WEST.contains(mC.getPlayers().closest(mulableBot.getBotName()))
                && scriptManager.isRunning(), 1000,120000);
        // Once in the bank wait till the bot trades the mule, then do the trade
        if(Constants.FALADOR_WEST.contains(mC.getPlayers().closest(mulableBot.getBotName()))){
            log("Bot is in bank");
            WidgetChild widgetChild = mC.getWidgets().getWidgetChild(162,43,0);
            if(SleepController.sleepUntil(()-> widgetChild != null && widgetChild.getText().contains(mulableBot.getBotName()),
                    250,25000)){
                mC.getTrade().tradeWithPlayer(mulableBot.getBotName());
                if(!mulableBot.isNewAccount()) {
                    mC.sleepUntil(() -> mC.getTrade().getTheirItems() != null, 7000);
                    mC.sleepUntil(() -> mC.getTrade().contains(false, Constants.NOTED_BRONZE), 2000);
                }
                if(mulableBot.getTinOreCount() != -1 && mulableBot.getTinOreCount() < 250 || mulableBot.isNewAccount()){
                    SleepController.sleepUntil(()-> mC.getTrade().isOpen(), 200, 5000);
                    mC.getTrade().addItem(Constants.NOTED_COPPER, 750);
                    mC.getTrade().addItem(Constants.NOTED_TIN, 750);
                    if(mulableBot.isNewAccount())
                        mulableBot.setAccountStatus(false); // not new anymore after getting supplies from mule
                }
                mC.getTrade().acceptTrade();
                if(mC.sleepUntil(() -> mC.getTrade().isOpen(2), 5000)){
                    mC.getTrade().acceptTrade();
                }
            }

        }

    }
}
