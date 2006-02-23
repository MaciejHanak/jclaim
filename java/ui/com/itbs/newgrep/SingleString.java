package com.itbs.newgrep;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alex Rass
 * @since Sep 30, 2005
 */
public class SingleString implements Search.What, Search {
    String what;
    public SingleString(String what) {
        this.what = what;
    }

    public String getWhat() {
        return what;
    }

    public String toString() {
        return what;
    }

    public void findLarge(File file, Search.What what, Search.Result result) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        SingleString searchString = (SingleString) what;
        int read;
        int position=0;
        int filePosition=0;
        int line=0, hitLine=-1;
        Search.Where where = new Search.Where(file.getAbsolutePath());
        StringBuffer lastLine = new StringBuffer(1024);
        while ((read = raf.read()) != -1) {
            if (read == 10) {
                if (hitLine == line) {
                    where.setFilePosition(filePosition);
                    where.setLineNumber(line);
                    where.setLine(lastLine.toString());
                    result.append(where);
                }
                line ++;
                lastLine.setLength(0);
            } else {
                lastLine.append((char)read);
            }
            while (position < searchString.getWhat().length() && read == searchString.getWhat().charAt(position) && (read = raf.read()) != -1) {
                if (read == 10) {
                    if (hitLine == line) {
                        where.setFilePosition(filePosition);
                        where.setLineNumber(line);
                        where.setLine(lastLine.toString());
                        result.append(where);
                    }
                    line ++;
                    lastLine.setLength(0);
                    break;
                }
                position++;
                lastLine.append((char)read);
                if (position == searchString.getWhat().length()) {
                    hitLine = line;
                }
            }
            position = 0;
            filePosition++;
            raf.seek(filePosition);
        }
    }


    /**
     * Find using RandomAccessFile
     * @param file to search
     * @param what to find
     * @param result to share
     * @throws IOException things to blow up
     */
    public void findR(File file, Search.What what, Search.Result result, Options options) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        SingleString searchString = (SingleString) what;
        byte[] whatLC = (options.caseSensitive) ? searchString.getWhat().getBytes() : searchString.getWhat().toLowerCase().getBytes();
        byte[] whatUC = (options.caseSensitive) ? searchString.getWhat().getBytes() : searchString.getWhat().toUpperCase().getBytes();
        int read;
        int position=0;
        int filePosition=0;
        int line=0;
        boolean seek;
        Search.Where where = new Search.Where(file.getAbsolutePath());
        while ((read = raf.read()) != -1) {
            if (read == 10) {
                line ++;
            }
            seek = false;
            while (position < searchString.getWhat().length()
                    && (read == whatLC[position] || read == whatUC[position])
                    && (read = raf.read()) != -1) {
                if (read == 10) {
                    line ++;
                    filePosition += position;
                    seek = false;
                    break;
                }
                seek = true;
                position++;
                if (position == searchString.getWhat().length()) {
                    where.setFilePosition(filePosition);
                    where.setLineNumber(line);
                    result.append(where);
                }
            }
            filePosition++;
            if (seek)
                raf.seek(filePosition);
            position = 0;
        }
    }

    /**
     * Find using BufferedInputStream and FileInputStream
     * @param file to search
     * @param what to find
     * @param result to share
     * @throws IOException things to blow up
     */
    public void findB(File file, Search.What what, Search.Result result, Options options) throws IOException {
        BufferedInputStream raf = new BufferedInputStream(new FileInputStream(file));
        SingleString searchString = (SingleString) what;
        byte[] whatLC = (options.caseSensitive) ? searchString.getWhat().getBytes() : searchString.getWhat().toLowerCase().getBytes();
        byte[] whatUC = (options.caseSensitive) ? searchString.getWhat().getBytes() : searchString.getWhat().toUpperCase().getBytes();
        int read;
        int position=0;
        int filePosition=0;
        int line=0;
        boolean seek;
        Search.Where where = new Search.Where(file.getAbsolutePath());
        while ((read = raf.read()) != -1) {
            if (read == 10) {
                line ++;
            }
            seek=false;
            raf.mark(whatUC.length);
            while (position < whatUC.length
                    && (read == whatLC[position] || read == whatUC[position])
                    && (read = raf.read()) != -1) {
                if (read == 10) {
                    line ++;
                    filePosition += position;
                    seek = false;
//                    raf.mark(whatUC.length); // optimization?
                    break;
                }
                seek = true;
                position++;
                if (position == searchString.getWhat().length()) {
                    where.setFilePosition(filePosition);
                    where.setLineNumber(line);
                    result.append(where);
                }
            }
            filePosition++;
            if (seek)
                raf.reset(); //filePosition
            position = 0;
        }
    }

    /**
     * Find using BufferedInputStream and FileInputStream
     * @param file to search
     * @param what to find
     * @param result to share
     * @throws IOException things to blow up
     */
    public void findTest(File file, Search.What what, Search.Result result, Options options) throws IOException {
        BufferedReader raf = new BufferedReader(new FileReader(file));
        SingleString searchString = (SingleString) what;
//        byte[] whatLC = (caseSensitive) ? searchString.getWhat().getBytes() : searchString.getWhat().toLowerCase().getBytes();
//        byte[] whatUC = (caseSensitive) ? searchString.getWhat().getBytes() : searchString.getWhat().toUpperCase().getBytes();
        String read;
//        int position=0;
//        int filePosition=0;
        int line=0;
//        boolean seek;
        Pattern p = Pattern.compile(searchString.getWhat());

        Search.Where where = new Search.Where(file.getAbsolutePath());
        while ((read = raf.readLine()) != null) {
/*
            if (read.contains(searchString.getWhat())) {
                where.setLineNumber(line);
                result.append(where);

            }
*/
/*
            for (int i=0; i<read.length(); i++) {
                while (position < whatUC.length
                        && (i+ position < read.length())
                        && (read.charAt(i + position) == whatLC[position] || read.charAt(i+position) == whatUC[position])) {
                    position++;
                    if (position == searchString.getWhat().length()) {
                        where.setLineNumber(line);
                        result.append(where);
                    }
                }
                if (position == searchString.getWhat().length()) break;
                position = 0;
            }
*/

            Matcher m = p.matcher(read);
            while(m.find()) {
                where.setLineNumber(line);
                where.setLine(m.group() + ": " + m.start());
                result.append(where);
            }

            line ++;
        }
    }
    /**
     * Find using Pattern Matching
     * @param file to search
     * @param what to find
     * @param result to share
     * @throws IOException things to blow up
     */
    public void find(File file, Search.What what, Search.Result result, Options options) throws IOException {
        BufferedReader raf = new BufferedReader(new FileReader(file));
        SingleString searchString = (SingleString) what;
        String line;
        int lineNumber =0;
        Pattern p = Pattern.compile(SearchTools.getPatternOptions(searchString.getWhat(), options), SearchTools.getRegexOptions(options));
        Matcher m;
        Search.Where where = new Search.Where(file.getAbsolutePath());
        if (!options.reverseLogic) // it's faster to do it here than inside
            while ((line = raf.readLine()) != null) {
                m = p.matcher(line);
                boolean found = m.find();
                if (found) {
                    where.setLineNumber(lineNumber);
                    where.setLine(line);
                    result.append(where);
                }
                lineNumber ++;
            }
        else
            while ((line = raf.readLine()) != null) {
                m = p.matcher(line);
                boolean found = m.find();
                if (!found) {
                    where.setLineNumber(lineNumber);
                    where.setLine(line);
                    result.append(where);
                }
                lineNumber ++;
            }
    }

}
