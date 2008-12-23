package com.itbs.util;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helps with managing the Comma Sepavated Value files.
 *
 * @author Alex Rass
 * @since Mar 22, 2006
 */
public class CSVHelper {
    private static byte[] EMPTY = "\"No data found\"".getBytes();

    /**
     * Writes the result set to the output stream as CSV.
     *
     * @param os     destination
     * @param tokens list of objects to write.  toString is used to convert.  Nulls become blanks.
     * @throws IOException when stream things blow up.
     */
    public static void writeCSV(OutputStream os, List tokens) throws IOException {
        Object temp;
        for (Object token : tokens) {
            os.write('"');
            temp = token;
            if (temp != null) {
                os.write(temp.toString().replaceAll("\"", "\"\"").getBytes());
            }
            os.write('"');
            os.write(',');
        }
        os.write('\n');
        os.flush(); // all done writing here.
    }

    /**
     * Writes the result set to the output stream as CSV.
     * Including column names!
     *
     * @param os destination
     * @param rs result set to write
     * @return results were found
     * @throws SQLException when DB things blow up.
     * @throws IOException  when stream things blow up.
     */
    public static boolean writeCSV(OutputStream os, ResultSet rs) throws SQLException, IOException {
        return writeCSV(os, rs, 1, true);
    }

    /**
     * Writes the result set to the output stream as CSV.
     *
     * @param os destination
     * @param rs result set to write
     * @param startIndex 1 based index. column to start with
     * @param headers print headers
     * @return results were found
     * @throws SQLException when DB things blow up.
     * @throws IOException  when stream things blow up.
     */
    public static boolean writeCSV(OutputStream os, ResultSet rs, int startIndex, boolean headers) throws SQLException, IOException {
        String temp;

        int columns = rs.getMetaData().getColumnCount();
        if (headers) {
            for (int i = startIndex; i <= columns; i++) {
                os.write('"');
                temp = rs.getMetaData().getColumnName(i);
                if (temp != null) {
                    os.write(temp.replaceAll("\"", "\"\"").getBytes());
                }
                os.write('"');
                if (i<columns) {
                    os.write(',');
                }
            } //columns
            os.write('\n');
        }

        boolean ran = false;
        while (rs.next()) {
            ran = true;
            for (int i = startIndex; i <= columns; i++) {
                os.write('"');
                temp = rs.getString(i);
                if (temp != null) {
                    os.write(temp.replaceAll("\"", "\"\"").getBytes());
                }
                os.write('"');
                if (i<columns) {
                    os.write(',');
                }
            }
            os.write('\n');
        }
        if (!ran && headers) {
            os.write(EMPTY);
        }
        os.flush(); // all done writing here.
        return ran;
    }

    public static List<String> performDelimeterParsing(String line, String delimeters) throws Exception {
        String[] result = line.split("\\"+delimeters);
        return Arrays.asList(result);
/*
        StringTokenizer tzr = new StringTokenizer(line, delimeters, true);
        List<String> tokens = new ArrayList<String>();
        String result;
        while (tzr.hasMoreElements()) {
            result = tzr.nextToken();
            if (!delimeters.equals(result)) {
                tokens.add(result);// map data
            }
        }
        return tokens;
*/
    }

    public static List<String> performCSVParsing(String line) throws Exception {
        return performCSVParsing(line, ",\"", true);
    }
    
    public static List<String> performCSVParsing(String line, String delimeters, boolean returnTokens) throws Exception {
        StringTokenizer tzr = new StringTokenizer(line, delimeters, true);
        int tokenCount = 0;
        List<String> tokens = new ArrayList<String>();
        String token, realToken = "";
        boolean doubleQuotes = false;
        while (tzr.hasMoreElements()) {
            token = (String) tzr.nextElement(); // string, ',' or '"'
            if ("\"".equals(token)) {
                doubleQuotes = !doubleQuotes; // turn on doublequote mode
                if (doubleQuotes) { // if doublequite started
                    realToken = ""; // move on
                } else { // last
                    tokens.add(realToken);// map data
                }
            } else {
                if (doubleQuotes) { // just add unless it's the last one
                    realToken += token;
                } else {
                    if (",".equals(token)) {
                        tokenCount++;
                        if (tokenCount > tokens.size())
                            tokens.add(null);
                        if (returnTokens) tokens.add(",");
                        continue;
                    }
                    realToken = token;
                    // map data
                    tokens.add(token);
                }
            }
        }
        return tokens;
    }

}
