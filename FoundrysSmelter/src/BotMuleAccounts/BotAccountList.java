package BotMuleAccounts;

import java.util.ArrayList;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 8/28/2017.
 * Linked List Singleton
 */
public class BotAccountList {

    private BotNode head;
    private static BotAccountList botLinkedList;


    private BotAccountList(){
        head = null;
    }

    /**
     * performs a double null check to reduce use of synchronization for effeciency
     * @return returns instance of the botLinkedList. if not created an instance will be made
     */
    public static BotAccountList getInstance(){
        if(botLinkedList == null){
            synchronized (BotAccountList.class){
                if(botLinkedList == null){
                    botLinkedList = new BotAccountList();
                    return botLinkedList;
                }
            }
        }
        return botLinkedList;


    }

    /**
     * Adds a new bot to the list only if it doesn't exist in the list already.
     * true if successful, false otherwise
     */
    // Server cannot make more than one instance of BotNode so I need to change the definition if I want to allow this
//    public boolean appendBotNode (String botName, int world) {
//        if (head == null) {
//            head = new BotNode(botName, world, head);
//            return true;
//        }
//
//        BotNode link = head;
//        while(link != null){
//            if(link.getBotName().equals(botName))
//                return false;
//            link = link.getLink();
//        }
//
//        link = new BotNode(botName, world, null);
//        return true;
//
//    }

    /**
     * Adds a new bot to the list only if it doesn't exist in the list already.
     * Updates bot if it exists already
     * true if successful, false otherwise
     */
    public boolean appendBotNode (BotNode newBotNode) {
        if (head == null) {
            head = newBotNode;
            return true;
        }

        BotNode link = head;
        while(link.getLink() != null){
            if(link.getBotName().equals(newBotNode.getBotName())) {
                link.setBars(newBotNode.getBars());
                link.setAccountStatus(newBotNode.isNewAccount());
                return false;
            }
            link = link.getLink();
        }

        if(!link.getBotName().equals(newBotNode.getBotName())) {
            link.setLink(newBotNode);
            return true;
        }
        else {
            link.setBars(newBotNode.getBars());
            link.setAccountStatus(newBotNode.isNewAccount());
        }


        return false; // not successful

    }

    /**
     *
     * @return
     */
    public boolean removeBotNode(String botName){

        if(head == null){
            return false;
        }

        BotNode link = head;
        if(link.getBotName().equals(botName)){
               head = head.getLink();
               return true;
        }


        link = link.getLink();

        while(link != null){
            if(link.getBotName().equals(botName)){
                link.setLink(link.getLink());
                return true;
            }
            link = link.getLink();
        }

        return false;
    }


    /**
     *
     * @param botName
     * @return Returns the BotNode with the specified name, null if not found
     */
    public BotNode getBotNode(String botName) {
        if(head == null){
            return null;
        }

        BotNode link = head;

        while(link != null){
            if(link.getBotName() == botName){
                return link;
            }
            link = link.getLink();
        }

        return null;
    }

    /**
     * Logs the entire list
     */
    public void displayList(){
        if(head == null){
            log("List is empty");
            return;
        }

        BotNode link = head;
        while(link != null){
            log(link.getBotName() + "  ");
            link = link.getLink();
        }

    }


    /**
     * Gets mulable account meaning the account(bot) has a good amount of bars and should have the bars transferred off
     * @return BotNode (Mulable account) or null if no accounts are muleable
     */
    public BotNode getMulable(){
        if(head == null){
            return null;
        }

        BotNode link = head;

        log("Getting mulable");
        while(link != null){
            if(link.getBars() > 100 || link.isNewAccount()){
                return link;
            }
            link = link.getLink();
        }
        log("after getting mulable");
        return null;
    }

    /**
     * Gets the head of the LinkedList
     * @return head
     */
    public BotNode getHead(){
        return head;
    }



}
