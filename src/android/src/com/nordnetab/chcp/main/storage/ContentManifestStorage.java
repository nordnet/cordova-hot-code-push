package com.nordnetab.chcp.main.storage;

import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Utility class to save and load content manifest file from the certain folder.
 *
 * @see ContentManifest
 * @see IObjectFileStorage
 */
public class ContentManifestStorage extends FileStorageAbs<ContentManifest> {

    private final String fileName;

    /**
     * Constructor.
     */
    public ContentManifestStorage() {
        fileName = PluginFilesStructure.MANIFEST_FILE_NAME;
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
