import java.util.HashMap;

/**
 * Created by Ben on 7/22/2017.
 */
public class Equipment {

    private HashMap<String, Integer> equipment;

    public Equipment(){
        equipment = new HashMap<>();
    }

    public int getAmmo(){
        return equipment.getOrDefault("Ammo", -1);
    }

    public int getStartingAmmoCount() {

        return equipment.getOrDefault("AmmoCount",-1);
    }

    public void putEquipment(String key, int id){
         equipment.put(key,id);
    }
}
