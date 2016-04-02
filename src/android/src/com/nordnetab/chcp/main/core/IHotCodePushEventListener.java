package com.nordnetab.chcp.main.core;

import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.events.AutoDownloadNotAllowedErrorEvent;
import com.nordnetab.chcp.main.events.AutoInstallNotAllowedErrorEvent;
import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.RollbackPerformedEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;

/**
 * Created by orarnon on 29/02/2016.
 */
public interface IHotCodePushEventListener {
    void onEvent(AssetsInstalledEvent event);

    void onEvent(AssetsInstallationErrorEvent event);

    void onEvent(AutoDownloadNotAllowedErrorEvent event);

    void onEvent(UpdateIsReadyToInstallEvent event);

    void onEvent(NothingToUpdateEvent event);

    void onEvent(UpdateDownloadErrorEvent event);

    void onEvent(AutoInstallNotAllowedErrorEvent event);

    void onEvent(UpdateInstalledEvent event);

    void onEvent(UpdateInstallationErrorEvent event);

    void onEvent(NothingToInstallEvent event);

    void onEvent(RollbackPerformedEvent event);
}
