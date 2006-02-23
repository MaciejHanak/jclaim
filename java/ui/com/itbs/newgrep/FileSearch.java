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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File Search only.
 * Ignores contents.  Just looks for a file.
 * @author Alex Rass
 * @since Nov 20, 2005
 */
public class FileSearch implements Search.What, Search {
    String what;
    public FileSearch(String what) {
        this.what = what;
    }

    public String getWhat() {
        return what;
    }

    public String toString() {
        return what;
    }


    /**
     * Find using Pattern Matching
     * @param file to search
     * @param what to find
     * @param result to share
     * @throws java.io.IOException things to blow up
     */
    public void find(File file, What what, Result result, Options options) throws IOException {
        BufferedReader raf = new BufferedReader(new FileReader(file));
        FileSearch searchString = (FileSearch) what;
        String line;
        int lineNumber =0;
        Pattern p = Pattern.compile(SearchTools.getPatternOptions(searchString.getWhat(), options), SearchTools.getRegexOptions(options));
        Matcher m;
        Where where;
        m = p.matcher(file.getName());
        boolean found = m.find();
        if (!options.reverseLogic) { // it's faster to do it here than inside
            if (found) {
                where = new Where(file.getAbsolutePath());
                where.setLineNumber(lineNumber);
                result.append(where);
            }
        } else { // reverse logic
            if (!found) {
                where = new Where(file.getAbsolutePath());
                result.append(where);
            }
        } // else reverse logic
    }
}
