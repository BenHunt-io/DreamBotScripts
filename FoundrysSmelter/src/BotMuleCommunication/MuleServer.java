package BotMuleCommunication;


import BotMuleAccounts.BotAccountList;
import Script.Bot;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.script.ScriptManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 9/1/2017.
 * This thread handles all communication between the bots and the mule.
 * Note: this thread in itself spawns threads to handle and communicate with each individual bot
 */
public class MuleServer extends Thread {

    private ScriptManager scriptManager;
    private ArrayList<Thread> botHandlers = new ArrayList();
    private ServerSocket serverSocket;
    private Socket servSock;
    private MethodContext mC;

    public MuleServer(ScriptManager scriptManager, MethodContext methodContext){
        this.scriptManager = scriptManager;
        this.mC = methodContext;

    }

    @Override
    public void run() {


        try {
            serverSocket = new ServerSocket(7000);

            while(scriptManager.isRunning()){ // while script manager is running
                servSock = serverSocket.accept(); // blocking operation
                // Make a thread to communicate with bot with
                Thread botHandlerThread = new Thread(new BotHandler(servSock,mC));
                // Start the thread.
                botHandlerThread.start();
                // Add the thread to the list of Threads ( so we can interrupt if need be)
                botHandlers.add(botHandlerThread);
            }


        } catch (IOException ex) {
            log("Server Failed " + ex);
        }
    }


    public ServerSocket getServerSocket(){return serverSocket;}
    public Socket getServSock(){return servSock;}
}
