package Script;

import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.skills.Skill;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/23/2017.
 */
public class XPObserver implements Observer {

    GuiInfo guiInfo;
    private MethodContext mC;

    public XPObserver(Observable observable, MethodContext mC){
        guiInfo = GuiInfo.getInstance();
        this.mC = mC;
        observable.addObserver(this);

    }

    @Override
    public void update() {
        guiInfo.updateXP(mC.getSkills().getExperience(Skill.SMITHING), mC);
    }
}
