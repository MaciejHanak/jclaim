package com.itbs.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Class from the book.
 * Cleaned up a little
 *
 * Not synchronized, make sure all model changes and UI calls are made on the UI Thread. (AR) 
 */
public class CheckBoxJList extends JList
    implements ListSelectionListener {

    static Color listForeground, listBackground,
        listSelectionForeground,
        listSelectionBackground;

    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        listForeground =  uid.getColor ("List.foreground");
        listBackground =  uid.getColor ("List.background");
        listSelectionForeground =  uid.getColor ("List.selectionForeground");
        listSelectionBackground =  uid.getColor ("List.selectionBackground");
    }

    Set <Integer> selectionCache = new CopyOnWriteArraySet<Integer>();

    public CheckBoxJList() {
        super();
        setCellRenderer (new CheckBoxListCellRenderer());
        addListSelectionListener (this);
    }

    /**
     * ListSelectionListener implementation.
     * Used to check off the checkboxes when item is selected.
     * Called whenever the value of the selection changes.
     * @param lse selection event object
     */
    public void valueChanged (ListSelectionEvent lse) {
//        if(true) return;
//        System.out.println (lse);
        if (! lse.getValueIsAdjusting()) {
            removeListSelectionListener (this);

            // remember everything selected as a result of this action
            HashSet <Integer> newSelections = new HashSet<Integer>();
            int size = getModel().getSize();
            for (int i=0; i<size; i++) {
                if (getSelectionModel().isSelectedIndex(i)) {
                    newSelections.add (i);
                }
            }

            // turn on everything that was previously selected
            for (Integer index : selectionCache) {
//                System.out.println("adding " + index);
                getSelectionModel().addSelectionInterval(index, index);
            }

            // add or remove the delta (go through newSelections now)
            for (Integer index : newSelections) {
                if (selectionCache.contains(index))
                    getSelectionModel().removeSelectionInterval(index, index);
                else
                    getSelectionModel().addSelectionInterval(index, index);
            }

            cacheSelection(size);
            addListSelectionListener (this);
        }
    }

    private void cacheSelection(int size) {
        // save selections for next time
        selectionCache.clear();
        for (int i=0; i<size; i++) {
            if (getSelectionModel().isSelectedIndex(i)) {
//                    System.out.println ("caching " + i);
                selectionCache.add (i);
            }
        }
    }


    public static void main (String[] args) {
        JList list = new CheckBoxJList ();
        DefaultListModel defModel = new DefaultListModel();
        list.setModel (defModel);
        String[] listItems = {"Chris", "Joshua", "Daniel", "Michael", "Don", "Kimi", "Kelly", "Keagan"};
        for (String s : listItems) {
            defModel.addElement(s);
        }       
        // show list
        JScrollPane scroller = new JScrollPane (list,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JFrame frame = new JFrame ("Checkbox JList");
        frame.getContentPane().add (scroller);
        frame.pack();
        frame.setVisible(true);
    }

    public void setSelectionInvert() {
        removeListSelectionListener (this);

        selectionCache.clear();
        
        ListSelectionModel model = getSelectionModel();
        java.util.List<Integer> selectedIndecies = new ArrayList<Integer>();
        int size = getModel().getSize();
        for(int i=0; i < size; i++) {
            if (model.isSelectedIndex(i)) {
                selectedIndecies.add(i);
            }
        }
        // setAll
        setSelectionInterval(0, getModel().getSize());
//        model.clearSelection();
        // set selection
/*
        for(Integer index:selectedIndecies) {
            model.addSelectionInterval(index, index);
        }
*/
        for(Integer index:selectedIndecies) {
            model.removeSelectionInterval(index, index);
        }

        cacheSelection(size);
        addListSelectionListener (this);
    }

    public void setSelectAll() {
        removeListSelectionListener (this);

        selectionCache.clear();
        setSelectionInterval(0, getModel().getSize());

        cacheSelection(getModel().getSize());
        addListSelectionListener (this);
    }

    public void setSelectionNone() {
        removeListSelectionListener (this);

        selectionCache.clear();
        getSelectionModel().removeSelectionInterval(0, getModel().getSize());
        
        addListSelectionListener (this);

    }

    /**
     * Does the drawing of the 2 components as one.
     */
    static class CheckBoxListCellRenderer extends JComponent
        implements ListCellRenderer {
        DefaultListCellRenderer defaultComp;
        JCheckBox checkbox;
        public CheckBoxListCellRenderer() {
            setLayout (new BorderLayout());
            defaultComp = new DefaultListCellRenderer();
            checkbox = new JCheckBox();
            add (checkbox, BorderLayout.WEST);
            add (defaultComp, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object  value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus){
            defaultComp.getListCellRendererComponent (list, value, index,
                                                      isSelected, cellHasFocus);
            /*
            checkbox.setSelected (isSelected);
            checkbox.setForeground (isSelected ?
                                    listSelectionForeground :
                                    listForeground);
            checkbox.setBackground (isSelected ?
                                    listSelectionBackground :
                                    listBackground);
            */
            checkbox.setSelected (isSelected);
            for (Component comp : getComponents()) {
                comp.setForeground(listForeground);
                comp.setBackground(listBackground);
            }
            return this;
        }
    }
}
