import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;

/**
 * Created by Ben on 7/21/2017.
 */
public class Constants {

    public static final int GAME_CHARGES = 8;
    public static final int [] GAMES_NECKLACE = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867};
    public static final int POT_DOSES = 4;
    public static final int [] COMBAT_POTIONS = {9740,9741,9742,9743};
    public static final String [] POTION_LIST = {"Combat potion", "Attack potion", "Strength potion", "Super combat", "Super attack", "Super strength",
            "Ranging potion"};
    public static final String [] FOOD_LIST = {"Lobster", "Trout", "Tuna", "Monkfish", "Salmon"};
    public static final int NUM_POTS_SUPPORTED = 5;
    public static final int GLORY_CHARGES = 6;
    public static final int [] GLORY = {1706, 1708, 1710, 1712, 11976, 11978};
    public static final int UNCHARGED_GLORY = 1704;
    public static final int LOBSTER = 379;
    public static final int DRAGON_BONES = 536;
    public static final int GREEN_DHIDE = 1753;

    public static final int LOOTING_BAG = 11941;


    public static final Food[] FOODS = {new Food(333,"Trout"), new Food(329,"Salmon"),
        new Food(361,"Tuna"), new Food(379, "Lobster"), new Food(7946,"Monkfish")};

    public static final Potion SUPER_ATTACK = new Potion(Skill.ATTACK, "Super attack", 1.15, 5
        , new int[] {9739,9741,9743,9745});
    public static final Potion SUPER_STRENGTH = new Potion(Skill.STRENGTH, "Super strength", 1.15, 5
        , new int[] {2440,157,159,161});
    public static final Potion ATTACK_POTION = new Potion(Skill.ATTACK, "Attack potion",1.10, 3
        , new int[] {2428,121,123,125});
    public static final Potion STRENGTH_POTION = new Potion(Skill.STRENGTH, "Strength potion", 1.10, 3
        , new int[] {113,115,117,119});
    public static final Potion RANGING_POTION = new Potion(Skill.RANGED, "Ranging potion", 1.10, 4
        , new int[] {2444,169,171,173});
    public static final Potion COMBAT_POTION = new Potion(Skill.ATTACK, "Combat potion",1.10, 3
        , new int[] {9739,9741,9743,9745});
    public static final Potion SUPER_COMBAT = new Potion(Skill.ATTACK, "Super combat", 1.15, 5
        , new int[] {12695,12697,12699,12701});

    public static final Potion[] POTIONS = {SUPER_ATTACK, SUPER_STRENGTH, ATTACK_POTION, STRENGTH_POTION, RANGING_POTION, COMBAT_POTION, SUPER_COMBAT};



    //////////// Locations ////////////////
    public static final Area EDGEVILL_BANK = new Area (3098,3499,3091,3488);
    public static final Area CORPORAL_BEAST = new Area(2980,4370,2964, 4400,2);
    public static final Area GREEN_DRAG_AREA = new Area(3158,3712,3137, 3703, 0);
    public static final Area EDGE_TELE_AREA = new Area(3090,3487,3084,3500,0);
    public static final Area GRAVEYARD_AREA = new Area(3158,3676,3165,3669,0);
    public static final Area LUMBRIDGE_SPAWN = new Area(3226,3209,3217,3227,0);

    ///////////// Obstacles ////////////////
    public static final int CAVE_EXIT = 679;
    public static final int WILDERNESS_DITCH = 23271;




}
