package com.nordnetab.chcp.storage;

import android.text.TextUtils;

import com.nordnetab.chcp.utils.FilesUtility;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
abstract class FileStorageAbs<T> implements IConfigFileStorage<T> {

    protected abstract T createInstance(String json);
    protected abstract String getFullPathForFileInFolder(String folder);

    public boolean storeInFolder(T object, String folder) {
        final String pathToStorableFile = getFullPathForFileInFolder(folder);
        if (TextUtils.isEmpty(pathToStorableFile)) {
            return false;
        }

        try {
            FilesUtility.writeToFile(object.toString(), pathToStorableFile);

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    public T loadFromFolder(String folder) {
        final String pathToStorableFile = getFullPathForFileInFolder(folder);
        if (TextUtils.isEmpty(pathToStorableFile)) {
            return null;
        }

        T result = null;
        try {
            String json = FilesUtility.readFromFile(pathToStorableFile);
            result = createInstance(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
