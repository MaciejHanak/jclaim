package com.itbs.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashSet;

/**
 * Class from the book.
 * Cleaned up a little
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

    HashSet selectionCache = new HashSet();

    public CheckBoxJList() {
        super();
        setCellRenderer (new CheckBoxListCellRenderer());
        addListSelectionListener (this);
    }

    // ListSelectionListener implementation
    public void valueChanged (ListSelectionEvent lse) {
//        System.out.println (lse);
        if (! lse.getValueIsAdjusting()) {
            removeListSelectionListener (this);

            // remember everything selected as a result of this action
            HashSet newSelections = new HashSet();
            int size = getModel().getSize();
            for (int i=0; i<size; i++) {
                if (getSelectionModel().isSelectedIndex(i)) {
                    newSelections.add (i);
                }
            }

            // turn on everything that was previously selected
            for (Object aSelectionCache : selectionCache) {
                int index = (Integer) aSelectionCache;
//                System.out.println("adding " + index);
                getSelectionModel().addSelectionInterval(index, index);
            }

            // add or remove the delta
            for (Object newSelection : newSelections) {
                Integer nextInt = (Integer) newSelection;
                int index = nextInt;
                if (selectionCache.contains(nextInt))
                    getSelectionModel().removeSelectionInterval(index, index);
                else
                    getSelectionModel().addSelectionInterval(index, index);
            }

            // save selections for next time
            selectionCache.clear();
            for (int i=0; i<size; i++) {
                if (getSelectionModel().isSelectedIndex(i)) {
//                    System.out.println ("caching " + i);
                    selectionCache.add (i);
                }
            }

            addListSelectionListener (this);

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


    class CheckBoxListCellRenderer extends JComponent 
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
