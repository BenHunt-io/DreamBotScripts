

import org.dreambot.api.Client;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.worldhopper.WorldHopper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.prefs.Preferences;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 7/5/2017.
 */
public class ListListeners {

    private MultiBoxGui multiBoxGui;
    private Preferences preferences;

    public ListListeners(MultiBoxGui multiBoxGui, Preferences preferences){
        this.multiBoxGui = multiBoxGui;
        this.preferences = preferences;
    }


    public class SpellListListener implements ListSelectionListener{

        @Override
        public void valueChanged(ListSelectionEvent e) {



            int selectedIndex = multiBoxGui.castSpellList.getSelectedIndex();
            String spell = multiBoxGui.spellListModel.getElementAt(selectedIndex).toString();

            multiBoxGui.manualCastLabel.setText(spell);
            preferences.put(Constants.SPELL, spell);

        }
    }

    public class PotionListListener implements ListSelectionListener{

        @Override
        public void valueChanged(ListSelectionEvent e) {

            int selectedIndex = multiBoxGui.potionList.getSelectedIndex();
            multiBoxGui.drinkLabel.setText(multiBoxGui.potionListModel.getElementAt(selectedIndex).toString());
            preferences.put(Constants.POTION_ITEMS, multiBoxGui.PotionField.getText().toString());
        }
    }

    public class WorldHopperListener implements ListSelectionListener{
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = multiBoxGui.worldHopperList.getSelectedIndex();
            preferences.putInt(Constants.WORLD, index);
        }
    }

    public class LeadUserListener implements ListSelectionListener{


        @Override
        public void valueChanged(ListSelectionEvent e) {
            log("test selected");
            multiBoxGui.selectLeadUserLbl.setText("Lead User: " +
                    multiBoxGui.leadUserList.getSelectedValue().toString());
        }
    }
}


