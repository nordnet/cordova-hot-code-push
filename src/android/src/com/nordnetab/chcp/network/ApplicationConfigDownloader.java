package com.nordnetab.chcp.network;

import com.nordnetab.chcp.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 */
public class ApplicationConfigDownloader extends JsonDownloader {

    public static class Result {

        public final ApplicationConfig config;
        public final Exception error;

        public Result(ApplicationConfig config) {
            this(config, null);
        }

        public Result(Exception error) {
            this(null, error);
        }

        public Result(ApplicationConfig config, Exception error) {
            this.config = config;
            this.error = error;
        }
    }

    public ApplicationConfigDownloader(String url) {
        super(url);
    }

    public Result download() {
        Result result;

        try {
            String json = downloadJson();
            ApplicationConfig config = ApplicationConfig.fromJson(json);

            result = new Result(config);
        } catch (Exception e) {
            e.printStackTrace();

            result = new Result(e);
        }

        return result;
    }
}
