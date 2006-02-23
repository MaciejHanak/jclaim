package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.gui.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Helps easy maintenance the status panel.
 * 
 * @author Alex Rass
 * @since Jan 29, 2006
 */
public class StatusPanel extends GradientPanel implements ActionListener {
    JCheckBox cbAway = new JCheckBox("Away");
    JLabel label = new JLabel();
    public StatusPanel() {
        super(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        cbAway.addActionListener(this);
        cbAway.setSelected(ClientProperties.INSTANCE.isIamAway());
        cbAway.setToolTipText("Global Away");
        add(cbAway);
        label.setIcon(ImageCacheUI.ICON_JC.getIcon());
        add(label);
    }

    /**
     * Allows others to set this.
     * @param on for Away
     */
    void setAway(boolean on) {
        cbAway.setSelected(on);
    }

    public void actionPerformed(ActionEvent e) {
        MenuManager.setGlobalAway(cbAway.isSelected());
    }

    public void setVisible(boolean aFlag) {
        if (aFlag) {
            label.setIcon(ImageCacheUI.ICON_JC_ANIM.getIcon());
            super.setVisible(true);
        } else {
            label.setIcon(ImageCacheUI.ICON_JC.getIcon());
            super.setVisible(ClientProperties.INSTANCE.isStatusbarAlwaysVisible());
        }
    }
}
