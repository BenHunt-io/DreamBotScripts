package BotMuleAccounts;

import org.dreambot.api.wrappers.interactive.Player;

/**
 * Created by Ben on 9/1/2017.
 * Singleton
 */
public class MuleAccount {

    private String muleName;
    private static MuleAccount muleAccount;
    private Player mulePlayer;

    private MuleAccount(){

    }

    public static MuleAccount getInstance(){
        if(muleAccount == null){
            muleAccount = new MuleAccount();
        }
        return muleAccount;
    }


    public String getMuleName(){
        return muleName;
    }
    public Player getMulePlayer() {return mulePlayer;}

    public void setMuleName(String name){
        this.muleName = name;
    }

    public void setMulePlayer(Player mulePlayer){this.mulePlayer = mulePlayer;}



}
