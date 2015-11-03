package com.nordnetab.chcp.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 * <p/>
 * Model describes difference between two manifest files.
 *
 * @see ManifestFile
 */
public class ManifestDiff {

    private List<ManifestFile> deleted;
    private List<ManifestFile> changed;
    private List<ManifestFile> added;

    /**
     * Class constructor
     */
    public ManifestDiff() {
        added = new ArrayList<ManifestFile>();
        changed = new ArrayList<ManifestFile>();
        deleted = new ArrayList<ManifestFile>();
    }

    /**
     * Getter for the list of deleted files.
     *
     * @return deleted files
     */
    public List<ManifestFile> deletedFiles() {
        return deleted;
    }

    /**
     * Getter for the list of existing files that has been changed.
     *
     * @return changed files
     */
    public List<ManifestFile> changedFiles() {
        return changed;
    }

    /**
     * Getter for the list of new files, that were added to the project.
     *
     * @return added files
     */
    public List<ManifestFile> addedFiles() {
        return added;
    }

    /**
     * Check if there is any difference between the manifest files.
     *
     * @return <code>true</code> if manifest files are the same; <code>false</code> - otherwise
     */
    public boolean isEmpty() {
        return added.isEmpty() && changed.isEmpty() && deleted.isEmpty();
    }

    /**
     * Getter for the combined list of added and changed files.
     *
     * @return list of changed and added files
     */
    public List<ManifestFile> getUpdateFiles() {
        List<ManifestFile> updateList = new ArrayList<ManifestFile>();
        updateList.addAll(added);
        updateList.addAll(changed);

        return updateList;
    }
}
