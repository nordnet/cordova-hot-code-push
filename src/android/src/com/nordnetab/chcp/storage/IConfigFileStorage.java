package com.nordnetab.chcp.storage;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public interface IConfigFileStorage<T> {

    boolean storeInFolder(T config, String folder);

    T loadFromFolder(String folder);
}
