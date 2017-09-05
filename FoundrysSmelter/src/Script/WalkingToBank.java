package Script;

import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.map.Tile;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/22/2017.
 */
public class WalkingToBank extends BotState{


    public WalkingToBank(Bot botContext, MethodContext methodContext, GuiInfo guiInfo) {
        super(botContext, methodContext, guiInfo);
    }

    @Override
    public void execute() {

        log("Walking to Bank");

        Tile randomTile = Constants.FALADOR_WEST.getRandomTile();
        while(scriptManager.isRunning() && !Constants.FALADOR_WEST.contains(localPlayer)){
            if(mC.getWalking().shouldWalk(5)){
                mC.getWalking().walk(randomTile);
                if(mC.getWalking().getDestinationDistance() < 5) {
                    mC.sleep(rand.nextInt(1000) + 1000);
                    mC.sleepUntil(() -> !localPlayer.isMoving(), rand.nextInt(5000) + 5000);
                    randomTile = Constants.FALADOR_WEST.getRandomTile();
                }
            }
        }

        bC.setCurrentState(bC.getMulingState());

    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String getStateName() {
        return "Walking to Bank";
    }
}
