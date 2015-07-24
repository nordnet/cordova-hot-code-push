package com.nordnetab.chcp.network;

import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.utils.MD5;
import com.nordnetab.chcp.utils.Paths;
import com.nordnetab.chcp.utils.URLUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Helper class to download files.
 */
public class FileDownloader {



    public static void downloadFiles(final String downloadFolder, final String contentFolderUrl, final List<ContentManifest.DiffFile> files) {
        boolean didFailDownload = false;
        for (ContentManifest.DiffFile file : files) {
            if (file.isRemoved) {
                continue;
            }

            //TODO: add feature to continue previous download
            String fileUrl = URLUtility.construct(contentFolderUrl, file.name);
            String filePath = Paths.get(downloadFolder, file.name);

            try {
                download(fileUrl, filePath, file.hash);
            } catch (IOException e) {
                didFailDownload = true;
                break;
            }
        }

        if (didFailDownload) {

        } else {

        }
    }

    /**
     * Download file from server, save it on the disk and check his hash.
     *
     * @param urlFrom  url to download from
     * @param filePath where to save file
     * @param checkSum checksum of the file
     * @return true - if file is downloaded and not corrupted; otherwise - false
     */
    public static void download(String urlFrom, String filePath, String checkSum) throws IOException {
        final File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        MD5 md5 = new MD5();

        URL downloadUrl = URLUtility.stringToUrl(urlFrom);
        if (downloadUrl == null) {
            throw new IOException("Invalid url format");
        }

        URLConnection connection = downloadUrl.openConnection();
        connection.connect();

        InputStream input = new BufferedInputStream(downloadUrl.openStream());
        OutputStream output = new BufferedOutputStream(new FileOutputStream(filePath, false));

        byte data[] = new byte[1024];
        int count;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
            md5.write(data, count);
        }

        output.flush();
        output.close();
        input.close();

        String downloadedFileHash = md5.calculateHash();
        if (!downloadedFileHash.equals(checkSum)) {
            throw new IOException("File is corrupted: checksum " + checkSum + " doesn't match " + downloadedFileHash);
        }
    }
}
