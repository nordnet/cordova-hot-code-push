package com.nordnetab.chcp.storage;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public interface IConfigPreferenceStorage<T> {

    boolean storeInPreference(T config);

    T loadFromPreference();
}
