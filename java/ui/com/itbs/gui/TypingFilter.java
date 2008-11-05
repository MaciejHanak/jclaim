package com.itbs.gui;

import com.itbs.util.DelayedThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Lets us add a customizeable key filter to any component.
 *
 * command will keep collecting keystrokes within 5 sec fromn each other.
 * Then wait 5 seconds and send a blank command back to component for a reset.
 *
 * @author Alex Rass
 * @since Oct 9, 2008
 */
public class TypingFilter extends KeyAdapter {
    ActionListener actionListener;
    String command ="";
    long last;
    final static long DELAY = 5*1000;// 5 sec
    DelayedThread thread;

    public TypingFilter(ActionListener listener) {
        this.actionListener = listener;
    }

    void sendCommand() {
        last = System.currentTimeMillis();
        // run the Delayed ActionThread
        if (thread==null) {
            thread = new DelayedThread(getClass().getName(),DELAY, new DelayedThread.StillAliveMonitor() {
                public boolean isAlive() {
                    return true;
                }
            }, new Runnable() {
                // this should happen every time we hit mark too.
                public void run() {
                    actionListener.actionPerformed(new ActionEvent(this, 0, command));
                }
            }, true, new Runnable() {
                public void run() {
                    command="";
                    actionListener.actionPerformed(new ActionEvent(this, 0, command));
                    thread=null;
                }
            });
            thread.start();
        }
        if (thread!=null) {
            thread.mark();
        }
    }

    public void keyTyped(KeyEvent e) {
        if ((last+DELAY)<System.currentTimeMillis()) {
            command =""; // clear it;
        }
        if (e.isControlDown() || e.isAltDown()) {
            return; // nothing we care for.
        }
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            command = command.substring(0, Math.min(command.length()-1, 0));
        } else if (Character.isLetterOrDigit(e.getKeyChar())) {
            command +=e.getKeyChar();
            sendCommand();
        }
    }
}
