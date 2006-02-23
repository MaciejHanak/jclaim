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
