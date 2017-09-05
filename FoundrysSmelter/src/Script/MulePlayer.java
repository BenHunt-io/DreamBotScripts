package Script;

import BotMuleAccounts.BotAccountList;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.wrappers.interactive.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Ben on 8/22/2017.
 * Represents the Script.MulePlayer. Holds all the information about the mule player as well as the method of talking to the
 * bots which is through the use of sockets.
 */
public class MulePlayer {

    private String mulePlayerName;
    private Player mulePlayer; // favor composition over inheritance
    private ServerSocket serverSocket;
    private Socket socket;
    private MethodContext mC;
    private ArrayList<BotAccountList> botAccounts;

    public MulePlayer(MethodContext methodContext){
        this.mC = methodContext;
        mulePlayerName = null;
        mulePlayer = null;
        botAccounts = new ArrayList<>();
    }


    public String getPlayerName(){return mulePlayerName;}
    public Player getPlayer(){return mulePlayer;}
    public void setPlayer(Player mulePlayer){
        this.mulePlayer = mulePlayer;
    }

    public void mule(){
        Socket socket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(7000);
            while(mC.getClient().getInstance().getScriptManager().isRunning()) {
                new OutputHandler(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public class OutputHandler implements Runnable{

        private Socket socket;

        public OutputHandler(Socket socket){
            this.socket = socket;
            new Thread(this).start();
        }
        @Override
        public void run() {
            DataOutputStream dataOutputStream = null;
            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                dataOutputStream.writeChars(mC.getLocalPlayer().getName()); // writes mule's players name to the Script.Bot's
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    public class InputHandler implements Runnable{

        private Socket socket;

        public InputHandler(Socket socket){
            this.socket = socket;
            new Thread(this).start();
        }

        @Override
        public void run() {

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                BotAccountList botAccount = (BotAccountList) objectInputStream.readObject();
                botAccounts.add(botAccount);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }






}
