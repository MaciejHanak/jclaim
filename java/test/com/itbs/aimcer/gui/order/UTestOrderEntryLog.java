package com.itbs.aimcer.gui.order;

import com.itbs.aimcer.gui.UTestFrameTest;

import java.util.Date;

/**
 * @author Created by  Administrator on Nov 2, 2004
 */
public class UTestOrderEntryLog extends UTestFrameTest {
    public static final String from = "from";
    public static final String to = "from";
    public static final String media = "media";
    public static final String side = "side";
    public static final String symb = "symbo";
    public static final String qty = "2000";
    public static final String mkt = "DAY";
    public static final String price = "12";
    public static final String comission = ".02";

    public void testOrderEntryItem() throws Exception {
        OrderEntryItem item;
        item = new OrderEntryItem(from, to, media, OrderEntryBase.ORDER_TO + " "
            + side + " " + qty + " " + symb + " " + mkt + " at " + price + " with " + comission);
        assertEquals(from, item.getFrom());
        assertEquals(to  , item.getTo());
        assertEquals(side, item.getBuy());
        assertEquals(symb, item.getSymbol());
        assertEquals(qty , item.getQty());
        assertEquals(mkt , item.getMarket());
        assertEquals(price, item.getPrice());
        assertEquals(comission, item.getCommission());
        item = new OrderEntryItem(from, to, media, OrderEntryBase.ORDER_TO + " "
            + side + " " + qty + " " + symb + " " + mkt + " at " + price);
        assertEquals(from, item.getFrom());
        assertEquals(to  , item.getTo());
        assertEquals(side, item.getBuy());
        assertEquals(symb, item.getSymbol());
        assertEquals(qty , item.getQty());
        assertEquals(mkt , item.getMarket());
        assertEquals(price, item.getPrice());
        assertNull(item.getCommission());
    }
    public void testDialog() throws Exception {
        // add
        OrderEntryLog.getInstance().showFrame();
        ((OrderEntryLog.OrderTableModel)OrderEntryLog.getInstance().table.getModel()).addData(new OrderEntryItem(
                new Date(), "bob", "dan", "msft", "80.90", "mkt", "1000", "This is good.", "buy", null, new Boolean(false), new Boolean(true))
        );
        ((OrderEntryLog.OrderTableModel)OrderEntryLog.getInstance().table.getModel()).addData(new OrderEntryItem("Bob", "Dan", "AOL", "Order to buy: 50 isdn lmt at 10"));
        //                                                                                                                            Order to sell:1 msft LMT at 10
        ((OrderEntryLog.OrderTableModel)OrderEntryLog.getInstance().table.getModel()).addData(new OrderEntryItem("boB", "daN", "AOL", "Order to sell: 50 isdn lmt at 10"));
        window.setVisible(true);
        waitForMe(100);
    }
}
