#!/usr/bin/env groovy

package com.jintin.droidlane

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import com.jintin.droidlane.utils.AESUtils
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
    static def MSG_MASTER_KEY = "enter master password to encrypt/decrypt secret key"
    static def MSG_SECRET = "please input secret key"
    static def MSG_WRONG_PASSWORD = "Password not correct"

    static def TRACKS = ["alpha", "beta", "production"] as String[]

    public DroidLane() {
        // Set the menu item name.
        super("DroidLane")
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"))
    }

    @Override
    void actionPerformed(AnActionEvent anActionEvent) {
        def project = anActionEvent.getData(PlatformDataKeys.PROJECT)
        def basePath = project.basePath + DROIDLANE_PATH

        def profilePath = getProfile(basePath)
        if (profilePath == null) {
            return
        }
        def list = LaneObj.fromFile(project, profilePath + DATA_NAME)
        if (list == null) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return
        }
        def password = Messages.showPasswordDialog(MSG_MASTER_KEY, TITLE)
        if (password == null) {
            return
        }
        for (def obj : list) {
            if (obj.track.empty) {
                obj.track = TRACKS[Messages.showChooseDialog(MSG_SELECT_TRACK, TITLE, TRACKS, TRACKS[0], Messages.getInformationIcon())]
            }
            def secret = getSecret(HOMEDIR + SECRET_PATH, obj.client_id, password)
            if (secret == null) {
                return
            }

            def changeList = getChangeList(profilePath + RECENT_CHANGE)
            def upload = new UploadTask(project, obj, secret, changeList)
            ProgressManager.getInstance().run(upload)
        }
    }

    static String getProfile(String path) {
        def list = new File(path).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".")//ignore hidden file
            }
        })
        if (list == null) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return null
        }
        def select = 0
        if (list.size() == 0) {
            select = -1
        } else if (list.size() > 1) {
            select = Messages.showChooseDialog(MSG_SELECT_PROJECT, TITLE, list, list[0], Messages.getInformationIcon())
        }
        if (select != -1) {
            return path + list[select]
        } else {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return null
        }
    }

    static JSONObject getSecret(String path, String client_id, String password) {
        def secretFile = new File(path + client_id)
        def secret
        if (secretFile.exists()) {
            if (password == null) {
                return null
            }
            secret = AESUtils.decrypt(secretFile.getText(), password)
            if (secret == null) {
                Messages.showErrorDialog(MSG_WRONG_PASSWORD, TITLE)
                return null
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
            secretFile.setText(AESUtils.encrypt(secret, password))
        }

        return new JSONObject().put("installed", new JSONObject()
                .put("client_id", client_id)
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