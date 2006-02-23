package com.itbs.util;

import com.itbs.gui.ErrorDialog;
import junit.framework.TestCase;

/**
 * @author Created by  Administrator on Apr 25, 2005
 */
public class UTestGeneralUtils extends TestCase {
    public void testDialog() throws Exception {
        ErrorDialog.displayError(null, "msg", "details");
    }
}
