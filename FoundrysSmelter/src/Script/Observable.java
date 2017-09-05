package Script;

/**
 * Created by Ben on 8/23/2017.
 */
public interface Observable {

    public void sendNotification();
    public void addObserver(Observer observer);
}
