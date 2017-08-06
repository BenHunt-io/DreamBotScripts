import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * Created by Ben on 7/21/2017.
 */
public class Constants {

    public static final int GAME_CHARGES = 8;
    public static final int [] GAMES_NECKLACE = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867};
    public static final int POT_DOSES = 4;
    public static final int [] COMBAT_POTIONS = {9740,9741,9742,9743};
    public static final String [] POTIONS = {"Combat Potion", "Super Attack", "Super Strength", "Super Defence", "Ranging Potion"};
    public static final int NUM_POTS_SUPPORTED = 5;
    public static final int GLORY_CHARGES = 4;
    public static final int [] GLORY = {1706, 1708, 1710, 1712};
    public static final int UNCHARGED_GLORY = 1704;
    public static final int LOBSTER = 379;
    public static final int DRAGON_BONES = 536;
    public static final int GREEN_DHIDE = 1753;

    public static final int LOOTING_BAG = 11941;



    //////////// Locations ////////////////
    public static final Area EDGEVILL_BANK = new Area (3098,3499,3091,3488);
    public static final Area CORPORAL_BEAST = new Area(2980,4370,2964, 4400,2);
    public static final Area GREEN_DRAG_AREA = new Area(3158,3712,3137, 3703, 0);
    public static final Area EDGE_TELE_AREA = new Area(3090,3487,3084,3500,0);
    public static final Area GRAVEYARD_AREA = new Area(3158,3676,3165,3669,0);
    public static final Area LUMBRIDGE_SPAWN = new Area(3217,3224,3224,3212,0);

    ///////////// Obstacles ////////////////
    public static final int CAVE_EXIT = 679;
    public static final int WILDERNESS_DITCH = 23271;


}
