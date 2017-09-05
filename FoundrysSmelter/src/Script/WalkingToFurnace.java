package Script;

import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.map.Tile;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/22/2017.
 */
public class WalkingToFurnace extends BotState {


    public WalkingToFurnace(Bot botContext, MethodContext methodContext, GuiInfo guiInfo) {
        super(botContext, methodContext, guiInfo);
    }

    @Override
    public void execute() {
        log("Should be Walking to Furnace");

        Tile randomTile = Constants.FALADOR_FURNACE.getRandomTile();
        while(scriptManager.isRunning() && !Constants.FALADOR_FURNACE.contains(localPlayer)){
            if(mC.getWalking().shouldWalk(5)){
                mC.getWalking().walk(randomTile);
                log(randomTile.getX() + " " + randomTile.getY());
                if(mC.getWalking().getDestinationDistance() < 5) {
                    mC.sleep(rand.nextInt(1000) + 1000);
                    mC.sleepUntil(() -> !localPlayer.isMoving(), rand.nextInt(5000) + 5000);
                    randomTile = Constants.FALADOR_FURNACE.getRandomTile();
                }
            }
        }

        bC.setCurrentState(bC.getSmeltingState());
    }

    @Override
    public boolean validate() {
        if(mC.sleepUntil(()-> mC.getInventory().count(Constants.COPPER_ORE) == 14
                && mC.getInventory().count(Constants.TIN_ORE) == 14, rand.nextInt(5000) + 5000)){
            return true;
        }
        else{
            scriptManager.stop();
            return false;
        }
    }

    @Override
    public String getStateName() {
        return "Walking to Furnace";
    }
}
