package com.itbs.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to.
 *
 * Code is taken from Sun's public sample.
 * Then modified to support extra features.
 */
public class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;
    private final Component panel;
    JLabel label;

    public ButtonTabComponent(final BetterTabbedPane pane, final Component panel) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null || panel == null) {
            throw new NullPointerException("TabbedPane is null or component is");
        }
        this.pane = pane;
        this.panel = panel;
        setOpaque(false);

        label = new JLabel();
        label.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                dispatchToParent(e);
            }

            public void mouseMoved(MouseEvent e) {
                dispatchToParent(e);
            }
        });
        label.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                dispatchToParent(e);
//                pane.setSelectedComponent(panel);
            }

            public void mousePressed(MouseEvent e) {
                dispatchToParent(e);
            }

            public void mouseReleased(MouseEvent e) {
                dispatchToParent(e);
            }

            public void mouseEntered(MouseEvent e) {
                dispatchToParent(e);
            }

            public void mouseExited(MouseEvent e) {
                dispatchToParent(e);
            }
        });
        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    } // Constructor

    Point point; int comp_x, comp_y; // for component with respect to this machine
    public void dispatchToParent(MouseEvent e) {
        point=((JComponent)e.getSource()).getLocation();
        //System.out.println("Dispatching: " + e.getX() + " " + e.getY());
        //System.out.println("Component position: " + ((JComponent)(e.getSource())).getLocation());
        comp_x=(int)point.getX(); comp_y=(int)point.getY();
        comp_x+=this.getX(); comp_y+=this.getY();
        e.translatePoint(comp_x, comp_y);
        dispatchEvent(e);
        dispatchToParentsParent(e);
    }
    public void dispatchToParentsParent(MouseEvent e) {
        point=((JComponent)e.getSource()).getParent().getLocation();
        //System.out.println("Dispatching: " + e.getX() + " " + e.getY());
        //System.out.println("Component position: " + ((JComponent)(e.getSource())).getLocation());
        comp_x=(int)point.getX(); comp_y=(int)point.getY();
        comp_x+=this.getX(); comp_y+=this.getY();
        e.translatePoint(comp_x, comp_y);
        getParent().getParent().dispatchEvent(e);
    }

    public JLabel getLabel() {
        return label;
    }

    /**
     * Little close button in the corner.
     */
    private class TabButton extends JButton implements ActionListener {
        Insets insets;
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Closes Tab - Ctrl-F4");

            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // precalc locations to avoid math later.
            insets = getBorder().getBorderInsets(this);
            insets.right = size - insets.right;
            insets.bottom = size - insets.bottom;
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            pane.remove(panel);
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        /** paint the cross */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            if (getModel().isRollover()) {
                g2.setColor(Color.RED);
                g2.fillRect(getModel().isRollover()?0:insets.left, getModel().isRollover()?0:insets.top, insets.right, insets.bottom);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.BLACK);
            }

            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    /**
     * Paint a border on mouse over.
     */
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
//                button.setBackground(Color.RED);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
//                button.setBackground(null);
            }
        }
    };
}