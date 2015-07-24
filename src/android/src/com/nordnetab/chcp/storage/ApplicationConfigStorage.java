package com.nordnetab.chcp.storage;

import android.content.Context;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
public class ApplicationConfigStorage extends Storage<ApplicationConfig> {

    private static final String CONFIG_FILE_NAME = "chcp.json";

    public ApplicationConfigStorage(Context applicationContext, String wwwFolder) {
        this(applicationContext, wwwFolder, CONFIG_FILE_NAME);
    }

    public ApplicationConfigStorage(Context applicationContext, String wwwFolder, String configFileName) {
        super(ApplicationConfig.class, applicationContext, Paths.get(wwwFolder, configFileName));
    }

    @Override
    protected ApplicationConfig createInstance(String json) {
        return ApplicationConfig.fromJson(json);
    }
}
