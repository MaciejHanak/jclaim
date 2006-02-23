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

package com.itbs.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides basic support for file transfer.
 *
 * @author Alex Rass
 * @since Jul 15, 2005
 */
public abstract class AbstractFileTransferHandler extends TransferHandler  {
    protected AbstractFileTransferHandler() {
    }

    public int getSourceActions(JComponent c) {
//        if (c instanceof JTextComponent) {
            return COPY_OR_MOVE;
//        }
//        return super.getSourceActions(c);
    }

    protected abstract void handle(JComponent c, List<File> fileList);

    public boolean importData(final JComponent c, Transferable t) {
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        try {
            if (hasFileFlavor(t.getTransferDataFlavors())) {
                final List<File> fileList = new ArrayList<File>();
                java.util.List files = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
                for (Object file1 : files) {
                    File file = (File) file1;
                    fileList.add(file);
                }
                if (fileList.size() == 0)
                    return false;
                // Make sure Explorer doesn't freeze
                new Thread() {
                    public void run() {
                        handle(c, fileList);
                    }
                }.start();
                return true;
            } else if (hasStringFlavor(t.getTransferDataFlavors())) {
                if (c instanceof JTextComponent) {
                    JTextComponent tc = (JTextComponent) c;
                    String str = (String)t.getTransferData(DataFlavor.stringFlavor);
                    tc.replaceSelection(str);
                    return true;
                }
            }
            return false;
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("importData: unsupported data flavor " + ufe.getMessage());
        } catch (IOException ieo) {
            System.out.println("importData: I/O exception " + ieo.getMessage());
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasFileFlavor(flavors))   { return true; }
        return hasStringFlavor(flavors);
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof JTextComponent) {
            JTextComponent comp = (JTextComponent) c;
            return new StringSelection(comp.getSelectedText());
        }
        return super.createTransferable(c);
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
        super.exportDone(source, data, action);
        if (action == MOVE) {
            if (source instanceof JTextComponent) {
                JTextComponent comp =  (JTextComponent) source;
                Document doc = comp.getDocument();
                if (doc != null) {
                    Caret caret = comp.getCaret();
                    int p0 = Math.min(caret.getDot(), caret.getMark());
                    int p1 = Math.max(caret.getDot(), caret.getMark());
                        if (p0 != p1) {
                            try {
                                doc.remove(p0, p1 - p0);
                            } catch (BadLocationException e) {
                                e.printStackTrace(); // should not happen
                            }
                        }
                } // if doc != null
            } // if text component
        } // if move
    }

    private static boolean hasFileFlavor(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStringFlavor(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

}
