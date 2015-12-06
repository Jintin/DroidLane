#!/usr/bin/env groovy

package com.jintin.droidlane

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import com.jintin.droidlane.utils.AESUtils
import com.jintin.droidlane.utils.JSonUtils
import org.json.JSONArray
import org.json.JSONObject

class DroidLane extends AnAction {

    static def TITLE = "DroidLane"

    static def DROIDLANE_PATH = "/.droidlane/"
    static def SECRET_PATH = DROIDLANE_PATH + "secret/"
    static def RECENT_CHANGE = "/recentChange"
    static def HOMEDIR = System.getProperty("user.home")
    static def DATA_NAME = "/data.json"

    static
    def MSG_INSTALL_HINT = "please add data in .droidlane first, See https://github.com/Jintin/DroidLane for more information"
    static def MSG_SELECT_PROJECT = "select project to upload"
    static def MSG_SELECT_TRACK = "select track to upload"
    static def MSG_DECRYPT_KEY = "enter master password to decrypt secret key"
    static def MSG_ENCRYPT_KEY = "enter master password to encrypt secret key"
    static def MSG_SECRET = "please input secret key"
    static def MSG_WRONG_PASSWORD = "Password not correct"

    static def CLIENT_ID = "client_id"
    static def PACKAGE = "package"
    static def APK = "apk"
    static def TRACK = "track"
    static def TRACKS = ["alpha", "beta", "production"] as String[]

    public DroidLane() {
        // Set the menu item name.
        super("DroidLane")
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"))
    }

    @Override
    void actionPerformed(AnActionEvent anActionEvent) {
        println(Locale.US.toString())
        def project = anActionEvent.getData(PlatformDataKeys.PROJECT)
        println(project)
        def basePath = project.basePath + DROIDLANE_PATH

        def projectPath = getProject(basePath)
        if (projectPath.empty) {
            return
        }
        def obj = JSonUtils.fromFile(projectPath + DATA_NAME)
        if (obj == null) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return
        }
        def track = obj.optString(TRACK)
        if (track.empty) {
            track = TRACKS[Messages.showChooseDialog(MSG_SELECT_TRACK, TITLE, TRACKS, TRACKS[0], Messages.getInformationIcon())]
        }
        def client_id = obj.getString(CLIENT_ID)
        def secretPath = HOMEDIR + SECRET_PATH + client_id
        def secret = getSecret(secretPath, client_id)
        if (secret == null) {
            return
        }
        def pkgName = obj.getString(PACKAGE)
        def apkPath = obj.getString(APK)
        if (apkPath.startsWith("~" + File.separator)) {
            apkPath = HOMEDIR + apkPath.substring(1);
        }
        print(apkPath)
        def apkFile

        if (apkPath.startsWith("/")) {
            apkFile = new File(apkPath)
        } else {
            apkFile = new File(project.basePath, apkPath)
        }

        def changeList = getChangeList(projectPath + RECENT_CHANGE)
        def upload = new UploadTask(project, pkgName, secret, apkFile, track, changeList)
        ProgressManager.getInstance().run(upload)

    }

    static String getProject(String path) {
        def list = new File(path).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".")//ignore hidden file
            }
        })
        if (list == null) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return ""
        }
        def select = 0
        if (list.size() == 0) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            select = -1
        } else if (list.size() > 1) {
            select = Messages.showChooseDialog(MSG_SELECT_PROJECT, TITLE, list, list[0], Messages.getInformationIcon())
        }
        if (select != -1) {
            return path + list[select]
        } else {
            return ""
        }
    }

    static JSONObject getSecret(String path, String client_id) {
        def secretFile = new File(path)
        def secret
        if (secretFile.exists()) {
            def password = Messages.showPasswordDialog(MSG_DECRYPT_KEY, TITLE)
            if (password == null) {
                return null
            }
            secret = AESUtils.decrypt(secretFile.getText(), password)
            if (secret == null) {
                Messages.showErrorDialog(MSG_WRONG_PASSWORD, TITLE)
                return getSecret(path, client_id)
            }
        } else {
            if (!secretFile.getParentFile().exists()) {
                secretFile.getParentFile().mkdirs()
            }
            secretFile.createNewFile()
            secret = Messages.showPasswordDialog(MSG_SECRET, TITLE)
            if (secret == null) {
                return null
            }
            def password = Messages.showPasswordDialog(MSG_ENCRYPT_KEY, TITLE)
            if (password == null) {
                return null
            }
            secretFile.setText(AESUtils.encrypt(secret, password))
        }

        return new JSONObject().put("installed", new JSONObject()
                .put(CLIENT_ID, client_id)
                .put("client_secret", secret)
                .put("redirect_uris", new JSONArray())
                .put("auth_uri", "https://accounts.google.com/o/oauth2/auth")
                .put("token_uri", "https://accounts.google.com/o/oauth2/token"))
    }

    static Map<String, String> getChangeList(String path) {
        def map = new HashMap<>()
        def file = new File(path)
        if (file == null) {
            return null
        }
        for (def lang : file.listFiles()) {
            if (lang.getName().startsWith(".")) {
                continue
            }
            map.put(lang.getName(), lang.getText())
        }
        return map
    }


}