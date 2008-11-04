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

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.aimcer.gui.Main;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.ErrorDialog;
import com.itbs.gui.GUIUtils;
import com.itbs.util.GeneralUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This is the Order Entrt Tracking screen.
 *
 * @author Alex Rass on Nov 2, 2004
 */
public class OrderEntryLog implements ConnectionEventListener {
    static OrderEntryLog instance = new OrderEntryLog();
    JFrame frame;
    JTable table;
    static Rectangle bounds = new Rectangle(100, 100, 600, 400);
    private static final String TITLE = "Order Entry Tracking";

    private static final String[] COLUMNS = {"Time", "From", "To", "B/S", "Symb", "Qty", "Price", "Market", "Commission",
                               "Cancel", "Done", "Comment"};
    /**  Constants that control the table column order */
    private static final int COL_TIME = 0;
    private static final int COL_FROM = 1;
    private static final int COL_TO   = 2;
    private static final int COL_BUY  = 3;
    private static final int COL_SYMBOL     = 4;
    private static final int COL_QTY        = 5;
    private static final int COL_PRICE      = 6;
    private static final int COL_MARKET     = 7;
    private static final int COL_COMMISSION  = 8;
    private static final int COL_INVALID    = 9;
    private static final int COL_DONE       = 10;
    private static final int COL_COMMENT    = 11;
    private static final String LOG_FILE = "trading.log";

    private OrderEntryLog() {
    }

    /**
     * Singleton.
     * @return instance
     */
    public static OrderEntryLog getInstance() {
        return instance;
    }

