package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.config.ContentManifest;

import java.util.Map;

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
     * @param url            url from where to download manifest
     * @param requestHeaders additional request headers
     */
    public ContentManifestDownloader(final String url, final Map<String, String> requestHeaders) {
        super(url, requestHeaders);
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }
}
