package com.nordnetab.chcp.model;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 */
public class ManifestFile {
    public final String name;
    public final String hash;

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

        ManifestFile comparedFile = (ManifestFile)o;

        return comparedFile.name.equals(name) && comparedFile.hash.equals(hash);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
