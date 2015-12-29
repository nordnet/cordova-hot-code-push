package com.nordnetab.chcp.main.updater;

import com.nordnetab.chcp.main.events.WorkerEvent;

/**
 * Created by Nikolay Demyankov on 29.12.15.
 */
interface WorkerTask extends Runnable {

    WorkerEvent result();

}
