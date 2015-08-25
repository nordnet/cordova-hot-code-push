package com.nordnetab.chcp.storage;

import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.model.IPluginFilesStructure;
import com.nordnetab.chcp.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
public class ContentManifestStorage extends FileStorageAbs<ContentManifest> {

    private final String fileName;

    public ContentManifestStorage(IPluginFilesStructure filesStructure) {
        fileName = filesStructure.manifestFileName();
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }

    @Override
    protected String getFullPathForFileInFolder(String folder) {
        return Paths.get(folder, fileName);
    }
}
