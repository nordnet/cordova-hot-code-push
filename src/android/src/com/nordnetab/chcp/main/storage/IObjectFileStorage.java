package com.nordnetab.chcp.main.storage;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Interface that describes utility class, which does saving/loading of the object to/from folder.
 */
public interface IObjectFileStorage<T> {

    /**
     * Save object into the folder. Object will be stored as JSON string.
     * Actual path to the file in the folder, where object is putted is determined by the implementation class.
     *
     * @param config object to store
     * @param folder absolute path to folder, where to save the object
     * @return <code>true</code> if object is saved; <code>false</code> - otherwise
     */
    boolean storeInFolder(T config, String folder);

    /**
     * Load object from folder.
     *
     * @param folder absolute path to the folder, where object lies.
     * @return instance of the object, loaded from the provided folder.
     */
    T loadFromFolder(String folder);
}
