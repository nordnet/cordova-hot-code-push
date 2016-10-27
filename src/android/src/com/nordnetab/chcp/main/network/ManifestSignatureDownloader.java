package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.config.ManifestSignature;

import java.util.Map;

/**
 * Helper class to download manifest signature file from the server.
 *
 * @see ManifestSignature
 * @see DownloadResult
 */
public class ManifestSignatureDownloader extends JsonDownloader<ManifestSignature> {

    /**
     * Class constructor
     *
     * @param url            url from where to download manifest
     * @param requestHeaders additional request headers
     */
    public ManifestSignatureDownloader(final String url, final Map<String, String> requestHeaders) {
        super(url, requestHeaders);
    }

    @Override
    protected ManifestSignature createInstance(String json) {
        return ManifestSignature.fromJson(json);
    }
}
