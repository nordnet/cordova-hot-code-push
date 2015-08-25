package com.nordnetab.chcp.model;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 */
public interface PluginFilesStructure {

    String contentFolder();

    String wwwFolder();

    String downloadFolder();

    String backupFolder();

    String installationFolder();

    String configFileName();

    String manifestFileName();

}
