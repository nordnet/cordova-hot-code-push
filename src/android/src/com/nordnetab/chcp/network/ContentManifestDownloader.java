package com.nordnetab.chcp.network;

import com.nordnetab.chcp.config.ContentManifest;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 *
 */
public class ContentManifestDownloader extends JsonDownloader<ContentManifest> {

    public ContentManifestDownloader(String url) {
        super(url);
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }
}
