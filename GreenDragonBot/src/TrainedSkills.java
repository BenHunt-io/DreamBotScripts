import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.skills.Skill;

/**
 * Created by Ben on 8/7/2017.
 */
public class TrainedSkills {

    private int startingXP;
    private int currentXP;
    private int gainedXP;

    private Skill skill;

    public TrainedSkills(Skill skill, int startingXP){
        this.skill = skill;
        this.startingXP = startingXP;
        this.currentXP = startingXP;
    }

    public boolean isTraining(MethodContext context) {
        if (startingXP < context.getSkills().getExperience(skill)) {
            return true;
        } else
            return false;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getStartingXP(){
        return startingXP;
    }

    public void setCurrentXP(int currentXP){
        this.currentXP = currentXP;
        gainedXP =  currentXP - startingXP;
    }

    public int getGainedXP(){
        return gainedXP;
    }
}
