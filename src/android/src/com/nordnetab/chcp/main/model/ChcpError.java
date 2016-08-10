package com.nordnetab.chcp.main.model;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Enum for plugin specific errors.
 */
public enum ChcpError {

    NONE(0, ""),

    // region Actual errors

    FAILED_TO_DOWNLOAD_APPLICATION_CONFIG(-1, "Failed to download application configuration file"),
    APPLICATION_BUILD_VERSION_TOO_LOW(-2, "Application build version is too low for this update"),
    FAILED_TO_DOWNLOAD_CONTENT_MANIFEST(-3, "Failed to download content manifest file"),
    FAILED_TO_DOWNLOAD_UPDATE_FILES(-4, "Failed to download update files"),
    FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER(-5, "Failed to move downloaded files to the installation folder"),
    UPDATE_IS_INVALID(-6, "Update package is broken"),
    FAILED_TO_COPY_FILES_FROM_PREVIOUS_RELEASE(-7, "Can't copy files from previous release to the new release"),
    FAILED_TO_COPY_NEW_CONTENT_FILES(-8, "Failed to copy new files to content directory"),
    LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND(-9, "Failed to load current application config"),
    LOCAL_VERSION_OF_MANIFEST_NOT_FOUND(-10, "Failed to load current manifest file"),
    LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND(-11, "Failed to load application config from download folder"),
    LOADED_VERSION_OF_MANIFEST_NOT_FOUND(-12, "Failed to load content manifest from download folder"),
    FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE(-13, "Failed to copy assets from application bundle in to external storage"),
    CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS(-14, "Can't launch installation task while download is in progress"),
    CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS(-15, "Can't launch download task while installation is in progress"),
    INSTALLATION_ALREADY_IN_PROGRESS(-16, "Installation already in progress"),
    DOWNLOAD_ALREADY_IN_PROGRESS(-17, "Download already in progress"),
    ASSETS_FOLDER_IN_NOT_YET_INSTALLED(-18, "API requests are ignored until assets are installed. Please, wait."),
    NEW_APPLICATION_CONFIG_IS_INVALID(-19, "Downloaded new application config, but it is not valid"),
    // endregion

    // region Kind of warnings

    NOTHING_TO_INSTALL(1, "Nothing to install"),
    NOTHING_TO_UPDATE(2, "Nothing new to load from server");

    // endregion

    private int errorCode;
    private String errorDescription;

    ChcpError(int errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    /**
     * Getter for error code.
     * By this you can determine what went wrong.
     *
     * @return error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Getter for error description.
     *
     * @return error description
     */
    public String getErrorDescription() {
        return errorDescription;
    }
}
