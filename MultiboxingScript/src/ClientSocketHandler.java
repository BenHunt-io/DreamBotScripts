
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.items.GroundItem;

import java.io.*;
import java.net.Socket;
import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 7/14/2017.
 */
public class ClientSocketHandler implements Runnable{

    private MethodContext methodContext;
    private Account account;
    private MultiBoxGui multiBoxGui;
    private ObjectOutputStream objectOS;
    private DataInputStream serverInput;
    private Thread currentThread;

    public ClientSocketHandler(MethodContext methodContext){
        this.methodContext = methodContext;
    }

    @Override
    public void run() {

        currentThread = Thread.currentThread();
       // new Thread(new closeWindowsRunnable()).start(); // waits for signal to close client windows from server


        String hostName = "localhost";
        int port = 7000;

        try {
            Socket clientEnd = new Socket(hostName,port);
            log("Created Client");

            ObjectOutputStream objectOS = new ObjectOutputStream(clientEnd.getOutputStream());
            serverInput = new DataInputStream(clientEnd.getInputStream());
            // initialize account with current skills
            account = new Account(methodContext.getSkills().getRealLevel(Skill.HITPOINTS),
                                methodContext.getSkills().getBoostedLevels(Skill.HITPOINTS),
                                methodContext.getLocalPlayer().getName());

            log(account.userName + " " + account.currentHP + " " + account.totalHP);


            while(!Thread.interrupted()){



                objectOS.reset(); // remove any previous objects from stream
                objectOS.writeObject(account);
                Thread.sleep(300);

                account.updateHP(methodContext.getSkills().getBoostedLevels(Skill.HITPOINTS));


            }
        } catch (IOException e) {
            e.printStackTrace();
            log(e + "");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

//    public class closeWindowsRunnable implements Runnable{
//
//        @Override
//        public void run() {
//
//            String hostName = "localhost";
//            int port = 7001;
//
//            try {
//                log("Client socket close window trying to connect");
//                Socket clientEnd = new Socket(hostName, port);
//                log("Close Window server/client connection made");
//
//                BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientEnd.getInputStream()));
//
//                if(inputStream.readLine().contains("closeWindows")){
//                    multiBoxGui.onExit();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }


    // really just updates that a kill happend on the account and puts ground Items on the account
    public void sendKillNotification(String[] groundItems){
        account.isKiller=true;
        account.groundItems = groundItems;

        try {
            account.isKiller = serverInput.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onExit(){
        currentThread.interrupt();

        try {
            Socket clientEnd = new Socket("localhost",7002);
            PrintWriter printWriter = new PrintWriter(clientEnd.getOutputStream(), true);

            printWriter.println(account.userName);
            log("Sent Username");
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
