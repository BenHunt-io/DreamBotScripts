package BotMuleCommunication;

import org.dreambot.api.methods.MethodContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Created by Ben on 9/4/2017.
 */
public class BotRemoveClient extends Thread{


    private MethodContext mC;

    public BotRemoveClient(MethodContext mC){
        this.mC = mC;
    }

    private Socket socket;

    @Override
    public void run() {


        try {
            socket = new Socket("localhost",7001);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(mC.getLocalPlayer().getName());
            printWriter.flush(); // make sure it gets sent
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
