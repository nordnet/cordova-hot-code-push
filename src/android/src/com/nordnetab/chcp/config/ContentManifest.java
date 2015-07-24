package com.nordnetab.chcp.config;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
public class ContentManifest {

    public static class File {
        public final String name;
        public final String hash;

        public File(String name, String hash) {
            this.name = name;
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof File)) {
                return super.equals(o);
            }

            File comparedFile = (File)o;

            return comparedFile.name.equals(name) && comparedFile.hash.equals(hash);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class DiffFile extends File {
        public final boolean isRemoved;

        public DiffFile(File file, boolean isRemoved) {
            super (file.name, file.hash);

            this.isRemoved = isRemoved;
        }
    }

    // region Json

    private static class JsonKeys {
        public static final String FILE_PATH = "file";
        public static final String FILE_HASH = "hash";
    }

    public static ContentManifest fromJson(String json) {
        ContentManifest manifest = new ContentManifest();
        try {
            JsonNode filesListNode = new ObjectMapper().readTree(json);
            for (JsonNode fileNode : filesListNode) {
                String fileName = fileNode.get(JsonKeys.FILE_PATH).asText();
                String fileHash = fileNode.get(JsonKeys.FILE_HASH).asText();
                manifest.files.add(new File(fileName, fileHash));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        manifest.jsonString = json;

        return manifest;
    }

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
        for (File fileEntry : files) {
            ObjectNode fileNode = nodeFactory.objectNode();
            fileNode.set(JsonKeys.FILE_PATH, nodeFactory.textNode(fileEntry.name));
            fileNode.set(JsonKeys.FILE_HASH, nodeFactory.textNode(fileEntry.hash));
            filesListNode.add(fileNode);
        }

        return filesListNode.toString();
    }

    // endregion

    private final List<File> files;
    private String jsonString;

    private ContentManifest() {
        this.files = new ArrayList<File>();
    }

    public List<File> getFiles() {
        return files;
    }

    // TODO: need more cleaner way to find differences between two lists
    public List<DiffFile> calculateDifference(ContentManifest manifest) {
        final List<File> oldManifestFiles = files;
        final List<File> newManifestFiles = (manifest != null && manifest.getFiles() != null)
                ? manifest.getFiles() : new ArrayList<File>();
        final List<DiffFile> diffFilesList =  new ArrayList<DiffFile>();

        // find all removed and updated files
        for (File oldFile : oldManifestFiles) {
            boolean isDeleted = true;
            for (File newFile : newManifestFiles) {
                if (newFile.name.equals(oldFile.name)) {
                    isDeleted = false;
                    if (!newFile.hash.equals(oldFile.hash)) {
                        diffFilesList.add(new DiffFile(newFile, false));
                    }

                    break;
                }
            }

            if (isDeleted) {
                diffFilesList.add(new DiffFile(oldFile, true));
            }
        }

        // add new files
        for (File newFile : newManifestFiles) {
            boolean isInList = false;
            for (DiffFile diffFile : diffFilesList) {
                if (diffFile.name.equals(newFile.name)) {
                    isInList = true;
                    break;
                }
            }
            if (!isInList) {
                diffFilesList.add(new DiffFile(newFile, false));
            }
        }

        return diffFilesList;
    }
}
