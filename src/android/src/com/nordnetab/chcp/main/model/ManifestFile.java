package com.nordnetab.chcp.main.model;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 * <p/>
 * Model holds information about file in web project.
 */
public class ManifestFile {

    /**
     * Relative path to the file inside the web project.
     */
    public final String name;

    /**
     * Hash of the file.
     * By this we will detect if project file has changed.
     */
    public final String hash;

    /**
     * Class constructor
     *
     * @param name relative path to the file in project
     * @param hash hash of the file
     */
    public ManifestFile(String name, String hash) {
        this.name = name;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof ManifestFile)) {
            return super.equals(o);
        }

        ManifestFile comparedFile = (ManifestFile) o;

        return comparedFile.name.equals(name) && comparedFile.hash.equals(hash);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
