#!/usr/bin/env groovy

package com.jintin.droidlane.framework

import com.google.api.services.androidpublisher.AndroidPublisher
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.jintin.droidlane.utils.NotifyUtils
import com.jintin.droidlane.utils.PublishUtils
import org.json.JSONObject

import java.security.GeneralSecurityException

abstract class DroidLaneTask<T> extends Task.Backgroundable {

    static def ERR_MSG = "message"
    static def ERR_DESC = "error_description"

    static def MSG_UPLOAD_START = "Uploading Start"
    static def MSG_UPLOAD_FAIL = "Upload Fail"
    static def MSG_UPLOAD_SUCCESS = "Update Success"

    T obj
    String pkgName
    JSONObject secret
    String result

    DroidLaneTask(Project project, T obj, JSONObject secret) {
        super(project, MSG_UPLOAD_START, true)
        this.obj = obj
        this.secret = secret
    }

    void setPkgName(String pkgName) {
        this.pkgName = pkgName
    }

    @Override
    void run(ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true)
        innerRun()
    }

    abstract void upload(AndroidPublisher.Edits edits, String editId);

    void innerRun() {
        try {
            // Create the API service.
            def service = PublishUtils.init(secret)
            def edits = service.edits()

            // Create a new edit to make changes to your listing.
            def editRequest = edits.insert(pkgName, null)
            def edit = editRequest.execute()
            def editId = edit.getId()

            upload(edits, editId)

            // Commit changes for edit.
            def commitRequest = edits.commit(pkgName, editId)
            commitRequest.execute()

        } catch (IOException | URISyntaxException | GeneralSecurityException ex) {
            def msg = ex.getMessage()
            println(msg)
            result = MSG_UPLOAD_FAIL + "\n" + msg

            def jsonIndex = msg.indexOf("{")
            if (jsonIndex != -1) {
                try {
                    def err = new JSONObject(msg.substring(jsonIndex))
                    if (!err.optString(ERR_DESC).empty) {
                        result = err.optString(ERR_DESC)
                    } else if (!err.optString(ERR_MSG).empty) {
                        result = err.getString(ERR_MSG)
                    }
                } catch (Exception ignore) {
                    println(ignore.toString())
                }
            }

        }
    }

    @Override
    void onSuccess() {
        if (result != null) {
            NotifyUtils.show(result, NotificationType.ERROR)
        } else {
            NotifyUtils.show(MSG_UPLOAD_SUCCESS, NotificationType.INFORMATION)
        }
    }

    @Override
    void onCancel() {
        NotifyUtils.show(MSG_UPLOAD_FAIL, NotificationType.ERROR)
    }
}
