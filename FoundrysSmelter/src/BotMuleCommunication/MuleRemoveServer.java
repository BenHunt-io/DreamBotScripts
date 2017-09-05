package BotMuleCommunication;

import BotMuleAccounts.BotAccountList;
import org.dreambot.api.script.ScriptManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Mule side server to remove bots when the script stops.
 * Created by Ben on 9/4/2017.
 */
public class MuleRemoveServer extends Thread {


    private ServerSocket serverSocket;
    private Socket servSock;
    private ScriptManager scriptManager;

    public MuleRemoveServer(ScriptManager scriptManager){
        this.scriptManager = scriptManager;
    }

    /**
     * Removes bots upon receiving a botName from a client
     */
    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(7001);

            while(scriptManager.isRunning()) {

                BotAccountList botAccountList = BotAccountList.getInstance();
                String accountName;


                servSock = serverSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(servSock.getInputStream()));
                accountName = bufferedReader.readLine();

                botAccountList.removeBotNode(accountName);
            }

        } catch (IOException e) {
            log("MuleRemoveServer failed");
        }

    }

    public ServerSocket getServerSocket(){return  serverSocket;}

}
