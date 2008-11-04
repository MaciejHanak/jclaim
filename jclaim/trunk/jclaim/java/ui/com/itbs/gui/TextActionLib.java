package com.itbs.gui;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Text actions supplied by java seem to work poorly.
 * 
 * Much simpler implementations are supplied here.
 *
 * @author Alex Rass
 * @since Mar 12, 2008 4:00:16 PM
 */
public class TextActionLib {
    public static final Action copy = new  CopyAction();
    public static final Action paste = new  PasteAction();
    public static final Action cut = new  CutAction();

    /**
      * Cuts the selected region and place its contents
      * into the system clipboard.
      * <p>
      * <strong>Warning:</strong>
      * Serialized objects of this class will not be compatible with
      * future Swing releases. The current serialization support is
      * appropriate for short term storage or RMI between applications running
      * the same version of Swing.  As of 1.4, support for long term storage
      * of all JavaBeans<sup><font size="-2">TM</font></sup>
      * has been added to the <code>java.beans</code> package.
      * Please see {@link java.beans.XMLEncoder}.
      *
      * @see DefaultEditorKit#cutAction
      * @see DefaultEditorKit#getActions
      */
     public static class CutAction extends TextAction {

         /** Create this object with the appropriate identifier. */
         public CutAction() {
             super(DefaultEditorKit.cutAction);
         }

         /**
          * The operation to perform when this action is triggered.
          *
          * @param e the action event
          */
         public void actionPerformed(ActionEvent e) {
             JTextComponent target = getTextComponent(e);
             if (target != null) {
//                 target.cut();
                 if (target.getCaret().getDot() != target.getCaret().getMark()) {
                     Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(target.getSelectedText()), null);
                     target.replaceSelection("");
                 }
             }
         }
     }

     /**
      * Copies the selected region and place its contents
      * into the system clipboard.
      * <p>
      * <strong>Warning:</strong>
      * Serialized objects of this class will not be compatible with
      * future Swing releases. The current serialization support is
      * appropriate for short term storage or RMI between applications running
      * the same version of Swing.  As of 1.4, support for long term storage
      * of all JavaBeans<sup><font size="-2">TM</font></sup>
      * has been added to the <code>java.beans</code> package.
      * Please see {@link java.beans.XMLEncoder}.
      *
      * @see DefaultEditorKit#copyAction
      * @see DefaultEditorKit#getActions
      */
     public static class CopyAction extends TextAction {

         /** Create this object with the appropriate identifier. */
         public CopyAction() {
             super(DefaultEditorKit.copyAction);
         }

         /**
          * The operation to perform when this action is triggered.
          *
          * @param e the action event
          */
         public void actionPerformed(ActionEvent e) {
             JTextComponent target = getTextComponent(e);
             if (target != null) {
//                 target.copy();
                 if (target.getCaret().getDot() != target.getCaret().getMark()) {
                     Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(target.getSelectedText()), null);
                 }
             }
         }
     }

     /**
      * Pastes the contents of the system clipboard into the
      * selected region, or before the caret if nothing is
      * selected.
      * <p>
      * <strong>Warning:</strong>
      * Serialized objects of this class will not be compatible with
      * future Swing releases. The current serialization support is
      * appropriate for short term storage or RMI between applications running
      * the same version of Swing.  As of 1.4, support for long term storage
      * of all JavaBeans<sup><font size="-2">TM</font></sup>
      * has been added to the <code>java.beans</code> package.
      * Please see {@link java.beans.XMLEncoder}.
      *
      * @see javax.swing.text.DefaultEditorKit#pasteAction
      * @see javax.swing.text.DefaultEditorKit#getActions
      */
     public static class PasteAction extends TextAction {

         /** Create this object with the appropriate identifier. */
         public PasteAction() {
             super(DefaultEditorKit.pasteAction);
         }

         /**
          * The operation to perform when this action is triggered.
          *
          * @param e the action event
          */
         public void actionPerformed(ActionEvent e) {
             JTextComponent target = getTextComponent(e);
             if (target != null) {

                 try {
                     target.replaceSelection(""+Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor));
                 } catch (UnsupportedFlavorException e1) {
                     e1.printStackTrace();
                 } catch (IOException e1) {
                     e1.printStackTrace();  
                 }
             }
         }
     }

} // class
