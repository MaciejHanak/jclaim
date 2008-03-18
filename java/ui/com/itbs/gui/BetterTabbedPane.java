package com.itbs.gui;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * @author Alex Rass
 * @since Mar 10, 2008 12:58:45 PM
 */
public class BetterTabbedPane extends JTabbedPane {
    private static final Logger log = Logger.getLogger(BetterTabbedPane.class.getName());
    
    public static boolean oldVM;

    /**
     * Locks
     */
    private final ReentrantLock lock = new ReentrantLock();
    private static final String TAB_UP = "Tab Up";
    private static final String TAB_DOWN = "Tab Down";
    private static final String TAB_CLOSE = "Tab Close";

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    /**
     * Creates an empty <code>TabbedPane</code> with a default
     * tab placement of <code>JTabbedPane.TOP</code>.
     *
     * @see #addTab
     */
    public BetterTabbedPane() {
        super();
        init();
    }

    /**
     * Creates an empty <code>TabbedPane</code> with the specified tab placement
     * of either: <code>JTabbedPane.TOP</code>, <code>JTabbedPane.BOTTOM</code>,
     * <code>JTabbedPane.LEFT</code>, or <code>JTabbedPane.RIGHT</code>.
     *
     * @param tabPlacement the placement for the tabs relative to the content
     * @see #addTab
     */
    public BetterTabbedPane(int tabPlacement) {
        super(tabPlacement);
        init();
    }

