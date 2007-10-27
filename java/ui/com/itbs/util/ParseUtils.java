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

package com.itbs.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>Title: Provides some parsing methods</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: ITBS, LLC.</p>
 * @author Alex Rass
 * @version 1.0
 */

public final class ParseUtils {
    private static final Logger log = Logger.getLogger(ParseUtils.class.getName());
    public static final String SPECIAL_CHAR_START = "<%";
    public static final String SPECIAL_CHAR_END = "%>";

    /**
     * Parses out the file, populating the fields
     * @param fileName name of file to parse (with path)
     * @param fields replacement fields.
     * @return parsed file
     * @throws java.io.FileNotFoundException whenever file is not found
     * @throws java.io.IOException whenever file can not be read
     */
    public static String parseFile(String fileName, Map <?, String> fields) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String file="";
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf(SPECIAL_CHAR_START)>=0) {
                line = replace(line, fields);
            }
            file+=line;
        }
        return file;
    }

    /**
     * Replaces occurances of all things in the fields map with the values.
     * @param source line to fix
     * @param fields parameters to replace
     * @return processed line where <%contained%> strings are replaced
     */
    public static String replace (String source, Map <?, String> fields) {
        String key, value;
        int keyLocation;
        int secondLocation;
        while ((keyLocation = source.indexOf(SPECIAL_CHAR_START) ) >= 0) {
            secondLocation = source.indexOf(SPECIAL_CHAR_END, keyLocation);
            if (secondLocation>=0) {
                key = source.substring(keyLocation+SPECIAL_CHAR_START.length(), secondLocation);
                value = fields.get(key);
                value = value==null?"":value;
                source = GeneralUtils.replace(source, SPECIAL_CHAR_START + key + SPECIAL_CHAR_END, value);
            } else { // abandoned start char
                // todo blow up, or something.  for now, just loose the junk
                source = source.substring(0, keyLocation)+source.substring(keyLocation+1);
                log.info("ParseUtils.replace: Problem in a file.  orphaned start tag");
            }
        }
        return source;
    }
}