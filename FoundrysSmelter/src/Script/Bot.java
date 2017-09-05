package Script;

import org.dreambot.api.methods.MethodContext;

/**
 * Created by Ben on 8/22/2017.
 */
public class Bot {

    BotState currentState;

    BotState newAccountSetup;
    BotState muling;
    BotState banking;
    BotState walkingToFurnace;
    BotState smelting;
    BotState walkingToBank;

    public Bot(MethodContext methodContext, GuiInfo guiInfo){

        muling = new Muling(this, methodContext, guiInfo);
        banking = new Banking(this,methodContext, guiInfo);
        walkingToFurnace = new WalkingToFurnace(this,methodContext, guiInfo);
        smelting = new Smelting(this,methodContext,guiInfo);
        walkingToBank = new WalkingToBank(this,methodContext,guiInfo);
        newAccountSetup = new NewAccountSetup(this,methodContext,guiInfo);

        currentState = newAccountSetup;
    }


    public void execute(){
        currentState.execute();
    }

    public boolean validate(){
      return  currentState.validate();
    }

    public void setCurrentState(BotState currentState){this.currentState = currentState;}
    public BotState getBankingState(){return banking;}
    public BotState getWalkingToFurnaceState(){return walkingToFurnace;}
    public BotState getSmeltingState(){return smelting;}
    public BotState getWalkingToBankState(){return walkingToBank;}
    public BotState getMulingState(){return muling;}
    public String  getCurrentStateName(){return currentState.getStateName();}



    }
