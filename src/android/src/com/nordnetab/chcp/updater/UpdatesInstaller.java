package com.nordnetab.chcp.updater;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.events.NothingToInstallEvent;
import com.nordnetab.chcp.model.ChcpError;
import com.nordnetab.chcp.model.IPluginFilesStructure;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
public class UpdatesInstaller {

    private static boolean isInstalling;

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

    private static void execute(final InstallationWorker task) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isInstalling = true;
                task.run();
                isInstalling = false;
            }
        }).start();
    }

    public static boolean isInstalling() {
        return isInstalling;
    }
}
