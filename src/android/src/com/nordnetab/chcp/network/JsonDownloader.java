package com.nordnetab.chcp.network;

import com.nordnetab.chcp.utils.URLUtility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
abstract class JsonDownloader<T> {

    private String downloadUrl;

    protected abstract T createInstance(String json);

    public JsonDownloader(String url) {
        this.downloadUrl = url;
    }

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

        // many of these calls can throw exceptions, so i've just
        // wrapped them all in one try/catch statement.
        // create a url object
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
