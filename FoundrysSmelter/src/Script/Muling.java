package Script;

import BotMuleAccounts.MuleAccount;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.bank.BankType;
import org.dreambot.api.wrappers.interactive.Entity;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;


/**
 * Created by Ben on 8/22/2017.
 */
public class Muling extends BotState {

    MuleAccount muleAccount;

    public Muling(Bot botContext, MethodContext methodContext, GuiInfo guiInfo) {
        super(botContext, methodContext, guiInfo);
        muleAccount = MuleAccount.getInstance();
    }


    /**
     * The bot will mule it's supplies over to the mule account if validate() returns true.
     */
    @Override
    public void execute() {
        log("Now Script.Muling");

        Entity bankBooth = mC.getBank().getClosestBank(BankType.BOOTH);
        if(bankBooth != null){
            bankBooth.interact();
            sleepUntil(()-> mC.getBank().isOpen(),rand.nextInt(5000) + 7500);
        }
        if(mC.getBank().isOpen()){
            mC.getBank().depositAllItems();
            if(mC.sleepUntil(()-> mC.getInventory().isEmpty(), rand.nextInt(5000) + 5000)){
                mC.getBank().setWithdrawMode(BankMode.NOTE);
                mC.getBank().withdrawAll(Constants.BRONZE_BAR);
                mC.getBank().close();
            }
        }


        // Trade with player
        mC.getTrade().tradeWithPlayer(muleAccount.getMuleName());
        int barCount = 0;
        if(mC.getInventory().count(Constants.NOTED_BRONZE) > 0){
            barCount = mC.getInventory().count(Constants.NOTED_BRONZE);
        }
        sleepUntil(() -> mC.getTrade().isOpen(), rand.nextInt(5000) + 7500);
        if(mC.getTrade().isOpen() && barCount > 0){
            mC.getTrade().addItem(Constants.NOTED_BRONZE,barCount);
            sleepUntil(() -> !mC.getWidgets().getWidgetChild(335,30).getText().equals(""), rand.nextInt(5000) + 5000);
            mC.getTrade().acceptTrade();
            if(mC.sleepUntil(() -> mC.getTrade().isOpen(2), rand.nextInt(5000) + 5000)){
                mC.getTrade().acceptTrade();
            }
        }
        bC.setCurrentState(bC.getBankingState());
    }


    /**
     * Validates that the bot should be muling supplies. If the mule player exists and is in the bank at the same time
     * the bot arrives at the bank then muling should take place.
     * @return whether or not muling should occur
     */
    @Override
    public boolean validate() {
        if(Constants.FALADOR_WEST.contains(localPlayer) && muleAccount.getMuleName() != null){
            muleAccount.setMulePlayer(mC.getPlayers().closest(player -> player.getName().equals(muleAccount.getMuleName())));
            if(muleAccount.getMulePlayer() != null && Constants.FALADOR_WEST.contains(muleAccount.getMulePlayer())){
                return true;
            }
        }
        // Otherwise should just go to banking state
        bC.setCurrentState(bC.getBankingState());
        return false;
    }

    @Override
    public String getStateName() {
        return "Script.Muling";
    }
}
