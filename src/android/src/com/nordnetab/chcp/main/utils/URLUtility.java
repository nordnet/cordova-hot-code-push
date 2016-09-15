package com.nordnetab.chcp.main.utils;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Utility class to work with URL's.
 */
public class URLUtility {

    /**
     * Convert string representation of the url into URL object.
     *
     * @param urlString url to convert
     * @return url object
     * @see URL
     */
    public static URL stringToUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
            final URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
        } catch (Exception e) {
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e2) {
                Log.d("CHCP", "Failed to transfer url string \"" + urlString + "\" to actual url", e2);
            }
        }

        return url;
    }

    /**
     * Construct url from the provided paths.
     * Doesn't support url parameters. Only file path
     *
     * @param urlParts parts of the url
     * @return constructed url
     */
    public static String construct(String... urlParts) {
        if (urlParts == null || urlParts.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        String startingPart = removeStartingDash(urlParts[0]);
        startingPart = removeEndingDash(startingPart);
        if (!startingPart.startsWith("http")) {
            builder.append("http://");
        }
        builder.append(startingPart);

        for (int i = 1, len = urlParts.length; i < len; i++) {
            String urlPart = removeEndingDash(urlParts[i]);
            if (!urlPart.startsWith("/")) {
                builder.append("/");
            }

            builder.append(urlPart);
        }

        return builder.toString();
    }

    private static String removeStartingDash(String string) {
        if (string.startsWith("/")) {
            string = removeStartingDash(string.substring(1));
        }

        return string;
    }

    private static String removeEndingDash(String string) {
        if (string.endsWith("/")) {
            string = removeEndingDash(string.substring(0, string.length() - 1));
        }

        return string;
    }
}
