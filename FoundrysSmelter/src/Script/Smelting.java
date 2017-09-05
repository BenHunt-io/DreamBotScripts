package Script;

import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.Menu;

import java.awt.*;

import static org.dreambot.api.methods.MethodProvider.log;


/**
 * Created by Ben on 8/22/2017.
 */
public class Smelting extends BotState {


    public Smelting(Bot botContext, MethodContext methodContext, GuiInfo guiInfo) {
        super(botContext, methodContext, guiInfo);
    }

    @Override
    public void execute() {
        log("Script.Smelting");

        GameObject furnace = mC.getGameObjects().closest(Constants.FURNACE);
        if(furnace != null){
            while(mC.getInventory().count(Constants.BRONZE_BAR) != 14 && scriptManager.isRunning()) {
                furnace.interact("Smelt");
                if (mC.sleepUntil(() -> mC.getDialogues().inDialogue()
                        && mC.getWidgets().getWidgetChild(311, 16) != null, rand.nextInt(5000) + 7500)){
                    log("In dialogue");
                    Rectangle bronzeRectangle = mC.getWidgets().getWidgetChild(311, 4).getRectangle();
                    mC.getMouse().click(bronzeRectangle, true);
                    Menu menu = new Menu(mC.getClient());
                    if (mC.sleepUntil(() -> menu.isMenuVisible(), rand.nextInt(2500) + 2500)) {
                        menu.clickIndex(3);
                    }
                    if (mC.sleepUntil(() -> mC.getDialogues().canEnterInput(), rand.nextInt(2500) + 2500)) {
                        mC.getKeyboard().type(rand.nextInt(1000) + 28);
                        mC.getDialogues().continueDialogue();
                        mC.sleepUntil(()-> !mC.getDialogues().inDialogue(), rand.nextInt(2500) + 2500);
                    }

                    mC.sleepUntil(() -> mC.getInventory().count(Constants.BRONZE_BAR) == 14
                            || mC.getDialogues().inDialogue(), rand.nextInt(10000) + 37500);
                    while (mC.getDialogues().inDialogue()) {
                        mC.getDialogues().continueDialogue();
                    }
                }
            }
        }

        bC.setCurrentState(bC.getWalkingToBankState());




    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String getStateName() {
        return "Script.Smelting";
    }
}
