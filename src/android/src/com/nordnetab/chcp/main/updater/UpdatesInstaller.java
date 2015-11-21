package com.nordnetab.chcp.main.updater;

import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.model.IPluginFilesStructure;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Utility class to perform update installation.
 */
public class UpdatesInstaller {

    private static boolean isInstalling;

    /**
     * Request update installation.
     * Installation performed in background. Events are dispatched to notify us about the result.
     *
     * @param filesStructure structure of the plugin working directories
     * @return <code>true</code> if installation has started; <code>false</code> - otherwise
     * @see IPluginFilesStructure
     * @see NothingToInstallEvent
     * @see com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent
     * @see com.nordnetab.chcp.main.events.UpdateInstalledEvent
     */
    public static boolean install(final IPluginFilesStructure filesStructure) {
        if (isInstalling) {
            return false;
        }

        if (!new File(filesStructure.installationFolder()).exists()) {
            EventBus.getDefault().post(new NothingToInstallEvent(null));
            return false;
        }

        InstallationWorker task = new InstallationWorker(filesStructure);
        execute(task);

        return true;
    }

    /**
     * Check if we are currently doing some installation.
     *
     * @return <code>true</code> - installation is in progress; <code>false</code> - otherwise
     */
    public static boolean isInstalling() {
        return isInstalling;
    }

    private static void execute(final InstallationWorker task) {
        isInstalling = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
                isInstalling = false;
            }
        }).start();
    }
}