package com.itbs.newgrep;

/**
 * @author Alex Rass
 * @since Nov 13, 2005
 */
public class Options implements Cloneable {
    boolean caseSensitive;
    boolean beginningOfALine; // ^
    boolean endOfALine;       // $
    // X|Y: x or y

    public boolean canonEquivalence=true;
    public boolean clearOutput;

//    Count Lines=1
//Count Words=0
//Progress Indicator=1
//Total Clear=0
//Load file list=0
    boolean reverseLogic;
    boolean printFilenameInOuput = true;
    boolean silentFinish;
    public boolean fileSearch;
    public boolean logSearch;
    public boolean regularExpression;

    public Object clone() {
        Options options = new Options();
        options.caseSensitive = caseSensitive;
        options.beginningOfALine = beginningOfALine;
        options.endOfALine = endOfALine;
        options.clearOutput = clearOutput;
        options.reverseLogic = reverseLogic;
        options.printFilenameInOuput = printFilenameInOuput;
        options.fileSearch = fileSearch;
        options.logSearch = logSearch;
        options.silentFinish = silentFinish;
        options.regularExpression = regularExpression;
//        options. = ;

        return options;
    }
}
