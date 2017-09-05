package BotMuleAccounts;

import org.dreambot.api.wrappers.interactive.Player;

import java.io.Serializable;

/**
 * Created by Ben on 9/2/2017.
 * Node for a linked lists to hold all current bots
 */
public class BotNode implements Serializable {

    private static BotNode botNode; // only instance of botNode;

    private boolean isInitialized;
    private boolean remove; // determines whether a bot should be removed or not, false by default

    private String botName;
    private int world;
    private int bars; // To track when to mule
    private int tinOreCount;
    private boolean newAccount; // if account is a new account
    private Player player;
    private BotNode next;

    /**
     * Made this class a singleton, because only 1 botNode needs to be created for every bot client.
     * The one instance can then be updated and sent to the server.
     * @param botName
     * @param world
     * @param link
     */
    private BotNode(String botName, int world, BotNode link){
        this.botName = botName;
        this.world = world;
        next = link;
        tinOreCount = -1;
        bars = -1;
    }

    /**
     * Marks whether or not the BotNode has been populated with data or if it's just an empty object.
     */
    private BotNode(){
        isInitialized = false;
        tinOreCount = -1;
        bars = -1;
    }


    public static BotNode getInstance(String botName, int world, BotNode link){
        if(botNode == null){
            botNode = new BotNode(botName,world,link);
            return botNode;
        }
        else if(!botNode.isInitialized){
            botNode.botName = botName;
            botNode.world = world;
            botNode.next = link;
        }
        return botNode;
    }

    public static BotNode getInstance(){
        if(botNode == null){
            botNode = new BotNode();
            return botNode;
        }
        return botNode;
    }


    public String getBotName(){return botName;}
    public int getWorld(){return world;}
    public BotNode getLink(){return next;}
    public int getBars(){return bars;}
    public int getTinOreCount(){return tinOreCount;}
    public Player getPlayer(){return player;}

    public void setLink(BotNode link){
        this.next = link;
    }
    public void setBars(int numOfBars){this.bars = numOfBars;}
    public void setTinOreCount(int count){this.tinOreCount = count;}
    public void setAccountStatus(boolean isNew){newAccount = isNew;}

    public boolean isNewAccount(){return newAccount;}




}
