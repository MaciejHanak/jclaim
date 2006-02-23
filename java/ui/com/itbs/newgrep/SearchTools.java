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

import java.util.regex.Pattern;

/**
 * @author Alex Rass
 * @since Nov 20, 2005
 */
public class SearchTools {
    static String getPatternOptions(String what, Options options) {
        what = options.regularExpression ? what : Pattern.quote(what);
        if (options.beginningOfALine) {
            what = "^" + what;
        }
        if (options.endOfALine) {
            what += "$";
        }
        return what;
    }

    static int getRegexOptions(Options options) {
        int Options = 0;//Pattern.LITERAL;
        if (options.canonEquivalence) {
          // In Unicode, certain characters can be encoded in more than one way.
          // Many letters with diacritics can be encoded as a single character
          // identifying the letter with the diacritic, and encoded as two
          // characters: the letter by itself followed by the diacritic by itself
          // Though the internal representation is different, when the string is
          // rendered to the screen, the result is exactly the same.
          Options |= Pattern.CANON_EQ;
        }
        if (!options.caseSensitive) {
          // Omitting UNICODE_CASE causes only US ASCII characters to be matched
          // case insensitively.  This is appropriate if you know beforehand that
          // the subject string will only contain US ASCII characters
          // as it speeds up the pattern matching.
          Options |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
/*
        if (checkDotAll.isSelected()) {
          // By default, the dot will not match line break characters.
          // Specify this option to make the dot match all characters,
          // including line breaks
          Options |= Pattern.DOTALL;
        }
*/
/*
        if (checkMultiLine.isSelected()) {
          // By default, the caret ^, dollar $  only match at the start
          // and the end of the string.  Specify this option to make ^ also match
          // after line breaks in the string, and make $ match before line breaks.
          Options |= Pattern.MULTILINE;
        }
*/
        return Options;
      }
}
