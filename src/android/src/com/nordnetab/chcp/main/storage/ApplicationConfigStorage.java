package com.nordnetab.chcp.main.storage;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.IPluginFilesStructure;
import com.nordnetab.chcp.main.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Utility class to save and load application config from the certain folder.
 *
 * @see ApplicationConfig
 * @see IObjectFileStorage
 */
public class ApplicationConfigStorage extends FileStorageAbs<ApplicationConfig> {

    private final String fileName;

    /**
     * Class constructor
     *
     * @param filesStructure structure of plugins directories
     * @see IPluginFilesStructure
     */
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