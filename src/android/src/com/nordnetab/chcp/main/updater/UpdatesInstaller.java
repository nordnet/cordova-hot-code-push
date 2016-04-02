package com.nordnetab.chcp.main.updater;

import android.content.Context;
import android.util.Log;

import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;

import java.io.File;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Utility class to perform update installation.
 */
public class UpdatesInstaller {

    private static boolean isInstalling;

    /**
     * Check if we are currently doing some installation.
     *
     * @return <code>true</code> - installation is in progress; <code>false</code> - otherwise
     */
    public static boolean isInstalling() {
        return isInstalling;
    }

    /**
     * Request update installation.
     * Installation performed in background. Events are dispatched to notify us about the result.
     *
     * @param context        application context
     * @param newVersion     version to install
     * @param currentVersion current content version
     * @return <code>ChcpError.NONE</code> if installation started; otherwise - error details
     * @see NothingToInstallEvent
     * @see com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent
     * @see com.nordnetab.chcp.main.events.UpdateInstalledEvent
     */
    public static ChcpError install(final Context context, final String newVersion, final String currentVersion) {
        // if we already installing - exit
        if (isInstalling) {
            return ChcpError.INSTALLATION_ALREADY_IN_PROGRESS;
        }

        // if we are loading update - exit
        if (UpdatesLoader.isExecuting()) {
            return ChcpError.CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS;
        }

        final PluginFilesStructure newReleaseFS = new PluginFilesStructure(context, newVersion);
        if (!new File(newReleaseFS.getDownloadFolder()).exists()) {
            return ChcpError.NOTHING_TO_INSTALL;
        }

        final WorkerTask task = new InstallationWorker(context, newVersion, currentVersion);
        execute(task);

        return ChcpError.NONE;
    }

    private static void execute(final WorkerTask task) {
        isInstalling = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
                isInstalling = false;

                // dispatch resulting event
                EventBus.getDefault().post(task.result());
            }
        }).start();
    }
}