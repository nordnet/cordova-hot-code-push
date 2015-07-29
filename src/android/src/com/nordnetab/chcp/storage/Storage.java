package com.nordnetab.chcp.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.nordnetab.chcp.utils.FilesUtility;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 */
abstract class Storage<T> {

    private static final String PREFS_FILE = "cordova-hot-code-push.prefs";
    private final String prefName;
    private SharedPreferences preferences;
    private final String pathToStorableFile;

    protected abstract T createInstance(String json);

    public Storage(Class<T> storableClass, Context applicationContext, String pathToFile) {
        preferences = applicationContext.getSharedPreferences(PREFS_FILE, 0);
        prefName = storableClass.getCanonicalName();
        pathToStorableFile = pathToFile;
    }

    public void storeInPreference(T object) {
        preferences.edit().putString(prefName, object.toString()).apply();
    }

    public void clearPreference() {
        preferences.edit().remove(prefName).apply();
    }

    public T loadFromPreference() {
        String prefValue = preferences.getString(prefName, "");
        if (TextUtils.isEmpty(prefValue)) {
            return null;
        }

        return createInstance(prefValue);
    }

    public boolean storeOnFS(T object) {
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

    public T loadFromFS() {
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
