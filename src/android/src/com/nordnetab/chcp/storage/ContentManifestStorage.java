package com.nordnetab.chcp.storage;

import android.content.Context;

import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
public class ContentManifestStorage extends Storage<ContentManifest> {

    private static final String MANIFEST_FILE_NAME = "chcp.manifest";

    public ContentManifestStorage(Context applicationContext, String wwwFolder) {
        this(applicationContext, wwwFolder, MANIFEST_FILE_NAME);
    }

    public ContentManifestStorage(Context applicationContext, String wwwFolder, String manifestFileName) {
        super(ContentManifest.class, applicationContext, Paths.get(wwwFolder, manifestFileName));
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }
}
