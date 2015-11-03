package com.nordnetab.chcp.main.model;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 * <p/>
 * Interface that determines structure of the plugin working directories, like where
 * to download updates, from where install them and so on.
 */
public interface IPluginFilesStructure {

    /**
     * Absolute path to plugins working directory.
     *
     * @return plugins work directory
     */
    String contentFolder();

    /**
     * Absolute path to the folder on the external storage where all web content is placed.
     * From this folder we will show web pages.
     * Think of this as an assets folder on the external storage.
     *
     * @return path to web content folder
     */
    String wwwFolder();

    /**
     * Absolute path to the temporary folder where we will put files during the update download.
     *
     * @return path to download folder
     */
    String downloadFolder();

    /**
     * Absolute path to the temporary folder where we put backup of the current web content before
     * installing new version. If during the installation some error will happen - we will restore content
     * from this folder.
     *
     * @return path to backup folder
     */
    String backupFolder();

    /**
     * Absolute path to the temporary folder where new update is located.
     * Folder is created after update download. We will perform installation from it.
     *
     * @return path to installation folder
     */
    String installationFolder();

    /**
     * Getter for the name of the application config file.
     *
     * @return name of the application config
     */
    String configFileName();

    /**
     * Getter for the name of the manifest file.
     *
     * @return manifest file name
     */
    String manifestFileName();
}
