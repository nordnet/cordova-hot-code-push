package com.nordnetab.chcp.main.storage;

import android.text.TextUtils;

import com.nordnetab.chcp.main.utils.FilesUtility;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Base class for storing/loading file from the certain folder.
 * Object is stored in file as JSON string.
 *
 * @see IObjectFileStorage
 */
abstract class FileStorageAbs<T> implements IObjectFileStorage<T> {

    /**
     * Create instance of the object from the JSON string
     *
     * @param json JSON string
     * @return instance of the created object
     */
    protected abstract T createInstance(String json);

    /**
     * Getter for the path to the file from which we want to restore an object.
     *
     * @param folder absolute path to folder, where file lies
     * @return absolute path to the file
     */
    protected abstract String getFullPathForFileInFolder(String folder);

    @Override
    public boolean storeInFolder(T object, String folder) {
        final String pathToStorableFile = getFullPathForFileInFolder(folder);
        if (TextUtils.isEmpty(pathToStorableFile)) {
            return false;
        }

        try {
            FilesUtility.writeToFile(object.toString(), pathToStorableFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
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
            //e.printStackTrace();
        }

        return result;
    }
}