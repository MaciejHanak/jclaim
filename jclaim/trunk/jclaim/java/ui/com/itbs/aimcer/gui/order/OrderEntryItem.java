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

import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.Connection;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Maintains an order entry entity.
 *
 * @author Alex Rass
 * @since Feb 20, 2006
 */
public class OrderEntryItem {
    Date time;
    String from, to, media, symbol, price, market, qty, comment;
    /** Buy/Sell/Sold etc. */
    String buy;
    String commission;
    Boolean invalid, done;
    int remaining;

    public OrderEntryItem() {
    }

    public OrderEntryItem(Nameable from, Nameable to, Connection connection, String line) {
        this(from.getName(), to.getName(), connection.getServiceName(), line);
    }

    public OrderEntryItem(String from, String to, String medium, String line) {
        time = new Date();
        this.from = from;
        this.to = to;
        this.media = medium;
        comment = "";
        invalid = Boolean.FALSE;
        done = Boolean.FALSE;
        StringTokenizer tok = new StringTokenizer(line.substring(OrderEntryBase.ORDER_TO.length()), " :");
        buy = tok.nextToken();
        qty = tok.nextToken();
        setRemaining(qty);
        symbol = tok.nextToken();
        market = tok.nextToken();
        if ("at".equals(market)) { // short IOI form
            market = "";
        }
        if (buy.startsWith("T")) { // no price for traded
            price = "";
        } else {
            String temp = tok.nextToken(); // at or MKT
            if ("at".equals(temp)) { // should cover the market
                price = tok.nextToken();
            } else {
                price = temp;
            }
    //            type = tok.nextToken();
            if (tok.hasMoreTokens()) {
                tok.nextToken(); // with
                commission = tok.nextToken();
            }
        }
    }

//        Boolean checkValue(String token, String trueValue) {
//            return token.equalsIgnoreCase(trueValue)?Boolean.TRUE:Boolean.FALSE;
//        }

    /**
     * Used by unit tests.
     */
    public OrderEntryItem(Connection connection, Date time, String from, String to, String symbol, String price, String market, String qty, String comment, String buy, String commission, Boolean invalid, Boolean done) {
        this.media = connection.getServiceName();
        this.time = time;
        this.from = from;
        this.to = to;
        this.symbol = symbol;
        this.price = price;
        this.market = market;
        this.qty = qty;
        remaining = Integer.parseInt(qty);
        this.comment = comment;
        this.buy = buy;
        setCommission(commission);
        this.invalid = invalid;
        this.done = done;
        if (buy.startsWith("T")) { // no price for traded
            this.price = "";
        }
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        if (qty!=null) {
            qty = qty.trim();
            qty = qty.endsWith("k")?qty.substring(0, qty.length() -1)+"000":qty;
        }
        this.qty = qty;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(String remaining) {
        try {
            this.remaining = Integer.parseInt(remaining);
        } catch (NumberFormatException e) {
            this.remaining = 0;
        }
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBuy() {
        return buy;
    }

    public void setBuy(String buy) {
        this.buy = buy;
    }

    public Boolean getInvalid() {
        return invalid;
    }

    public void setInvalid(Boolean invalid) {
        this.invalid = invalid;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        if (commission == null || commission.trim().length()==0)
            this.commission = null;
        else
            this.commission = commission;
    }

    /**
     * @return a string representation of the object.
     */
    public String toString() {
        return OrderEntryBase.ORDER_TO + buy + ":" + qty + " " + symbol + (price.length() ==0?"":(" at " + price)) + //" as of " + time +
        (commission == null?"":(" with " + commission));
//                ORDER_TO + actionType + ":"
//                        + (qty.getText().endsWith("k")?qty.getText().substring(0, qty.getText().length() -1)+"000":qty.getText())
//                        + " " + symbol.getText() + " " + market.getSelectedItem() + " at " + (price.getText().trim().length()==0?"MKT":price.getText()) +  ((commission==null || commission.getText().trim().length() == 0)?"":" with " + commission.getText());
    }
}
