#!/usr/bin/env groovy

package com.jintin.droidlane

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import com.jintin.droidlane.framework.DroidLaneAction

class ApkAction extends DroidLaneAction<ArrayList<ApkObj>> {

    static def MSG_SELECT_TRACK = "select track to upload"

    ApkAction() {
        super("DroidLane Apk")
    }

    @Override
    ArrayList<ApkObj> getData(String path) {
        return ApkObj.fromFile(project, path)
    }

    @Override
    void generateTask(ArrayList<ApkObj> datas, String password) {
        for (def apkObj : datas) {
            def secret = getSecret(apkObj.client_id, password)
            if (secret == null) {
                return
            }
            if (apkObj.track.empty) {
                def TRACKS = ["alpha", "beta", "production"] as String[]
                apkObj.track = TRACKS[Messages.showChooseDialog(MSG_SELECT_TRACK, TITLE, TRACKS, TRACKS[0], Messages.getInformationIcon())]
            }

            def task = new ApkTask(project, apkObj, secret)
            ProgressManager.getInstance().run(task)
        }
    }
}