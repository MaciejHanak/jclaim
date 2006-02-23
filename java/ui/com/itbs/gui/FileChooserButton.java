package com.itbs.gui;

/**
 * @author Alex Rass on Oct 8, 2004
 */

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


/**
 * Custom button for selecting files.
 **/
public class FileChooserButton extends BetterButton
{
    static String lastLocation;

    /** DateChooser instance to use to change the date. */
    private JFileChooser chooser = new JFileChooser(lastLocation);

    /** String used in empty lists **/
    public static final String EMPTY = "Choose...";

    /** File path property. */
    private File file;

    /** Parent component. */
    final Component parent;

    private static FileFilter filter = new FileFilter() {
        public boolean accept(File f) {
            return f.exists();
        }

        public String getDescription() {
            return "Only Files";
        }
    };
    /**
     * Called when the button is clicked, in order to fire an
     * <code>ActionEvent</code>. Displays the dialog to change the
     * date instead of generating the event and updates the date
     * property.
     *
     * @param e <code>ActionEvent</code> to fire
     **/
    protected void fireActionPerformed(ActionEvent e)
    {
        int returnVal = chooser.showOpenDialog(parent);
        lastLocation = chooser.getCurrentDirectory().getAbsolutePath();
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           setFileName(chooser.getSelectedFile());
        }
        super.fireActionPerformed(e);
    }

    public void setFileFilter(FileFilter filter) {
        chooser.setFileFilter(filter);
    }
    /**
     * Constructs a new <code>DateButton</code> object with a given
     * date.
     *
     * @param name initial value
     **/
    public FileChooserButton(Component parent, final String name)
    {
        this(parent);
        if (name!=null)
            setText(name);
        this.file = new File(name);
    }

    /**
     * Constructs a new <code>DateButton</code> object with the system
     * date as the initial date.
     **/
    public FileChooserButton(Component parent)
    {
        super(EMPTY);
        this.parent = parent;
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    /**
     * Gets the value.
     *
     * @return the current value of the file property
     **/
    public File getFile()
    {
        return file;
    }

    /**
     * Sets the values
     *
     * @param fileName new value of the name property
     */
    public void setFileName(final File fileName)
    {
        final File old = file;
        file = fileName;
        if (fileName == null)
        {
            setText(EMPTY);
        }
        else
        {
            setText(fileName.getName());
            chooser.setCurrentDirectory(fileName);
        }
        firePropertyChange("name", old, fileName);
    }



    /** @see TableCellEditor **/
    public static TableCellEditor getDateButtonCellEditor()
    {
        return new FileButtonCellEditor();
    }

    /** @see TableCellEditor **/
    public static class FileButtonCellEditor
            extends AbstractCellEditor
            implements TableCellEditor
    {
        /** Button **/
        private FileChooserButton button;

        // Methods implementing CellEditor

        /** @see CellEditor **/
        public Object getCellEditorValue()
        {
            return button.getFile().getPath();
        }

        // Methods implementing TableCellEditor
        /** @see TableCellEditor **/
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column)
        {

            String startFile = table.getValueAt(row, column).toString();
            button = new FileChooserButton(table, startFile);
            return button;
        }
    }
}