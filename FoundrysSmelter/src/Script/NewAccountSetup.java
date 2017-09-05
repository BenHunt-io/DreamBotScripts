package Script;

import BotMuleAccounts.BotNode;
import BotMuleAccounts.MuleAccount;
import Sleep.SleepController;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.skills.Skill;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;

/**
 * Created by Ben on 9/4/2017.
 */
public class NewAccountSetup extends BotState {

    private MuleAccount muleAccount;

    public NewAccountSetup(Bot botContext, MethodContext methodContext, GuiInfo guiInfo) {
        super(botContext, methodContext, guiInfo);
        muleAccount = MuleAccount.getInstance();
    }

    @Override
    public void execute() {

        BotNode.getInstance().setAccountStatus(true); // is new account
        log("Set to new account");
        // Trade with player
        SleepController.sleepUntil(() -> mC.getPlayers().closest(muleAccount.getMuleName()) != null, rand.nextInt(500) + 500, 100000);
        mC.getTrade().tradeWithPlayer(muleAccount.getMuleName());
        SleepController.sleepUntil(() -> mC.getTrade().isOpen(), rand.nextInt(5000) + 7500, 15000);
        if(mC.getTrade().isOpen()){
            sleepUntil(() -> !mC.getWidgets().getWidgetChild(335,30).getText().equals(""), rand.nextInt(5000) + 5000);
            mC.getTrade().acceptTrade();
            if(mC.sleepUntil(() -> mC.getTrade().isOpen(2), rand.nextInt(5000) + 5000)){
                mC.getTrade().acceptTrade();
            }
        }
        BotNode.getInstance().setAccountStatus(false); // is not new account
        bC.setCurrentState(bC.getBankingState());
    }

    @Override
    public boolean validate() {
        if(mC.getSkills().getExperience(Skill.SMITHING) < 40){
            return true;
        }
        return false;
    }

    @Override
    public String getStateName() {
        return "New Account Setup";
    }
}
