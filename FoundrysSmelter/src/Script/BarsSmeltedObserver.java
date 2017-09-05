package Script;

import org.dreambot.api.methods.MethodContext;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/24/2017.
 */
public class BarsSmeltedObserver implements Observer {

    MethodContext mC;
    GuiInfo guiInfo;

    public BarsSmeltedObserver(Observable observable, MethodContext mC){
        observable.addObserver(this);
        this.mC = mC;
        guiInfo = GuiInfo.getInstance();
    }

    @Override
    public void update() {
        int barsSmelted = 0;
        if(guiInfo.getXPGained() % 6.25 != 0){
            barsSmelted++;
        }
        barsSmelted += (int)(guiInfo.getXPGained() / 6.25);
        guiInfo.setBarsSmelted(barsSmelted);
    }
}
