#!/usr/bin/env groovy

package com.jintin.droidlane

import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.ApkListing
import com.google.api.services.androidpublisher.model.Track
import com.intellij.openapi.project.Project
import com.jintin.droidlane.framework.DroidLaneTask
import org.json.JSONObject

class ApkTask extends DroidLaneTask<ApkObj> {

    static def MIME_TYPE_APK = "application/vnd.android.package-archive"

    ApkTask(Project project, ApkObj obj, JSONObject secret) {
        super(project, obj, secret)
        setPkgName(obj.pkgName)
    }

    @Override
    void upload(AndroidPublisher.Edits edits, String editId) {

        // Upload new apk to developer console
        def uploadRequest = edits.apks()
                .upload(pkgName, editId, new FileContent(MIME_TYPE_APK, obj.apk))
        def apk = uploadRequest.execute()

        // Assign apk to track.
        def apkVersionCodes = new ArrayList<>()
        apkVersionCodes.add(apk.getVersionCode())
        def updateTrackRequest = edits.tracks()
                .update(pkgName, editId, obj.track, new Track().setVersionCodes(apkVersionCodes))
        updateTrackRequest.execute()

        // Update recent changes field in apk listing.
        if (obj.changeList != null) {
            for (def lang : obj.changeList.keySet()) {
                def apkListing = new ApkListing()
                apkListing.setRecentChanges(obj.changeList.get(lang))
                def updateRecentChangesRequest = edits.apklistings()
                        .update(pkgName, editId, apk.getVersionCode(), lang, apkListing)
                updateRecentChangesRequest.execute()
            }
        }
    }
}
