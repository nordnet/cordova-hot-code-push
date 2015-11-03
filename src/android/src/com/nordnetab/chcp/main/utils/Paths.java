package com.nordnetab.chcp.main.utils;

/**
 * Created by Nikolay Demyankov on 21.07.15.
 * <p/>
 * Since Cordova doesn't support Java 7 (according to the gradle) - we have to use our own Paths class.
 * Once it moves to Java 7 - we can use java.nio.file.Path
 */
public class Paths {

    /**
     * Construct path from the given set of paths.
     *
     * @param paths list of paths to concat
     * @return resulting path
     */
    public static String get(String... paths) {
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            builder.append(normalizeDashes(path));
        }

        return builder.toString();
    }

    private static String normalizeDashes(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }
}