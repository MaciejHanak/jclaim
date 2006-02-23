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

/**
 * @author Alex Rass
 * @since  Jul 20, 2005
 */
public interface Search {
    /** Defines search parameters */
    public interface What {
        void find(File where, What what, Result result, Options options) throws IOException;
//        boolean isCaseSensitive();
    }

    /** Defines where match was found.  Transient. */
    class Where {
        private String file;
        private int lineNumber;
        private String line;
        private long filePosition;

        public Where(String file) {
            this.file = file;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setFilePosition(long filePosition) {
            this.filePosition = filePosition;
        }

        public long getFilePosition() {
            return filePosition;
        }

        /**
         * 0-based.
         * @param lineNumber to set
         */
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getFile() {
            return file;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }
    }

    /** Callback */
    public interface Result {
        void startResource(String name);
        void append(Where whatFound);
    }

} // interface
