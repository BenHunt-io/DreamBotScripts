import org.dreambot.api.Client;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.wrappers.items.GroundItem;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 7/14/2017.
 */
public class ServerSocketHandler implements Runnable {


    private ArrayList<Account> accounts;
    private ServerSocket serverSocket;
    private Socket serverEnd;
    private MultiBoxGui multiBoxGui;


    public ServerSocketHandler(MultiBoxGui multiBoxGui){
        this.multiBoxGui = multiBoxGui;
    }


    @Override
    public void run() {

        try {
            accounts = new ArrayList<>(); // init empty list
            serverSocket = new ServerSocket(7000);
            while(!serverSocket.isClosed()) {
                log("Waiting for client");
                serverEnd = serverSocket.accept(); // waits for client request
                new Handler(serverEnd, accounts.size());
                log("Created Server");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class Handler implements Runnable{

        private Socket serverEnd;
        private int accountNumber;
        private ObjectInputStream clientInput;
        private DataOutputStream dataOutputStream;

        public Handler(Socket serverEnd, int accountNumber){

            this.serverEnd = serverEnd;
            this.accountNumber = accountNumber;
            new Thread(this).start(); // Server 7000 Handling whether account is killer/ update health

            // Server 7002.. Removes User GUI if client script stops (removes account from list, update GUI)
            new Thread(new removeClientGui()).start();



        }
        @Override
        public void run() {


            try {
                clientInput = new ObjectInputStream(serverEnd.getInputStream());
                dataOutputStream = new DataOutputStream(serverEnd.getOutputStream());
                // Add it to the list initially, then keep on replacing it at it's index, since new object is sent everytime
                Account account = (Account) clientInput.readObject();
                accounts.add(account);

                multiBoxGui.accountsLM.insertElementAt(account.userName, accounts.size()-1);
                log("Added Account to server  " + accounts.size());

                while(!serverSocket.isClosed()){
                    account = (Account) clientInput.readObject();

                    log("read in another object");
                    // Show drop and who killed
                    if(account.isKiller){
                        log("account is killer, " + account.userName);
                        multiBoxGui.groundItemLM.removeAllElements();
                        for(String groundItem: account.groundItems) {
                            multiBoxGui.groundItemLM.insertElementAt(groundItem, multiBoxGui.groundItemLM.size());
                        }
                        multiBoxGui.recentKillLbl.setText("Recent Kill by: " + account.userName);
                        dataOutputStream.writeBoolean(false); // notify that kill should be set to false again on the client side

                    }
                    else {
                        // replace the account at the assigned position with the updated account info
                        accounts.set(accountNumber, account);
                        if (Thread.interrupted()) {
                            log("test interrupted");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }



        }
    }


    // Returns the accounts that will most likely be called to update the GUI
    public ArrayList<Account> getAccounts(){
        return accounts;
    }

    public void closeClientWindows(){
        try {
            ServerSocket serverSocket = new ServerSocket(7001);
            Socket serverEnd;
            while(true) {
                serverEnd = serverSocket.accept();

                PrintWriter clientOutput = new PrintWriter(serverEnd.getOutputStream(), true);
                clientOutput.println(new String("closeWindows"));
                log("finished sending message to client to close windows");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Client stops script, GUI displaying their heatlh should be removed. Decrement account count and remove
    public class removeClientGui implements Runnable {


        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(7002);
                Socket serverEnd;
                while(true) {
                    serverEnd = serverSocket.accept();

                    BufferedReader clientInput = new BufferedReader(new InputStreamReader(serverEnd.getInputStream()));
                    String accountName = clientInput.readLine();
                    log("recieved on server 7002");
                    for(int i = 0; i < accounts.size(); i++){
                        if(accountName.equals(accounts.get(i).userName)){
                            accounts.remove(i); // Remove the account with matching userName321
                            multiBoxGui.accountsLM.remove(i);

                        }
                    }
                    log("finished sending message to client to close windows");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onExit(){
        try {
            if(serverSocket!= null){
                serverSocket.close(); // Only need to close serversocket?

            }

            //serverEnd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
