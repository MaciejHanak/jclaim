package com.itbs.util;

import java.io.*;
import java.net.URL;

/**
 * All rights reserved.
 * Created by: ARass  on  Date: Apr 21, 2004
 */
public class ClassUtil {

    /**
     * This method composes the entire class; finds a reference file.
     *
     * @param filename the name of the reference file
     * @return name of the resource
     * @throws java.io.IOException when the file cannot be found or when the BufferedReader can't be closed
     */
    private static String getResourceName(String filename) throws IOException {
        if (filename == null || filename.equals("")) {
            throw new FileNotFoundException("The filename is null or empty.");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Throwable().printStackTrace(new PrintStream(baos));
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()));
        BufferedReader br = new BufferedReader(isr);
        try {
            br.readLine(); // Error message
            br.readLine(); // this function
            br.readLine(); // utility function

            String className = br.readLine().substring(4);
            int index = className.indexOf('(');
            index = className.lastIndexOf('.', index);
            index = className.lastIndexOf('.', index - 1);
            className = className.substring(0, index);
            className = "/" + className.replace('.', '/') + "/" + filename;
            return className;
        } finally {
            try { br.close(); } catch (IOException e) { } // noone cares
        }
    }

    /**
     * This method composes the entire class; finds a reference file.
     *
     * @param filename the name of the reference file
     * @return an InputStream for the reference file
     * @throws java.io.IOException when the file cannot be found or when the BufferedReader can't be closed
     */
    public static URL getURLFromCallersClassDirectory(String filename) throws IOException {
        String resourceName = getResourceName(filename);
        URL is = ClassUtil.class.getResource(resourceName);

        if (is == null) {
            throw new FileNotFoundException("The file could not be found: " + resourceName);
        }
        return is;
    }
    /**
     * This method composes the entire class; finds a reference file.
     *
     * @param filename the name of the reference file
     * @return an InputStream for the reference file
     * @throws java.io.IOException when the file cannot be found or when the BufferedReader can't be closed
     */
    public static String getFileContentFromCallersClassDirectory(String filename) throws IOException {
            String resourceName = getResourceName(filename);
            InputStream is = ClassUtil.class.getResourceAsStream(resourceName);
            if (is == null) {
                throw new FileNotFoundException("The file could not be found: " + resourceName);
            }
            InputStreamReader isr = new InputStreamReader(is);
            char[] buff = new char[32*1024];
            StringBuffer result = new StringBuffer(32 * 1024);
            while (is.available() > 0) {
                int size = isr.read(buff);
                if (size == -1) // eof
                    break;
                result.append(buff, 0, size);
            }
            return result.toString();
    }
    /**
     * This method composes the entire class; finds a reference file.
     *
     * @param resourceName the name of the reference file
     * @return an InputStream for the reference file
     * @throws java.io.IOException when the file cannot be found or when the BufferedReader can't be closed
     */
    public static Reader getReader(String resourceName) throws IOException {
        InputStream is = ClassUtil.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new FileNotFoundException("The resource could not be found: " + resourceName);
        }
        return  new InputStreamReader(is);
    }

    public static boolean compareWithNull(Object one, Object two) {
        return ((one == two) || (one != null && two != null && one.equals(two)));
    }

}
