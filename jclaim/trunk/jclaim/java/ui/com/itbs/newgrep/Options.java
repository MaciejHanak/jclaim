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
