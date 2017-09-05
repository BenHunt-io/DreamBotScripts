package BotMuleCommunication;

/**
 * Created by Ben on 9/2/2017.
 */

import BotMuleAccounts.BotNode;
import BotMuleAccounts.MuleAccount;
import BotMuleAccounts.SerialPlayer;
import Script.Bot;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.script.ScriptManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.TimerTask;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * To handle communication from the side of the bot to the mule (server)
 */
public class BotClient extends TimerTask{

    private MethodContext mC;
    private ScriptManager scriptManager;

    public BotClient(MethodContext mC){
        this.mC = mC;
        scriptManager = mC.getClient().getInstance().getScriptManager();
    }

    @Override
    public void run() {
        Socket socket;

        try {

            BotNode botNode = BotNode.getInstance(mC.getLocalPlayer().getName(), mC.getClient().getCurrentWorld(), null);
            socket = new Socket("localhost",7000);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(botNode);
            oos.flush();


            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String muleName = bufferedReader.readLine();
            if(muleName != null){
                MuleAccount.getInstance().setMuleName(muleName);
            }

        } catch (IOException ex) {
            System.out.println("Connection Failed");
        }
    }
}
