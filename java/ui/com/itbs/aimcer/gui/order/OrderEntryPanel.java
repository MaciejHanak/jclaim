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
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.gui.ComponentFactory;
import com.itbs.aimcer.gui.MessageWindow;
import com.itbs.gui.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * @author Alex Rass
 * @since Oct 21, 2004
 *
 * TODO
 *   Add stt when a person cancels an order, a message is sent to the other party.  Add a way to confirm too.
 *  IOI for Order to
 */
public class OrderEntryPanel extends OrderEntryBase {
    private Action BUY, SELL;

/*
    private static final String[] MARKET_OPTIONS = {
        "LMT",
        "MKT",
        "IOC",
        "MOC",
        "MOO"
    };
    private static final String[] ORDER_OPTIONS = {
        "DAY",
        "GTC",
        "GTD",
        "FOK",
        "IOC"
    };
*/
    private static final String[] TIF_OPTIONS = {
        "DAY",
        "GTC",
        "FOK",
        "AON",
        "IOC",
        "MOO",
        "MOC",
        "EXT",
    };

    static final String ACTION_TYPE_BUY = "buy";
    static final String ACTION_TYPE_SELL = "sell";

    public OrderEntryPanel(Connection connection, Nameable name, MessageWindow historyPane) {
        super();

        BUY = new ActionAdapter("Buy", new OrderAction(ATT_BUY, ACTION_TYPE_BUY, connection, name, historyPane), 'B');
        BUY.setEnabled(false);
        SELL = new ActionAdapter("Sell", new OrderAction(ATT_SELL, ACTION_TYPE_SELL, connection, name, historyPane), 'L');
        SELL.setEnabled(false);

        JButton btn;
        btn = new BetterButton(BUY);
        btn.setBackground(Color.GREEN.darker());
        btn.setForeground(Color.WHITE);
        add(btn);

        addSQP(this);

        add(market = new JComboBox(TIF_OPTIONS));
        ComponentFactory.fixWidget(market, "TIF");
//        add(type = new JComboBox(ORDER_OPTIONS));

        if (ClientProperties.INSTANCE.isAllowCommissionEntry()) {
            commission = new BetterTextField(7);
            commission.setDocument(new PriceDocument(8));
            ComponentFactory.fixWidget(commission,"Commission");
            add(commission);
        }

        btn = new BetterButton(SELL);
        btn.setBackground(Color.RED);
        btn.setForeground(Color.WHITE);
        add(btn);
    } // OrderEntryPanel


    protected void addCaretListener(JComponent comp) {
        ((JTextComponent)comp).addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                boolean enabled =
                        symbol.getText().trim().length()!=0 &&
                        qty.getText().trim().length()!=0;
                BUY.setEnabled(enabled);
                SELL.setEnabled(enabled);
            }
        });
    }


    public static class IOIUpdateDialog extends JDialog {
        OrderEntryItem orderItem;
        IOIUpdateDialog(final JFrame owner, final OrderEntryItem orderItem) {
            super(owner, "IOI Update", true);
            this.orderItem = orderItem;
            GUIUtils.addCancelByEscape(this);

            // Prep these early so we can use them in action adapters
            final BetterTextField uQty = getQtyEditComponent("Ex-Qty");
            final BetterTextField uPrice = getPriceEditComponent("Ex-Price");
            final BetterTextField remain = new BetterTextField(5);
            final JTextComponent comment = new BetterTextPane();

            final ActionAdapter aPartial = new ActionAdapter("Partial", "Confirm partial order", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orderItem.setRemaining(remain.getText());
                    orderItem.setComment(comment.getText() + " \n" + e.getActionCommand() + " " + uQty.getText() + "@" + uPrice.getText() + " on " + new Date());
                    OrderEntryLog.notifyUser(orderItem, "(IOI Partial Fill -- " + orderItem + " --  Executed " + uQty.getText() + "@" + uPrice.getText() + " Lvs " + remain.getText() + " on " + new Date()+ ")");
                    IOIUpdateDialog.this.setVisible(false);
                }
            }, 'P');
            aPartial.setEnabled(false);
            final ActionAdapter aFill = new ActionAdapter("Fill", "Confirm full order", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orderItem.setComment(comment.getText() + " \n" + e.getActionCommand() + " " + orderItem.getRemaining() + "@" + uPrice.getText() + " on " + new Date());
                    OrderEntryLog.notifyUser(orderItem, "(IOI Fill -- " + orderItem + " --  Executed on " + new Date() + ")");
                    orderItem.setRemaining(0);
                    orderItem.setDone(Boolean.TRUE);
                    IOIUpdateDialog.this.setVisible(false);
                }
            }, 'F');
            final ActionAdapter aClose = new ActionAdapter("Close", "Does nothing", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orderItem.setComment(comment.getText());
                    IOIUpdateDialog.this.setVisible(false);
                }
            }, 'S');
            final ActionAdapter aCancel = new ActionAdapter("Cancel", "Cancel the order", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orderItem.setInvalid(Boolean.TRUE);
                    orderItem.setComment(orderItem.getComment() + " \n" + getName() + " on " + new Date());
                    OrderEntryLog.notifyUser(orderItem, "(IOI Cancel -- " + orderItem + " --  cancelled)");
                    IOIUpdateDialog.this.setVisible(false);
                }
            }, 'C');

            setLayout(new GridLayout(0,1));

//        private Component addConstants() {
            JPanel panel = new JPanel();
            BetterTextField sym = new BetterTextField(orderItem.getSymbol(), 5);
            ComponentFactory.fixWidget(sym, TITLE_SYMBOL);
            sym.setEditable(false);
            panel.add(sym);
            JTextField buy = new BetterTextField(orderItem.getBuy(),  5);
            ComponentFactory.fixWidget(buy, "Side");
            buy.setEditable(false);
            panel.add(buy);
            BetterTextField  qty = getQtyEditComponent(TITLE_QTY);
            qty.setText(orderItem.getQty());
            qty.setEditable(false);
            panel.add(qty);
            BetterTextField price = getPriceEditComponent(TITLE_PRICE);
            price.setText(orderItem.getPrice());
            price.setEditable(false);
            panel.add(price);
            add(panel);

//        private Component addEditables() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            uQty.setText(""+orderItem.getRemaining());
            panel.add(uQty);
            uPrice.setText(orderItem.getPrice());
            panel.add(uPrice);
            ComponentFactory.fixWidget(remain, "Lvs Qty");
            remain.setEditable(false);
            panel.add(remain);
            panel.setBorder(new BevelBorder(BevelBorder.LOWERED));

            uQty.addCaretListener(new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    int newQty=0;
                    if (uQty.getText().length() != 0)
                        newQty = orderItem.getRemaining() - Integer.parseInt(uQty.getText());
                    aPartial.setEnabled(newQty != 0);
                    if (newQty == 0) {
                        remain.setText("");
                    } else {
                        remain.setText(""+newQty);
                        remain.setForeground(newQty>0?Color.LIGHT_GRAY:Color.RED);
                    }
                }
            });
            add(panel);

            ComponentFactory.fixWidget(comment, TITLE_COMMENT);
            comment.setText(orderItem.getComment());
            add(new JScrollPane(comment));

//        private Component addButtons() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
            panel.add(new BetterButton(aPartial));
            panel.add(new BetterButton(aFill));
            panel.add(new BetterButton(aCancel));
            panel.add(new BetterButton(aClose));
            add(panel);
            pack();
        }

    } // class IOIUpdateDialog


}