    /**
     * Creates an empty <code>TabbedPane</code> with the specified tab placement
     * and tab layout policy.  Tab placement may be either:
     * <code>JTabbedPane.TOP</code>, <code>JTabbedPane.BOTTOM</code>,
     * <code>JTabbedPane.LEFT</code>, or <code>JTabbedPane.RIGHT</code>.
     * Tab layout policy may be either: <code>JTabbedPane.WRAP_TAB_LAYOUT</code>
     * or <code>JTabbedPane.SCROLL_TAB_LAYOUT</code>.
     *
     * @param tabPlacement    the placement for the tabs relative to the content
     * @param tabLayoutPolicy the policy for laying out tabs when all tabs will not fit on one run
     * @throws IllegalArgumentException if tab placement or tab layout policy are not
     *                                  one of the above supported values
     * @see #addTab
     * @since 1.4
     */
    public BetterTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        init();
    }
    MouseAdapter dndMouseAdapter;
    private void init() {
        setupKeys(this);
        dndMouseAdapter = new TabMoveHandler(); // 1
//         dndMouseAdapter = new MouseHandler();     // 2

        addMouseListener(dndMouseAdapter);
        addMouseMotionListener(dndMouseAdapter);

    }

    AbstractAction ACT_TAB_UP = new AbstractAction(TAB_UP) {
        public void actionPerformed(ActionEvent e) {
            log.info("" + this.getValue(NAME));
            JTabbedPane tp = BetterTabbedPane.this;
            int index = tp.getSelectedIndex();
            if (index > 0) {
                tp.setSelectedIndex(--index);
            }
        }
    };
    AbstractAction ACT_TAB_DOWN = new AbstractAction(TAB_DOWN) {
        public void actionPerformed(ActionEvent e) {
            log.info("" + this.getValue(NAME));
            JTabbedPane tp = BetterTabbedPane.this;
            int index = tp.getSelectedIndex();
            if (index < tp.getTabCount() - 1) {
                tp.setSelectedIndex(++index);
            }
        }
    };

    AbstractAction ACT_TAB_CLOSE = new AbstractAction(TAB_CLOSE) {
        public void actionPerformed(ActionEvent e) {
            log.info("" + this.getValue(NAME));
            JTabbedPane tp = BetterTabbedPane.this;
            int index = tp.getSelectedIndex();
            tp.remove(index);
        }
    };

    /**
     * Use to provide key mapping to other components
     *
     * @param comp Component to connect keys to.
     */
    public void setupKeys(RootPaneContainer comp) {
        GUIUtils.addAction(comp, KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK, ACT_TAB_UP);
        GUIUtils.addAction(comp, KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK, ACT_TAB_DOWN);
        GUIUtils.addAction(comp, KeyEvent.VK_F4, KeyEvent.CTRL_DOWN_MASK, ACT_TAB_CLOSE);
//        GUIUtils.addAction(comp, KeyEvent.VK_ESCAPE, 0, ACT_LOG);
        // if you want to add more mappings to same buttons, just use text constants.
    }

    /**
     * Use to provide key mapping to other components
     *
     * @param comp Component to connect keys to.
     */
    public void setupKeys(JComponent comp) {
//        comp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK));
//        comp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK));
        GUIUtils.addAction(comp, KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK, ACT_TAB_UP);
        GUIUtils.addAction(comp, KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK, ACT_TAB_DOWN);
        GUIUtils.addAction(comp, KeyEvent.VK_F4, KeyEvent.CTRL_DOWN_MASK, ACT_TAB_CLOSE);
        // if you want to add more mappings to same buttons, just use text constants.
    }


    /**
     * Returns the index of the tab for the specified component.
     * Returns -1 if there is no tab for this component.
     *
     * @param component the component for the tab
     * @return the first tab which matches this component, or -1
     *         if there is no tab for this component
     */
    public int indexOfComponent(Component component) {
        lock();
        try {
            return super.indexOfComponent(component);
        } finally {
            unlock();
        }
    }


    /**
     * Returns the tab component at <code>index</code>.
     *
     * Here b/c Mac is 1.6-challenged.
     *
     * @param index the index of the item being queried
     * @return the tab component at <code>index</code>
     * @throws IndexOutOfBoundsException if index is out of range
     *                                   (index < 0 || index >= tab count)
     * @see #setTabComponentAt
     * @since 1.6
     */
    public Component getTabComponentAtReflect(int index) {
//        return super.getTabComponentAt(index);
        if (!oldVM) {
            try {
                Class types[] = new Class[]{int.class};
                Method method = getClass().getMethod("getTabComponentAt", types);
                return (Component) method.invoke(this, index);
            } catch (Exception e) { // jdk 1.5 compatibility
                // this is 1.5, lets do smth else
                oldVM = true;
            }
        }
        return null;
    }


    /**
     * Here b/c Mac is 1.6-challenged.
     *
     * Sets the component that is responsible for rendering the
     * title for the specified tab.  A null value means
     * <code>JTabbedPane</code> will render the title and/or icon for
     * the specified tab.  A non-null value means the component will
     * render the title and <code>JTabbedPane</code> will not render
     * the title and/or icon.
     * <p/>
     * Note: The component must not be one that the developer has
     * already added to the tabbed pane.
     *
     * @param index     the tab index where the component should be set
     * @param component the component to render the title for the
     *                  specified tab
     * @throws IndexOutOfBoundsException if index is out of range
     *                                   (index < 0 || index >= tab count)
     * @throws IllegalArgumentException  if component has already been
     *                                   added to this <code>JTabbedPane</code>
     * @beaninfo preferred: true
     * attribute: visualUpdate true
     * description: The tab component at the specified tab index.
     * @see #getTabComponentAt
     * @since 1.6
     */
    public void setTabComponentAtReflect(int index, Component component) {
//        super.setTabComponentAt(index, component);
        if (!oldVM) {
            try {
                Class types[] = new Class[]{int.class, Component.class};
                Method method = getClass().getMethod("setTabComponentAt", types);
                method.invoke(this, index, component);
            } catch (Exception e) { // jdk 1.5 compatibility
                // this is 1.5, lets do smth else
                oldVM = true;
            }
        }
    }

    /**
     * Came from here:
     * http://forum.java.sun.com/thread.jspa?threadID=263180&messageID=3253894
     */
    public class TabMoveHandler extends MouseAdapter implements MouseMotionListener {
        int startIndex = -1;
        private int currentIndex = -1;

        /* (non-Javadoc)
        * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
        */
        public void mousePressed(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                startIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            }
            currentIndex = -1;
        }

        /* (non-Javadoc)
        * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
        */
        public void mouseReleased(MouseEvent e) {
            BetterTabbedPane tabbedPane = (BetterTabbedPane) e.getSource();
            if (!e.isPopupTrigger()) {
                int endIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());

                if (startIndex != -1 && endIndex != -1 && startIndex != endIndex) {
                    moveTab(tabbedPane, startIndex, endIndex);
                    tabbedPane.setSelectedIndex(endIndex);

                    // ONLY OMS SPECIFIC LINE
//                    OMSBasePanel.getInstance().saveUISettings();
                }
            }
            startIndex = -1;
            clearRectangle(tabbedPane);
            currentIndex = -1;
        }

        /**
         * @param pane tabbedPane
         * @param src source tab index
         * @param dst destination tab index
         */
        private void moveTab(BetterTabbedPane pane, int src, int dst) {
            // Get all the properties
            Component comp = pane.getComponentAt(src);
            Component tabConrol = pane.getTabComponentAtReflect(src);
            String label = pane.getTitleAt(src);
            Icon icon = pane.getIconAt(src);
            Icon iconDis = pane.getDisabledIconAt(src);
            String tooltip = pane.getToolTipTextAt(src);
            boolean enabled = pane.isEnabledAt(src);
            int keycode = pane.getMnemonicAt(src);
            int mnemonicLoc = pane.getDisplayedMnemonicIndexAt(src);
            Color fg = pane.getForegroundAt(src);
            Color bg = pane.getBackgroundAt(src);

            // Remove the tab
            pane.remove(src);

            // Add a new tab
            pane.insertTab(label, icon, comp, tooltip, dst);

            // Restore all properties
            pane.setDisabledIconAt(dst, iconDis);
            pane.setEnabledAt(dst, enabled);
            pane.setMnemonicAt(dst, keycode);
            pane.setDisplayedMnemonicIndexAt(dst, mnemonicLoc);
            pane.setForegroundAt(dst, fg);
            pane.setBackgroundAt(dst, bg);
            pane.setTabComponentAtReflect(dst, tabConrol);
        }


        /* (non-Javadoc)
        * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
        */
        public void mouseDragged(MouseEvent e) {
            if (startIndex != -1) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                int index = tabbedPane.indexAtLocation(e.getX(), e.getY());

                if (index != -1 && index != currentIndex) { // moved over another tab
                    clearRectangle(tabbedPane);
                    currentIndex = index;
                }

                if (currentIndex != -1 && currentIndex != startIndex) {
                    drawRectangle(tabbedPane);
                }
            }
        }

        private void clearRectangle(JTabbedPane tabbedPane) {
            if (currentIndex == -1) {
                return;
            }
            TabbedPaneUI ui = tabbedPane.getUI();
            Rectangle rect = ui.getTabBounds(tabbedPane, currentIndex);
            tabbedPane.repaint(rect);
        }

        private void drawRectangle(JTabbedPane tabbedPane) {
            TabbedPaneUI ui = tabbedPane.getUI();
            Rectangle rect = ui.getTabBounds(tabbedPane, currentIndex);
            Graphics graphics = tabbedPane.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }

        /* (non-Javadoc)
        * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
        */
        public void mouseMoved(MouseEvent e) {
        }

        /* (non-Javadoc)
        * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
        */
        public void mouseExited(MouseEvent e) {
            clearRectangle((JTabbedPane) e.getSource());
            currentIndex = -1;
        }
    }

    // ******************************************************* ALTERNATIVE DND

