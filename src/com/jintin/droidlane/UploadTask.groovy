package com.jintin.droidlane

import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.model.ApkListing
import com.google.api.services.androidpublisher.model.Track
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.jintin.droidlane.utils.PublishUtils
import org.json.JSONObject

import java.security.GeneralSecurityException

class UploadTask extends Task.Backgroundable {

    static def TITLE = "DroidLane"

    static def MIME_TYPE_APK = "application/vnd.android.package-archive"
    static def ERR_MSG = "message"
    static def ERR_DESC = "error_description"

    static def MSG_UPLOAD_START = "Uploading apk"
    static def MSG_UPLOAD_FAIL = "Upload Fail"
    static def MSG_UPLOAD_SUCCESS = "Update Success"

    private String name
    private JSONObject secret
    private File apkFile
    private String track
    private Map<String, String> changeList

    UploadTask(Project project, String name, JSONObject secret, File apkFile, String track, Map<String, String> changeList) {
        super(project, MSG_UPLOAD_START, true)
        this.name = name
        this.secret = secret
        this.apkFile = apkFile
        this.track = track
        this.changeList = changeList
    }

    @Override
    void run(ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true)
        upload()
    }

    void upload() {
        try {
            // Create the API service.
            def service = PublishUtils.init(secret)
            def edits = service.edits()

            // Create a new edit to make changes to your listing.
            def editRequest = edits.insert(name, null /** no content */)
            def edit = editRequest.execute()
            def editId = edit.getId()

            // Upload new apk to developer console
            def apkPath = apkFile.toURI().getPath()
            def apkFile = new FileContent(MIME_TYPE_APK, new File(apkPath))
            def uploadRequest = edits.apks().upload(name, editId, apkFile)
            def apk = uploadRequest.execute()

            // Assign apk to alpha track.
            def apkVersionCodes = new ArrayList<>()
            apkVersionCodes.add(apk.getVersionCode())
            def updateTrackRequest = edits.tracks().update(name, editId, track, new Track().setVersionCodes(apkVersionCodes))
            updateTrackRequest.execute()

            // Update recent changes field in apk listing.
            if (changeList != null) {
                for (def lang : changeList.keySet()) {
                    def newApkListing = new ApkListing()
                    newApkListing.setRecentChanges(changeList.get(lang))
                    def updateRecentChangesRequest = edits.apklistings().update(name, editId, apk.getVersionCode(), lang, newApkListing)
                    updateRecentChangesRequest.execute()
                }
            }

            // Commit changes for edit.
            def commitRequest = edits.commit(name, editId)
            commitRequest.execute()

        } catch (IOException | URISyntaxException | GeneralSecurityException ex) {
            def msg = ex.getMessage()
            println(msg)
            setCancelText(MSG_UPLOAD_FAIL + "\n" + msg)

            def jsonIndex = msg.indexOf("{")
            if (jsonIndex != -1) {
                try {
                    def err = new JSONObject(msg.substring(jsonIndex))
                    if (!err.optString(ERR_DESC).empty) {
                        setCancelText(err.optString(ERR_DESC))
                    } else if (!err.optString(ERR_MSG).empty) {
                        setCancelText(err.getString(ERR_MSG))
                    }
                } catch (Exception ignore) {
                    println(ignore.toString())
                }
            }

        }
    }

    @Override
    void onSuccess() {
        if (getCancelText() != null) {
            Messages.showErrorDialog(getCancelText(), TITLE)
        } else {
            Messages.showInfoMessage(MSG_UPLOAD_SUCCESS, TITLE)
        }
    }

    @Override
    public void onCancel() {
        Messages.showInfoMessage(MSG_UPLOAD_FAIL, TITLE)
    }
}
