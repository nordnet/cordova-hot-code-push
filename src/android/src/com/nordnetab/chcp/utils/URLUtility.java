package com.nordnetab.chcp.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
public class URLUtility {

    // TODO: make it URLEncode?
    public static URL stringToUrl(String urlString) {
        try {
            return new URL(URLDecoder.decode(urlString, "UTF-8"));
        } catch (Exception e) {
            try {
                return new URL(urlString);
            } catch (MalformedURLException e2) {
                return null;
            }
        }
    }

    // Doesn't support url parameters. Only file path
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

        for (int i=1, len=urlParts.length; i<len; i++) {
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
            string = removeEndingDash(string.substring(0, string.length()-1));
        }

        return string;
    }
}
