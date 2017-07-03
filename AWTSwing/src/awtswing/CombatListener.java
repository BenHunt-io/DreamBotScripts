/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awtswing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Ben
 */
public class CombatListener implements ActionListener {
    
    private MultiBoxGui multiBoxGui;
    
    public CombatListener(MultiBoxGui multiBoxGui){
     
        this.multiBoxGui = multiBoxGui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        multiBoxGui.meleeButton.setText("test");
    }
    
    
    
}
