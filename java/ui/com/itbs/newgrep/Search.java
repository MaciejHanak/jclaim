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
