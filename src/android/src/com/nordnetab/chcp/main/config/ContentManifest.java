package com.nordnetab.chcp.main.config;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Model for content manifest.
 * Content manifest is a configuration file, that holds the list of all web project files with they hashes.
 * Used to determine which files has been removed from the project, which are added or updated.
 */
public class ContentManifest {

    // region Json

    // keys to parse json
    private static class JsonKeys {
        public static final String FILE_PATH = "file";
        public static final String FILE_HASH = "hash";
    }

    /**
     * Create instance of the object from JSON string.
     * JSON string is a content of the chcp.manifest file.
     *
     * @param json JSON string to parse
     * @return content manifest instance
     */
    public static ContentManifest fromJson(String json) {
        ContentManifest manifest = new ContentManifest();
        try {
            JsonNode filesListNode = new ObjectMapper().readTree(json);
            for (JsonNode fileNode : filesListNode) {
                String fileName = fileNode.get(JsonKeys.FILE_PATH).asText();
                String fileHash = fileNode.get(JsonKeys.FILE_HASH).asText();
                manifest.files.add(new ManifestFile(fileName, fileHash));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        manifest.jsonString = json;

        return manifest;
    }

    /**
     * Convert object into JSON string
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        if (TextUtils.isEmpty(jsonString)) {
            jsonString = generateJson();
        }

        return jsonString;
    }

    private String generateJson() {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ArrayNode filesListNode = nodeFactory.arrayNode();
        for (ManifestFile fileEntry : files) {
            ObjectNode fileNode = nodeFactory.objectNode();
            fileNode.set(JsonKeys.FILE_PATH, nodeFactory.textNode(fileEntry.name));
            fileNode.set(JsonKeys.FILE_HASH, nodeFactory.textNode(fileEntry.hash));
            filesListNode.add(fileNode);
        }

        return filesListNode.toString();
    }

    // endregion

    private final List<ManifestFile> files;
    private String jsonString;

    private ContentManifest() {
        this.files = new ArrayList<ManifestFile>();
    }

    /**
     * Getter for list of web project files.
     *
     * @return list of files
     */
    public List<ManifestFile> getFiles() {
        return files;
    }

    /**
     * Find differences between this manifest and the new one.
     * Current object is considered as an old manifest.
     *
     * @param manifest new manifest, relative to which we will calculate the difference
     * @return calculated difference between manifests
     * @see ManifestDiff
     * @see ManifestFile
     */
    // TODO: need more cleaner way to find differences between two lists
    public ManifestDiff calculateDifference(ContentManifest manifest) {
        final List<ManifestFile> oldManifestFiles = files;
        final List<ManifestFile> newManifestFiles = (manifest != null && manifest.getFiles() != null)
                ? manifest.getFiles() : new ArrayList<ManifestFile>();

        final ManifestDiff diff = new ManifestDiff();
        final List<ManifestFile> changedFiles = diff.changedFiles();
        final List<ManifestFile> deletedFiles = diff.deletedFiles();
        final List<ManifestFile> addedFiles = diff.addedFiles();


        // find deleted and updated files
        for (ManifestFile oldFile : oldManifestFiles) {
            boolean isDeleted = true;
            for (ManifestFile newFile : newManifestFiles) {
                if (oldFile.name.equals(newFile.name)) {
                    isDeleted = false;
                    if (!newFile.hash.equals(oldFile.hash)) {
                        changedFiles.add(newFile);
                    }

                    break;
                }
            }
            if (isDeleted) {
                deletedFiles.add(oldFile);
            }
        }

        // find new files
        for (ManifestFile newFile : newManifestFiles) {
            boolean isFound = false;
            for (ManifestFile oldFile : oldManifestFiles) {
                if (newFile.name.equals(oldFile.name)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                addedFiles.add(newFile);
            }
        }

        return diff;
    }
}
