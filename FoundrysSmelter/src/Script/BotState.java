package Script;

import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.wrappers.interactive.Player;


import java.util.Random;

/**
 * Created by Ben on 8/22/2017.
 */
public abstract class BotState {

    Bot bC;
    MethodContext mC;
    Player localPlayer;
    Random rand;
    ScriptManager scriptManager;
    private GuiInfo guiInfo;

    public BotState(Bot botContext, MethodContext methodContext, GuiInfo guiInfo){
        this.bC = botContext;
        this.mC = methodContext;
        localPlayer = methodContext.getLocalPlayer();
        rand = new Random(System.currentTimeMillis());
        scriptManager = methodContext.getClient().getInstance().getScriptManager();
        this.guiInfo = guiInfo;
    }



    public abstract void execute();
    public abstract boolean validate();
    public abstract String getStateName();

}
