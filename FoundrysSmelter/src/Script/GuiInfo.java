package Script;

import BotMuleAccounts.BotNode;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.skills.Skill;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/23/2017.
 */

/**
 * Implemented as a singleton
 */
public class GuiInfo {

    private int barsSmelted;
    private int currentXP;
    private int startingXP;
    private int xpGained;
    private BotNode head;

    private static GuiInfo guiInfo; // only one of this variable (object) exists

    private GuiInfo(){
    }

    /**
     * Pass in method context
     * @param
     * @return
     */
    public static GuiInfo getInstance(){
        if(guiInfo == null){
            synchronized (GuiInfo.class){
                if(guiInfo == null){
                    guiInfo = new GuiInfo();
                    return guiInfo;
                }
            }
        }
        return guiInfo;
    }




    public void setBarsSmelted(int barsSmelted){ this.barsSmelted = barsSmelted;}


    public int getBarsSmelted(){return barsSmelted;}

    public void updateXP (int currentXP, MethodContext mC){
        if(startingXP == 0){
            startingXP = mC.getSkills().getExperience(Skill.SMITHING);
        }
        this.currentXP = currentXP;
        this.xpGained = currentXP - this.startingXP;
        //log(startingXP + " "  + currentXP + " " + xpGained);
    }

    public int getCurrentXP(){return currentXP;}

    public int getXPGained(){return xpGained;}

    // Sets head to traverse the list. Update everytime list gets updated
    public void setHeadNode(BotNode head){this.head = head;}




}
