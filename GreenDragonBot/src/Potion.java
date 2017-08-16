import org.dreambot.api.methods.skills.Skill;

/**
 * Created by Ben on 8/6/2017.
 */
public class Potion {

    private Skill skill;
    private String potion;
    private double percentBoosted;
    private int baseBoosted;
    private int[] potionIds;

    public Potion(Skill skill, String potion, double percentBoosted, int baseBoosted, int[] potionIds){
        this.skill = skill;
        this.potion = potion;
        this.percentBoosted = percentBoosted;
        this.baseBoosted = baseBoosted;
        this.potionIds = potionIds;
    }

    public Skill getSkill(){
        return skill;
    }

    public String getPotion(){
        return potion;
    }

    public int getTotalBoosted(int realLevel){

        return (int) (realLevel * percentBoosted) - realLevel + baseBoosted;
    }

    public int[] getPotionIds(){
        return potionIds;
    }


}
