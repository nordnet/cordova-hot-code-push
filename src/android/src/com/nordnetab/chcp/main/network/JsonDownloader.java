package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.utils.URLUtility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Helper class to download JSON and convert it into appropriate object.
 * Used internally.
 *
 * @see DownloadResult
 */
abstract class JsonDownloader<T> {

    private String downloadUrl;

    /**
     * Create instance of the object from json string.
     *
     * @param json loaded JSON string
     * @return instance of the object, created from the JSON string
     */
    protected abstract T createInstance(String json);

    /**
     * Class constructor
     *
     * @param url url from which JSON should be loaded
     */
    public JsonDownloader(String url) {
        this.downloadUrl = url;
    }

    /**
     * Perform download.
     *
     * @return result of the download
     * @see DownloadResult
     */
    public DownloadResult<T> download() {
        DownloadResult<T> result;

        try {
            String json = downloadJson();
            T value = createInstance(json);

            result = new DownloadResult<T>(value);
        } catch (Exception e) {
            e.printStackTrace();

            result = new DownloadResult<T>(e);
        }

        return result;
    }

    private String downloadJson() throws Exception {
        StringBuilder jsonContent = new StringBuilder();

        URL url = URLUtility.stringToUrl(downloadUrl);
        if (url == null) {
            throw new Exception("Invalid url format:" + downloadUrl);
        }

        URLConnection urlConnection = url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        char data[] = new char[1024];
        int count;
        while ((count = bufferedReader.read(data)) != -1) {
            jsonContent.append(data, 0, count);
        }
        bufferedReader.close();

        return jsonContent.toString();
    }
}
