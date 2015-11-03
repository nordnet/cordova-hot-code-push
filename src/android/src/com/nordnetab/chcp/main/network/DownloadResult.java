package com.nordnetab.chcp.main.network;

/**
 * Created by Nikolay Demyankov on 28.08.15.
 * <p/>
 * Class holds information about download result like it's value and error details.
 */
public class DownloadResult<T> {

    /**
     * Loaded data
     */
    public final T value;

    /**
     * Occurred error, if any
     */
    public final Exception error;

    /**
     * Class constructor
     *
     * @param value loaded value
     */
    public DownloadResult(T value) {
        this(value, null);
    }

    /**
     * Class constructor
     *
     * @param error occurred error
     */
    public DownloadResult(Exception error) {
        this(null, error);
    }

    private DownloadResult(T value, Exception error) {
        this.value = value;
        this.error = error;

    }
}