import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.wrappers.items.Item;

import java.util.HashMap;

/**
 * Created by Ben on 7/22/2017.
 */
public class Equipment {

    private int ammoStartCount; // Starting ammt of ammo
    private int ammoID; // Item Id of the Ammo, Default value of 0
    private int currentAmmoCount; // current Ammo of the user

    public Equipment(int ammoID, int ammoStartCount) {
        this.ammoID = ammoID;
        this.ammoStartCount = ammoStartCount;
    }

    public void setEquipment(int ammoId, int ammoStartCount){
        this.ammoID = ammoId;
        this.ammoStartCount = ammoStartCount;
    }

    public int getAmmoId(){
        return ammoID;
    }

    public int getAmmoStartCount(){
        return ammoStartCount;
    }

    // Gets and Sets the current Ammo Count


    int getCurrentAmmoCount(MethodContext methodContext){
        if(!methodContext.getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())) {
            this.currentAmmoCount = methodContext.getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()).getAmount();
            return this.currentAmmoCount;
        }
        else
            return 0;
    }



    public int getWithdrawlAmt(){
        return (ammoStartCount - currentAmmoCount);
    }
}
