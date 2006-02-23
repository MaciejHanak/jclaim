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

package com.itbs.newgrep;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * AND
 *
 * @author Alex Rass
 * @since Sep 30, 2005
 */
public class DoubleString implements Search.What {
    String what1, what2;
    public DoubleString(String what1, String what2) {
        this.what1 = what1;
        this.what2 = what2;
    }

    public String getWhat1() {
        return what1;
    }
    public String getWhat2() {
        return what2;
    }

    public String toString() {
        return what1 + " and " + what2;
    }

    public void find(File file, Search.What what, Search.Result result, Options options) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        DoubleString searchString = (DoubleString) what;
        int read;
        int position=0;
        int filePosition=0;
        int line=0;
        boolean foundOne = false;
        boolean foundTwo = false;
        Search.Where where = new Search.Where(file.getAbsolutePath());
        while ((read = raf.read()) != -1) {
            if (read == 10) {
                line ++;
                foundOne = false;
                foundTwo = false;
            }

            while (position < searchString.getWhat1().length() && read == searchString.getWhat1().charAt(position) && (read = raf.read()) != -1) {
                if (read == 10) {
                    line ++;
                    foundOne = false;
                    foundTwo = false;
                    break;
                }
                position++;
                if (position == searchString.getWhat1().length()) {
                    foundOne = true;
                    if (foundTwo) {
                        where.setFilePosition(filePosition);
                        where.setLineNumber(line);
                        result.append(where);
                    }
                }
            }
            position = 0;
            while (position < searchString.getWhat2().length() && read == searchString.getWhat2().charAt(position) && (read = raf.read()) != -1) {
                if (read == 10) {
                    line ++;
                    foundOne = false;
                    foundTwo = false;
                    break;
                }
                position++;
                if (position == searchString.getWhat2().length()) {
                    foundTwo = true;
                    if (foundOne) {
                        where.setFilePosition(filePosition);
                        where.setLineNumber(line);
                        result.append(where);
                    }
                }
            }
            position = 0;
            filePosition++;
            raf.seek(filePosition);
        }
    }

}
