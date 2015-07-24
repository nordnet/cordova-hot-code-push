package com.nordnetab.chcp.utils;

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
 */
public class FilesUtility {

    public static void delete(String pathToFileOrDirectory) {
        delete(new File(pathToFileOrDirectory));
    }

    public static void delete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                delete(child);
        }

        fileOrDirectory.delete();
    }

    public static void ensureDirectoryExists(String dirPath) {
        ensureDirectoryExists(new File(dirPath));
    }

    public static void ensureDirectoryExists(File dir) {
        if (dir != null && (!dir.exists() || !dir.isDirectory())) {
            dir.mkdirs();
        }
    }

    public static void copy(File src, File dst) throws IOException {
        ensureDirectoryExists(dst);

        if (src.isDirectory()) {
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

    private static void copyFile(File fromFile, File toFile) throws IOException{
        InputStream in =  new BufferedInputStream(new FileInputStream(fromFile));
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

    public static String readFromFile(String filePath) throws IOException {
        return readFromFile(new File(filePath));
    }

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

    public static void writeToFile(String content, String filePath) throws IOException {
        writeToFile(content, new File(filePath));
    }

    public static void writeToFile(String content, File dstFile) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dstFile, false));

        bufferedWriter.write(content);

        bufferedWriter.close();
    }

    public static String calculateFileHash(String filePath) throws Exception {
        return calculateFileHash(new File(filePath));
    }

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
