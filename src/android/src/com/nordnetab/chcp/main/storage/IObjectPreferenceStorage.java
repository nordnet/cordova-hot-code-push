package com.nordnetab.chcp.main.storage;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Interface describes utility class that performs storing/loading of the object to/from the SharedPreferences.
 *
 * @see android.content.SharedPreferences
 */
public interface IObjectPreferenceStorage<T> {

    /**
     * Store object into the shared preference
     *
     * @param object object to store
     * @return <code>true</code> if object is saved; <code>false</code> - otherwise
     */
    boolean storeInPreference(T object);

    /**
     * Load object from shared preference.
     *
     * @return instance of the restored object
     */
    T loadFromPreference();
}
