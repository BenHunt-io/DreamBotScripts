package BotMuleCommunication;

import BotMuleAccounts.BotAccountList;
import BotMuleAccounts.BotNode;
import org.dreambot.api.methods.MethodContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 9/1/2017.
 */
public class BotHandler implements Runnable{

    private Socket socket;
    private MethodContext mC;

    public BotHandler(Socket socket, MethodContext mC){
        this.socket = socket;
        this.mC = mC;
    }

    @Override
    public void run() {

        BotAccountList botAccountList = BotAccountList.getInstance();
        try {
            log("Before reading in on Server");
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            BotNode bot = (BotNode) ois.readObject();
            botAccountList.appendBotNode(bot); // adds a new botNode to the list, if it doesn't exist
            log("After" + bot.getBotName() + bot.getWorld());

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(mC.getLocalPlayer().getName());
            printWriter.flush(); // make sure it's sent


        } catch (IOException ex) {
            log("BotHandler failed " + ex);
        } catch (ClassNotFoundException ex) {
            log("BotHandler failed " + ex);
        }
    }


}
