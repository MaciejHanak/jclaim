/*
 * Copyright (c) 2006, ITBS LLC. All Rights Reserved.
 *
 *     This file is part of JClaim.
 *
 *     JClaim is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; version 2 of the License.
 *
 *     JClaim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JClaim; if not, find it at gnu.org or write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.itbs.aimcer.gui.order;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.MessageImpl;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.ComponentFactory;
import com.itbs.aimcer.gui.Main;
import com.itbs.gui.BetterTextField;
import com.itbs.gui.ErrorDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Alex Rass
 * @since Feb 20, 2006
 */
abstract public class OrderEntryBase extends JPanel {
    protected static final String TITLE_SYMBOL = "Symbol";
    protected static final String TITLE_PRICE = "Price";
    protected static final String TITLE_QTY = "Qty";
    protected static final String TITLE_COMMENT = "Comment";
    public static final String ORDER_TO = "IOI ";
    public static final DateFormat timeFormat = new SimpleDateFormat("<hh:mm:ss>");
    final static Color COLOR_BUY = Color.GREEN.darker(), COLOR_SELL = Color.RED;

    protected BetterTextField symbol, qty, price;
    protected BetterTextField commission;
    protected JComboBox market;

    protected MutableAttributeSet ATT_BUY, ATT_SELL, ATT_NORMAL;


    protected OrderEntryBase() {
        super();
        ATT_NORMAL = new SimpleAttributeSet();
        StyleConstants.setFontFamily(ATT_NORMAL,"Monospaced");
        StyleConstants.setFontSize(ATT_NORMAL, ClientProperties.INSTANCE.getFontSize());

        ATT_BUY = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        StyleConstants.setBold(ATT_BUY, true);
        ATT_SELL = (MutableAttributeSet) ATT_BUY.copyAttributes();
        StyleConstants.setForeground(ATT_BUY, COLOR_BUY);
        StyleConstants.setForeground(ATT_SELL, COLOR_SELL);

        setBorder(new LineBorder(Color.BLACK));
    }

    protected void addSQP(JPanel center) {
        //      Symbol:
        symbol = new BetterTextField(5);
        ComponentFactory.fixWidget(symbol, TITLE_SYMBOL);
        symbol.setMaxLength(6);
        addCaretListener(symbol);
        center.add(symbol);

//      Qty:
        qty = getQtyEditComponent(TITLE_QTY);
        addCaretListener(qty);
        center.add(qty);

//      Price:
        price = getPriceEditComponent(TITLE_PRICE);
        addCaretListener(price);
        center.add(price);
    }

    /**
     * Instead of listeners, just code which button whould be ok when.
     * @param comp to monitor
     */
    abstract void addCaretListener(JComponent comp);

    protected static BetterTextField getQtyEditComponent(String title) {
        BetterTextField editComp = new BetterTextField(5);
//        editComp.setMaxLength(5);
        editComp.setDocument(new QtyDocument(5));
        ComponentFactory.fixWidget(editComp, title);
        return editComp;
    }

    protected static BetterTextField getPriceEditComponent(String title) {
        BetterTextField editComp = new BetterTextField(7);
        editComp.setDocument(new PriceDocument(8));
        ComponentFactory.fixWidget(editComp, title);
        return editComp;
    }

    /** @see javax.swing.text.PlainDocument */
    protected static class QtyDocument extends BetterTextField.LengthDocument
    {
        public QtyDocument(int charCount) {
            super(charCount);
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null)
                return;
            str = str.toLowerCase();
            for (int i = 0; i < str.length(); i++) {
                char letter = str.charAt(i);
                if ((getLength()==0 || getLength() > 2 ) && letter == 'k')
                    continue;
                if (getLength() > 1 && getText(getLength() - 1, 1).equals("k"))
                    return;
                if (Character.isDigit(letter) || letter == 'k')
                    super.insertString(offs+i, ""+letter, a); //getLength()
            }
        }
    }

    /** @see javax.swing.text.PlainDocument */
    protected static class PriceDocument extends BetterTextField.LengthDocument
    {
        public PriceDocument(int charCount) {
            super(charCount);
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null)
                return;
            str = str.toLowerCase();
            for (int i = 0; i < str.length(); i++) {
                char letter = str.charAt(i);
                if (Character.isDigit(letter) || letter == '.')
                    super.insertString(offs+i, ""+letter, a);
            }
        }
    } // class PriceDocument

    protected class OrderAction extends AbstractAction {
        private String actionType;
        private Connection connection;
        final private Contact name;

        protected OrderAction(String actionType,
                            Connection connection, Contact name) {
            this.actionType = actionType;
            this.connection = connection;
            this.name = name;
        }

        public void actionPerformed(ActionEvent e) {
            String error = "log the order";
            try {
                Message message = new MessageImpl(name, true, constructMessage(actionType));
                //Main.getLogger().log(message);
                error = "display the order on screen";
                Main.globalWindowHandler.addTextToHistoryPanel(name, message, false);
//                historyPane.appendHistoryText(, style);
                error = "send the order through";
                ((MessageSupport) connection).sendMessage(message);
                // clear order
                resetEntry();
            } catch (Exception exc) {
                ErrorDialog.displayError(OrderEntryBase.this, "Failed to " + error + ".\nPlease correct the problem and retry.", exc);
            }
        }

        String constructMessage(String actionType) {
            String quantity = (qty.getText().endsWith("k")?qty.getText().substring(0, qty.getText().length() -1)+"000":qty.getText());
            String fullPrice = price.getText().trim().length()==0?"MKT":price.getText();
            return new OrderEntryItem(connection, new Date(), null, null,
                    symbol.getText(),
                    fullPrice,
                    market==null?null:(String) market.getSelectedItem(),
                    quantity,
                    null,
                    actionType,
                    commission==null?null:commission.getText(),
                    false, false
                    ).toString();
//                ORDER_TO + actionType + ":"
//                        + (qty.getText().endsWith("k")?qty.getText().substring(0, qty.getText().length() -1)+"000":qty.getText())
//                        + " " + symbol.getText() + " " + market.getSelectedItem() + " at " + (price.getText().trim().length()==0?"MKT":price.getText()) +  ((commission==null || commission.getText().trim().length() == 0)?"":" with " + commission.getText());
        }
    } // class OrderAction

    protected void resetEntry() {
        symbol.setText("");
        qty.setText("");
        price.setText("");
        if (market!=null)
            market.setSelectedIndex(0);
        if (commission!=null)
            commission.setText("");
        symbol.requestFocus();
    }


} // class
