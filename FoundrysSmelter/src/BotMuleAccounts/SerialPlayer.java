package BotMuleAccounts;

import org.dreambot.api.Client;
import org.dreambot.api.wrappers.interactive.Player;

import java.io.Serializable;

/**
 * Created by Ben on 9/3/2017.
 */
public class SerialPlayer extends Player implements Serializable {


    public SerialPlayer(Client client, Object o) {
        super(client, o);
    }
}
