package Script;

import org.dreambot.api.methods.MethodContext;

import java.util.ArrayList;

import static org.dreambot.api.methods.MethodProvider.log;


/**
 * Created by Ben on 8/23/2017.
 */
public class GuiUpdater implements Runnable, Observable{

    ArrayList<Observer> observers;
    private GuiInfo guiInfo;


    public GuiUpdater(MethodContext mC){
        observers = new ArrayList<>();
        XPObserver xpObserver = new XPObserver(this, mC);
        BarsSmeltedObserver barsSmeltedObserver = new BarsSmeltedObserver(this,mC);

    }

    @Override
    public void run() {
        sendNotification();
    }

    @Override
    public void sendNotification() {
        for(Observer observer: observers){
            observer.update();
        }
    }

    public void addObserver(Observer observer){
        observers.add(observer);
    }



}
