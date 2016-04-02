package com.nordnetab.chcp.main.network;

import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.utils.FilesUtility;
import com.nordnetab.chcp.main.utils.MD5;
import com.nordnetab.chcp.main.utils.Paths;
import com.nordnetab.chcp.main.utils.URLUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Helper class to download files.
 */
public class FileDownloader {
    private static int sConcurrencyLevel = 1;

    public static int getConcurrencyLevel() {
        return sConcurrencyLevel;
    }

    public static void setConcurrencyLevel(int concurrencyLevel) {
        sConcurrencyLevel = concurrencyLevel;
    }

    /**
     * Download file from server, save it on the disk and check his hash.
     *
     * @param urlFrom  url to download from
     * @param filePath where to save file
     * @param checkSum checksum of the file
     * @throws IOException
     */
    public static void download(String urlFrom, String filePath, String checkSum) throws IOException {
        Log.d("CHCP", "Loading file: " + urlFrom);

        File downloadFile = new File(filePath);
        FilesUtility.delete(downloadFile);
        FilesUtility.ensureDirectoryExists(downloadFile.getParentFile());

        MD5 md5 = new MD5();

        URL downloadUrl = URLUtility.stringToUrl(urlFrom);
        if (downloadUrl == null) {
            throw new IOException("Invalid url format");
        }

        // don't use SSLv3 to download files
        // see https://code.google.com/p/android/issues/detail?id=78187
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());

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

        if(TextUtils.isEmpty(checkSum)) {
            return;
        }

        String downloadedFileHash = md5.calculateHash();
        if (!downloadedFileHash.equals(checkSum)) {
            throw new IOException(String.format("File %s is corrupt: checksum %s doesn't match hash %s of the downloaded file", downloadFile.getPath(), checkSum, downloadedFileHash));
        }
    }

    /**
     * Download list of files.
     * Full url to the file is constructed from the contentFolderUrl and ManifestFile#hash (relative path).
     * For each downloaded file we perform check of his hash. If it is different from the one, that provided
     * if ManifestFile#hash - exception will be thrown.
     * Download stops on any error.
     *
     * @param downloadFolder   absolute path to the folder, where downloaded files should be placed
     * @param contentFolderUrl root url on the server, where all files are located
     * @param files            list of files to download
     * @throws IOException
     * @see ManifestFile
     */
    public static void downloadFiles(final String downloadFolder, final String contentFolderUrl, List<ManifestFile> files) throws Exception {
        List<Callable<Boolean>> downloadFileTasks = createAsyncDownloadTaskList(downloadFolder, contentFolderUrl, files);

        Log.d("CHCP",  String.format("Downloading files using %s thread(s)", sConcurrencyLevel));
        ExecutorService threadPool = Executors.newFixedThreadPool(sConcurrencyLevel);
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(threadPool);

        List<Future<Boolean>> downloadFileFutures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for(Callable<Boolean> downloadFileTask : downloadFileTasks) {
            downloadFileFutures.add(completionService.submit(downloadFileTask));
        }

        try {
            for(int i = 0; i < downloadFileFutures.size(); i++) {
                Future<Boolean> taskResult = completionService.take();
                taskResult.get();
            }
        } catch(InterruptedException ex) {
        } catch(ExecutionException ex) {
            cancelAllTasks(downloadFileFutures);
            throw ex;
        } finally {
            threadPool.shutdownNow();
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        Log.d("CHCP",  String.format("Downloading files finished successfully after %d ms", elapsedTime));
    }

    private static List<Callable<Boolean>> createAsyncDownloadTaskList(final String downloadFolder, final String contentFolderUrl, List<ManifestFile> files) {
        List<Callable<Boolean>> downloadFileTasks = new ArrayList<>();

        for (ManifestFile file : files) {
            final String sourceFileUrl = URLUtility.construct(contentFolderUrl, file.name);
            final String destinationFileUrl = Paths.get(downloadFolder, file.name);
            final String filesChecksum = file.hash;

            downloadFileTasks.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    // NOTE: the below line make sure that we always check the checksum of files that are downloaded from a manifest file!
                    if(TextUtils.isEmpty(filesChecksum)) {
                        throw new IllegalArgumentException("File checksum is missing");
                    }

                    download(sourceFileUrl, destinationFileUrl, filesChecksum);

                    return Boolean.TRUE;
                }
            });
        }

        return downloadFileTasks;
    }

    private static void cancelAllTasks(List<Future<Boolean>> downloadTaskFutures) {
        for(Future<Boolean> downloadTaskFuture : downloadTaskFutures) {
            downloadTaskFuture.cancel(true);
        }
    }
}
