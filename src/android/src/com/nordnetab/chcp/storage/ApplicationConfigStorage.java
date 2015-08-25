package com.nordnetab.chcp.storage;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.IPluginFilesStructure;
import com.nordnetab.chcp.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
public class ApplicationConfigStorage extends FileStorageAbs<ApplicationConfig> {

    private final String fileName;

    public ApplicationConfigStorage(IPluginFilesStructure filesStructure) {
        fileName = filesStructure.configFileName();
    }

    @Override
    protected ApplicationConfig createInstance(String json) {
        return ApplicationConfig.fromJson(json);
    }

    @Override
    protected String getFullPathForFileInFolder(String folder) {
        return Paths.get(folder, fileName);
    }
}
