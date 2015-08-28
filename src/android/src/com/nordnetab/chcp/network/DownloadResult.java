package com.nordnetab.chcp.network;

/**
 * Created by Nikolay Demyankov on 28.08.15.
 *
 *
 */
public class DownloadResult<T> {
    public final T value;
    public final Exception error;

    public DownloadResult(T value) {
        this(value, null);
    }

    public DownloadResult(Exception e) {
        this(null, e);
    }

    public DownloadResult(T value, Exception error) {
        this.value = value;
        this.error = error;

    }
}
