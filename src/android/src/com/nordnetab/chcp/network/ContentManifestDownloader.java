package com.nordnetab.chcp.network;

import com.nordnetab.chcp.config.ContentManifest;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 *
 */
public class ContentManifestDownloader extends JsonDownloader {

    public static class Result {

        public final ContentManifest manifest;
        public final Exception error;

        public Result(ContentManifest manifest) {
            this(manifest, null);
        }

        public Result(Exception e) {
            this(null, e);
        }

        public Result(ContentManifest manifest, Exception error) {
            this.manifest = manifest;
            this.error = error;
        }
    }

    public ContentManifestDownloader(String url) {
        super(url);
    }

    public Result download() {
        Result result;

        try {
            String json = downloadJson();
            ContentManifest manifest = ContentManifest.fromJson(json);

            result = new Result(manifest);
        } catch (Exception e) {
            e.printStackTrace();

            result = new Result(e);
        }

        return result;
    }
}
