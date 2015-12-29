package com.nordnetab.chcp.main.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nikolay Demyankov on 21.07.15.
 * <p/>
 * Helper class to work with file system.
 */
public class FilesUtility {

    /**
     * Delete object at the given location.
     * If it is a folder - it will be deleted recursively will all content.
     *
     * @param pathToFileOrDirectory absolute path to the file/directory.
     */
    public static void delete(String pathToFileOrDirectory) {
        delete(new File(pathToFileOrDirectory));
    }

    /**
     * Delete file object.
     * If it is a folder - it will be deleted recursively will all content.
     *
     * @param fileOrDirectory file/directory to delete
     */
    public static void delete(File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            return;
        }

        if (fileOrDirectory.isDirectory()) {
            File[] filesList = fileOrDirectory.listFiles();
            for (File child : filesList) {
                delete(child);
            }
        }

        final File to = new File(fileOrDirectory.getAbsolutePath() + System.currentTimeMillis());
        fileOrDirectory.renameTo(to);
        to.delete();

        //fileOrDirectory.delete();
    }

    /**
     * Check if folder exists at the given path.
     * If not - it will be created with with all subdirectories.
     *
     * @param dirPath absolute path to the directory
     */
    public static void ensureDirectoryExists(String dirPath) {
        ensureDirectoryExists(new File(dirPath));
    }

    /**
     * Check if folder exists.
     * If not - it will be created with with all subdirectories.
     *
     * @param dir file object
     */
    public static void ensureDirectoryExists(File dir) {
        if (dir != null && (!dir.exists() || !dir.isDirectory())) {
            dir.mkdirs();
        }
    }

    /**
     * Copy object from one place to another.
     * Can be used to copy file to file, or folder to folder.
     *
     * @param src absolute path to source object
     * @param dst absolute path to destination object
     * @throws IOException
     */
    public static void copy(String src, String dst) throws IOException {
        copy(new File(src), new File(dst));
    }

    /**
     * Copy file object from one place to another.
     * Can be used to copy file to file, or folder to folder.
     *
     * @param src source file
     * @param dst destination file
     * @throws IOException
     */
    public static void copy(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            ensureDirectoryExists(dst);

            String[] filesList = src.list();
            for (String file : filesList) {
                File srcFile = new File(src, file);
                File destFile = new File(dst, file);

                copy(srcFile, destFile);
            }
        } else {
            copyFile(src, dst);
        }
    }

    private static void copyFile(File fromFile, File toFile) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(fromFile));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(toFile));

        // Transfer bytes from in to out
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     * Read data as string from the provided file.
     *
     * @param filePath absolute path to the file
     * @return data from file
     * @throws IOException
     */
    public static String readFromFile(String filePath) throws IOException {
        return readFromFile(new File(filePath));
    }

    /**
     * Read data as string from the provided file.
     *
     * @param file file to read from
     * @return data from file
     * @throws IOException
     */
    public static String readFromFile(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line).append("\n");
        }

        bufferedReader.close();

        return content.toString().trim();
    }

    /**
     * Save string into the file
     *
     * @param content  data to store
     * @param filePath absolute path to file
     * @throws IOException
     */
    public static void writeToFile(String content, String filePath) throws IOException {
        writeToFile(content, new File(filePath));
    }

    /**
     * Save string into the file
     *
     * @param content data to store
     * @param dstFile where to save
     * @throws IOException
     */
    public static void writeToFile(String content, File dstFile) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dstFile, false));

        bufferedWriter.write(content);

        bufferedWriter.close();
    }

    /**
     * Calculate MD5 hash of the file at the given path
     *
     * @param filePath absolute path to the file
     * @return calculated hash
     * @throws Exception
     * @see MD5
     */
    public static String calculateFileHash(String filePath) throws Exception {
        return calculateFileHash(new File(filePath));
    }

    /**
     * Calculate MD5 hash of the file
     *
     * @param file file whose hash we need
     * @return calculated hash
     * @throws Exception
     * @see MD5
     */
    public static String calculateFileHash(File file) throws Exception {
        MD5 md5 = new MD5();
        InputStream in = new BufferedInputStream(new FileInputStream(file));

        int len;
        byte[] buff = new byte[8192];
        while ((len = in.read(buff)) > 0) {
            md5.write(buff, len);
        }

        return md5.calculateHash();
    }
}