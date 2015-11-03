package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.config.ContentManifest;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Helper class to download content manifest file from the server.
 *
 * @see ContentManifest
 * @see DownloadResult
 */
public class ContentManifestDownloader extends JsonDownloader<ContentManifest> {

    /**
     * Class constructor
     *
     * @param url url from where to download manifest
     */
    public ContentManifestDownloader(String url) {
        super(url);
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }
}