    /**
     * Shows the frame.  New or not.
     */
    public void showFrame() {
        if (frame == null || !frame.isDisplayable()) {
            frame = GUIUtils.createFrame(TITLE);
            frame.setBounds(bounds);
            GUIUtils.addCancelByEscape(frame);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(getSpreadSheet());
            frame.getContentPane().add(getButtons(), BorderLayout.SOUTH);
            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    savePosition();
                }
                public void componentMoved(ComponentEvent e) {
                    savePosition();
                }
            });
            reloadData();
        }
        frame.setVisible(true);
    }

    private void reloadData() {
        // load data
        File clientTradeLog = new File(ClientProperties.INSTANCE.getLogPath(), LOG_FILE);
        if (clientTradeLog.exists() && clientTradeLog.isFile()) {
            List <OrderEntryItem> data;
            XMLDecoder d = null;
            try {
/*
                boolean newFormat = true;
                RandomAccessFile raf = new RandomAccessFile(clientTradeLog, "r");
                if (raf.length()>0) {
                    newFormat = raf.readChar() != '<';
                }
                raf.close();
                if (newFormat)
*/
                d = new XMLDecoder(new GZIPInputStream(new FileInputStream(clientTradeLog)));
/*
                else
                    d = new XMLDecoder(new FileInputStream(clientTradeLog));
*/
                data = (List <OrderEntryItem>) d.readObject();
                ((OrderTableModel)table.getModel()).setData(data);
            } catch (IOException e) {
                if (e.getMessage().startsWith("Not in ") && e.getMessage().endsWith(" format")) {
                    ErrorDialog.displayError(frame, "File is corrupt.\n", e);
                } else {
                    ErrorDialog.displayError(frame, "Failed to load trading history\n" + e.getMessage(), e);
                }
            } catch (Exception e) {
                ErrorDialog.displayError(frame, "Failed to load trading history\n" + e.getMessage(), e);
            } finally {
                if (d!=null)
                    d.close();
            }
        }
    }

    private void saveData() {
//        if (true)
//            return;
        File clientTradeLog = new File(ClientProperties.INSTANCE.getLogPath(), LOG_FILE);
        XMLEncoder e = null;
        try {
            clientTradeLog.delete();
            final OutputStream out = new GZIPOutputStream(new FileOutputStream(clientTradeLog));
            e = new XMLEncoder(out);
            e.writeObject(((OrderTableModel)table.getModel()).getData());
//            out.flush();
//            out.close();
        } catch (Exception ex) {
            ErrorDialog.displayError(frame, "Failed to save trading history", ex);
        } finally {
            if (e!=null)
                e.close();
        }
    }

    private Component getButtons() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel.setOpaque(false);
        panel.add(new JButton(new ActionAdapter("Export", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "Not available in demo mode");
                // todo prompt for filename
                for (Object o : ((OrderTableModel) table.getModel()).getData()) {
                    OrderEntryItem oei = (OrderEntryItem) o;
                    // todo get tabbed representation
                    // todo write it out
                }
            }
        },'X')));
        panel.add(new JButton(new ActionAdapter("Delete Cancelled", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Iterator data = ((OrderTableModel)table.getModel()).getData().iterator();
                while (data.hasNext()) {
                    OrderEntryItem oei =  (OrderEntryItem) data.next();
                    if (oei.invalid)
                        data.remove();
                }
                ((OrderTableModel)table.getModel()).fireTableChanged(new TableModelEvent(table.getModel()));
                saveData();
            }
        },'I')));
        panel.add(new JButton(new ActionAdapter("Delete Executed", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Iterator data = ((OrderTableModel)table.getModel()).getData().iterator();
                while (data.hasNext()) {
                    OrderEntryItem oei =  (OrderEntryItem) data.next();
                    if (oei.done)
                        data.remove();
                }
                ((OrderTableModel)table.getModel()).fireTableChanged(new TableModelEvent(table.getModel()));
                saveData();
            }
        },'D')));
        return panel;
    }

    /**
     * Create the table with fields.
     * @return panel with table
     */
    private Component getSpreadSheet() {
        table = new JTable(new OrderTableModel());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ((DefaultTableModel)table.getModel()).setColumnIdentifiers(COLUMNS);
        table.setFocusCycleRoot(false); // if this has to be removed due to compatibility with 1.4,
        // please add a isManagingFocus() method which returns false
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        table.setPreferredScrollableViewportSize(new Dimension(550, 200));

        for (int inx = 0; inx < COLUMNS.length; inx++)
        {
            TableColumn tc = table.getColumn(COLUMNS[inx]);
            int width;
//            tc.setResizable(false);
            switch(inx) {
                case COL_TIME:     width = 50;  setupBackground(tc);      break;
                case COL_FROM:     width = 50;  setupBackground(tc);      break;
                case COL_TO:       width = 50;  setupBackground(tc);      break;
                case COL_BUY:      width = 50;  setupBackground(tc);      break;
                case COL_SYMBOL:   width = 50;  setupBackground(tc);      break;
                case COL_QTY:      width = 50;  setupBackground(tc);      break;
                case COL_PRICE:    width = 50;  setupBackground(tc);      break;
                case COL_MARKET:   width = 50;  setupBackground(tc);      break;
                case COL_COMMISSION: width = ClientProperties.INSTANCE.isAllowCommissionEntry()?50:3;  setupBackground(tc);      break;
                case COL_INVALID:  width = 30; setupCheckBoxEditor(tc);   break;  // cancel
                case COL_DONE:     width = 20; setupCheckBoxEditor(tc);   break;
                case COL_COMMENT:  width = 150;    break;
                default: throw new IllegalArgumentException("Unknown column: " + inx);
            }
            tc.setPreferredWidth(width);
        }
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        OrderTableModel model = (OrderTableModel)table.getModel();
                        JDialog dlg = new OrderEntryPanel.IOIUpdateDialog(frame,(model).data.get(row));
                        dlg.setVisible(true);
                        model.fireTableRowsUpdated(row, row);
                    }
                }
            }
        });

        return new JScrollPane(table);
    }

    /**
     * Sticks a checkbox to every  cell in a column
     * @param tc TableColumn
     */
    private void setupCheckBoxEditor(TableColumn tc) {
        JCheckBox jcb = new JCheckBox();
        //remove the last choice, it's only there for some DB rows created
        //outside of this tool
        tc.setCellEditor(new DefaultCellEditor(jcb));
        tc.setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Boolean bvalue = (Boolean) value;
                JCheckBox jcb = new JCheckBox();
                jcb.setSelected(bvalue);
//                System.out.println("cell Renderer" + new Date());
                return jcb;
            }
        });
    }
    /**
     * Sticks a background based on item to every  cell in a column
     * @param tc TableColumn
     */
    private void setupBackground(TableColumn tc) {
        tc.setCellRenderer(new DefaultTableCellRenderer() {
            boolean strike;
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table,  value, isSelected,  hasFocus, row, column);
                strike = false;
                OrderEntryItem item = ((OrderTableModel)table.getModel()).getData().get(row);
                if (!isSelected) {
                    boolean buy = "buy".equalsIgnoreCase(item.getBuy());
                    boolean sell = "sell".equalsIgnoreCase(item.getBuy());
                    if ((item.getDone()) || (item.getInvalid()))  {
                        comp.setBackground(Color.WHITE);
                        comp.setForeground(Color.GRAY);
                    } else {
                        comp.setBackground(buy?OrderEntryPanel.COLOR_BUY:(sell?OrderEntryPanel.COLOR_SELL:Color.WHITE));
                        comp.setForeground(Color.BLACK);
                    }
                }
                if (item.getInvalid())
                    strike = true;


                return comp;
            }

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if ( strike )
                {
                  g.setColor( getForeground() );
                  int midpoint = getHeight() / 2;
                  g.drawLine( 0, midpoint, getWidth()-1, midpoint );
                }
            }
        });
    }

    private void savePosition() {
        bounds = frame.getBounds();
    }

    // ***************************************************************************************
    // **************************  Order Table Model  ****************************************
    // ***************************************************************************************

    /** @see javax.swing.table.DefaultTableModel **/
    public static class OrderTableModel extends DefaultTableModel {
        /** holds the table data **/
        private List <OrderEntryItem> data = new ArrayList<OrderEntryItem>();

        /** @see DefaultTableModel **/
        public int getRowCount() {
            return data == null? 0 : data.size();
        }

        /** @see DefaultTableModel **/
        public Object getValueAt(int row, int column) {
            OrderEntryItem item = data.get(row);
            switch (column) {
                case COL_TIME:    return item.time;
                case COL_FROM:    return item.from;
                case COL_TO:      return item.to;
                case COL_BUY:     return item.buy;
                case COL_SYMBOL:  return item.symbol;
                case COL_QTY:     return item.qty;
                case COL_PRICE:   return item.price;
                case COL_MARKET:  return item.market;
                case COL_COMMISSION: return item.commission;
                case COL_INVALID: return item.invalid;
                case COL_DONE:    return item.done;
                case COL_COMMENT: return item.comment;
                default: throw new IllegalArgumentException("Wrong column: " + column);
            }
        }

        /** @see DefaultTableModel **/
        public void setValueAt(Object aValue, int row, int column)
        {
            OrderEntryItem item = data.get(row);
            switch (column) {
                case COL_TIME:    item.time    = (Date) aValue;    break;
                case COL_FROM:    item.from    = (String) aValue;  break;
                case COL_TO:      item.to      = (String) aValue;  break;
                case COL_BUY:     item.buy     = (String) aValue; break;
                case COL_SYMBOL:  item.symbol  = (String) aValue;  break;
                case COL_QTY:     item.qty     = (String) aValue;  break;
                case COL_PRICE:   item.price   = (String) aValue;  break;
                case COL_MARKET:  item.market  = (String) aValue;  break;
                case COL_COMMISSION: item.commission    = (String) aValue;  break;
                case COL_INVALID: item.invalid = (Boolean) aValue;
                                  notifyUserOfCancel(item); break;
                case COL_DONE:    item.done    = (Boolean) aValue;
                                  notifyUserOfFill(item); break;
                case COL_COMMENT: item.comment = (String) aValue;  break;
                default: throw new IllegalArgumentException("Wrong column: " + column);
            }
            OrderEntryLog.getInstance().saveData();
        }

        private void notifyUserOfCancel(OrderEntryItem item) {
            notifyUser(item, "(IOI Cancel -- " + item +
                            (item.invalid?" -- cancelled.)":" -- is no longer cancelled.)"));
        }
        private void notifyUserOfFill(OrderEntryItem item) {
            notifyUser(item, "(IOI Fill -- " + item +
                            (item.done?" -- executed on " + new Date() + ")":" was not filled.)"));
        }

        /** @see DefaultTableModel **/
        public boolean isCellEditable(int row, int column)
        {
            return column == COL_COMMENT || column == COL_DONE || column == COL_INVALID;
        }

        /**
         * Sets the table data
         * @param item the table data
         */
        public void addData(OrderEntryItem item)
        {
            data.add(item);
            OrderEntryLog.getInstance().saveData();
            fireTableChanged(new TableModelEvent(this));
        }

        /**
         * Sets the table data
         * @param item the table data
         */
        public void setData(List<OrderEntryItem> item)
        {
            data = item;
            fireTableChanged(new TableModelEvent(this));
        }

        /**
         * Returns the table data
         * @return the table data
         */
        public List <OrderEntryItem> getData()
        {
            return data;
        }
    }

    static void notifyUser(OrderEntryItem item, String message) {
        if (Main.isMyself(item.from, item.media, item.to)) {
            return;
        }
        ContactWrapper wrapper = Main.findContact(item.from, item.media, item.to);
        if (wrapper != null) {
            if (wrapper.getConnection() instanceof MessageSupport) {
                ((MessageSupport) wrapper.getConnection()).sendMessage(new MessageImpl(wrapper, true, false, message));
            } else {
                Main.complain("Connection does not appear to be a message connection.  Notify failed.");
            }
        } else {
            Main.complain("Failed to find the user in your list.\n  Please login into " + item.media + " with your " + item.to + " username.\n  Notify failed.");
        }
    }

      /////////////////////////////////////////////////////////////////////
     // *******************  ConnectionEventListener   **************  //
    ///////////////////////////////////////////////////////////////////

    public boolean messageReceived(MessageSupport connection, Message message) {
        try {
            String line = GeneralUtils.stripHTML(message.getText());
            if (line.startsWith(OrderEntryBase.ORDER_TO)) {
                final OrderEntryItem item = new OrderEntryItem((message.isOutgoing()?connection.getUser():message.getContact()),
                                                (!message.isOutgoing()?connection.getUser():message.getContact()),
                                                connection,
                                                line);
                if (frame==null || ClientProperties.INSTANCE.isOrderCausesShowManageScreen())  // bring up the screen
                    showFrame(); // this way we don't open it for no good reason
                ((OrderTableModel)table.getModel()).addData(item);
                // todo return false;
                return true;
            }
        } catch (Exception e) {
            System.out.println(""+message.getText());
            e.printStackTrace();
        }
        return true;
    }

    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        return false;
    }

    public void typingNotificationReceived(MessageSupport connection, Nameable contact) { }
    public void connectionInitiated(Connection connection) { }
    public void connectionLost(Connection connection) { }
    public void connectionFailed(Connection connection, String message) { }
    public void connectionEstablished(Connection connection) { }
    public void statusChanged(Connection connection, Contact contact, Status oldStatus) { }
    public void statusChanged(Connection connection) { }
    public boolean contactRequestReceived(final String user, final MessageSupport connection) {  return true; }

    /**
     * A previously requested icon has arrived.
     * Icon will be a part of the contact.
     *
     * @param connection connection
     * @param contact    contact
     */
    public void pictureReceived(IconSupport connection, Contact contact) { }
    /**
     * Gets called when an assynchronous error occurs.
     *
     * @param message   to display
     * @param exception exception for tracing
     */
    public void errorOccured(String message, Exception exception) {
        //don't care
    }

    /**
     * Other side requested a file transfer.
     @param connection connection
      * @param contact
     * @param filename
     * @param description
     * @param connectionInfo
     */
    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
        // don't care
    }
}
