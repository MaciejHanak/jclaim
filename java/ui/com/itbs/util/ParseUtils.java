package com.itbs.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * <p>Title: Provides some parsing methods</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: ITBS, LLC.</p>
 * @author Alex Rass
 * @version 1.0
 */

public final class ParseUtils {
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
                source = replace(source, SPECIAL_CHAR_START + key + SPECIAL_CHAR_END, value);
            } else { // abandoned start char
                // todo blow up, or something.  for now, just loose the junk
                source = source.substring(0, keyLocation)+source.substring(keyLocation+1);
                System.out.println("ParseUtils.replace: Problem in a file.  orphaned start tag");
            }
        }
        return source;
    }

    /**
     * Recursively search within input for any segment match searchFor exactly,
     * replaceWith will be replaced.
     * Replaced string will not be searched again.
     * So searchFor could be a substring of replaceWith, without running into overflow error.
     *
     * @param input original String to be replaced
     * @param searchFor String to be replaced in the original String
     * @param replaceWith String to be replaced with in the target String
     * @return the resulting string
     */
    public static String replace(final String input, final String searchFor, final String replaceWith)
    {
        if (input == null)
        {
            return "";
        }
        String current = input;
        final int pos = current.indexOf(searchFor);
        if (pos != -1)
        {
            current = current.substring(0, pos) + replaceWith
                + replace(current.substring(pos + searchFor.length()),
                          searchFor, replaceWith);
        }
        return current;
    }

}