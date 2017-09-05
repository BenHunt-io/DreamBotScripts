package Script;

import BotMuleAccounts.BotNode;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.bank.BankType;
import org.dreambot.api.wrappers.interactive.Entity;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/22/2017.
 */
public class Banking extends BotState {


    public Banking(Bot botContext, MethodContext methodContext, GuiInfo guiInfo) {
        super(botContext, methodContext, guiInfo);
    }

    @Override
    public void execute() {
        log("should be banking");
        Entity bankBooth;
        while(scriptManager.isRunning() && !mC.getBank().isOpen()) {
            bankBooth = mC.getBank().getClosestBank(BankType.BOOTH);
            if (bankBooth != null) {
                bankBooth.interact();
                mC.sleepUntil(() -> mC.getBank().isOpen(), rand.nextInt(5000) + 7500);
            }
        }
        if(mC.getBank().isOpen() && mC.getBank().count(Constants.COPPER_ORE) > 14 &&
                mC.getBank().count(Constants.TIN_ORE) >  14 || BotNode.getInstance().isNewAccount()){

            mC.getBank().depositAllItems();
            if(mC.getBank().contains(Constants.BRONZE_BAR))
                BotNode.getInstance().setBars(mC.getBank().get(Constants.BRONZE_BAR).getAmount());
            else BotNode.getInstance().setBars(0);
            if(mC.getBank().contains(Constants.TIN_ORE))
                BotNode.getInstance().setTinOreCount(mC.getBank().get(Constants.TIN_ORE).getAmount());
            else BotNode.getInstance().setTinOreCount(0);

            if(mC.getBank().getWithdrawMode().equals(BankMode.NOTE))
                mC.getBank().setWithdrawMode(BankMode.ITEM);
            mC.getBank().withdraw(Constants.TIN_ORE, 14);
            mC.getBank().withdrawAll(Constants.COPPER_ORE);
            mC.getBank().close();
        }

        bC.setCurrentState(bC.getWalkingToFurnaceState()); // State changed to walking to furnace
    }

    @Override
    public boolean validate() {
        if(Constants.FALADOR_WEST.contains(localPlayer)){
            return true;
        }
        else {
            scriptManager.stop();
            return false;
        }
    }

    @Override
    public String getStateName() {
        return "Script.Banking";
    }
}