/*
    public BetterTabbedPane()
    {
        this(TOP);
    }

    public XTabbedPane(int tabPlacement)
    {
        this(tabPlacement, WRAP_TAB_LAYOUT);
    }

    public XTabbedPane(int tabPlacement, int tabLayoutPolicy)
    {
        super(tabPlacement, tabLayoutPolicy);

    }
*/
    private Cursor defaultCursor, handCursor;

    private void dragTab(int dragIndex, int tabIndex)
    {
        String title = getTitleAt(dragIndex);
        Icon icon = getIconAt(dragIndex);
        Component component = getComponentAt(dragIndex);
        String toolTipText = getToolTipTextAt(dragIndex);

        Color background = getBackgroundAt(dragIndex);
        Color foreground = getForegroundAt(dragIndex);
        Icon disabledIcon = getDisabledIconAt(dragIndex);
        int mnemonic = getMnemonicAt(dragIndex);
        int displayedMnemonicIndex = getDisplayedMnemonicIndexAt(dragIndex);
        boolean enabled = isEnabledAt(dragIndex);

        remove(dragIndex);
        insertTab(title, icon, component, toolTipText, tabIndex);

        setBackgroundAt(tabIndex, background);
        setForegroundAt(tabIndex, foreground);
        setDisabledIconAt(tabIndex, disabledIcon);
        setMnemonicAt(tabIndex, mnemonic);
        setDisplayedMnemonicIndexAt(tabIndex, displayedMnemonicIndex);
        setEnabledAt(tabIndex, enabled);
    }

    private Cursor getDefaultCursor()
    {
        if (defaultCursor == null)
        {
            defaultCursor = Cursor.getDefaultCursor();
        }

        return defaultCursor;
    }

    private Cursor getHandCursor()
    {
        if (handCursor == null)
        {
            handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }

        return handCursor;
    }

    private int getTabIndex(int x, int y)
    {
        return getUI().tabForCoordinate(this, x, y);
    }

    private void maybeSetDefaultCursor()
    {
        Cursor cursor = getDefaultCursor();

        if (getCursor() != cursor)
        {
            setCursor(cursor);
        }
    }

    private void maybeSetHandCursor()
    {
        Cursor cursor = getHandCursor();

        if (getCursor() != cursor)
        {
            setCursor(cursor);
        }
    }
    class MouseHandler extends MouseInputAdapter {

        public void mouseDragged(MouseEvent e) {
            if (dragIndex != -1) {
                if (getTabIndex(e.getX(), e.getY()) != -1) {
                    maybeSetHandCursor();
                } else {
                    maybeSetDefaultCursor();
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
                int tabIndex = getTabIndex(e.getX(), e.getY());
                if (tabIndex != -1) {
                    dragIndex = tabIndex;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
                if (dragIndex != -1) {
                    int tabIndex = getTabIndex(e.getX(), e.getY());
                    if (tabIndex != -1 && tabIndex != dragIndex) {
                        dragTab(dragIndex, tabIndex);
                        setSelectedIndex(tabIndex);
                    }
                    dragIndex = -1;
                    maybeSetDefaultCursor();
                }
            }
        }

        private int dragIndex = -1;
    } // Mouse class
} // class
