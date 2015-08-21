package com.nordnetab.chcp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 */
public class ManifestDiff {

    private List<ManifestFile> deleted;
    private List<ManifestFile> changed;
    private List<ManifestFile> added;

    public ManifestDiff() {
        added = new ArrayList<ManifestFile>();
        changed = new ArrayList<ManifestFile>();
        deleted = new ArrayList<ManifestFile>();
    }

    public List<ManifestFile> deletedFiles() {
        return deleted;
    }

    public List<ManifestFile> changedFiles() {
        return changed;
    }

    public List<ManifestFile> addedFiles() {
        return added;
    }

    public boolean isEmpty() {
        return added.isEmpty() && changed.isEmpty() && deleted.isEmpty();
    }

    public List<ManifestFile> getUpdateFiles() {
        List<ManifestFile> updateList = new ArrayList<ManifestFile>();
        updateList.addAll(added);
        updateList.addAll(changed);

        return updateList;
    }

}
