package com.nordnetab.chcp.model;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public enum ChcpError {

    FAILED_TO_DOWNLOAD_APPLICATION_CONFIG(-1, "Failed to download application configuration file"),
    APPLICATION_BUILD_VERSION_TOO_LOW(-2, "Application build version is too low for this update"),
    FAILED_TO_DOWNLOAD_CONTENT_MANIFEST(-3, "Failed to download content manifest file"),
    FAILED_TO_DOWNLOAD_UPDATE_FILES(-4, "Failed to download update files"),
    FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER(-5, "Failed to move downloaded files to the installation folder"),

    UPDATE_IS_INVALID(-100, "Update package is broken"),
    FAILED_TO_CREATE_BACKUP(-101, "Could not create backup before the installation"),
    FAILED_TO_COPY_NEW_CONTENT_FILES(-102, "Failed to copy new files to content directory");

    private int errorCode;
    private String errorDescription;

    ChcpError(int errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }


}
