import org.dreambot.api.wrappers.items.GroundItem;

import java.io.Serializable;

/**
 * Created by Ben on 7/14/2017.
 */
// TO be serializable has to implement serializable
public class Account implements Serializable{

    public int totalHP;
    public int currentHP;
    public String userName;
    public boolean isKiller = false;
    public String[] groundItems;

    public Account(int totalHP, int currentHP, String userName){
        this.totalHP = totalHP;
        this.currentHP = currentHP;
        this.userName = userName;
    }

    public void updateHP(int newHP){
        currentHP = newHP;
    }
}
