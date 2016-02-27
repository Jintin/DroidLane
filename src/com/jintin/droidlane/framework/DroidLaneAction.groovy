#!/usr/bin/env groovy

package com.jintin.droidlane.framework

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.jintin.droidlane.utils.AESUtils
import com.jintin.droidlane.utils.NotifyUtils
import org.json.JSONArray
import org.json.JSONObject

abstract class DroidLaneAction<T> extends AnAction {

    static def TITLE = "DroidLane"
    static def DROIDLANE_PATH = "/.droidlane/"
    static def HOMEDIR = System.getProperty("user.home")

    static
    def MSG_INSTALL_HINT = "please add data in .droidlane first, See https://github.com/Jintin/DroidLane for more information"
    static def MSG_SELECT_PROJECT = "select project to upload"
    static def MSG_MASTER_KEY = "enter master password to encrypt/decrypt secret key"
    static def MSG_SECRET = "please input secret key"
    static def MSG_WRONG_PASSWORD = "Password not correct"

    Project project

    DroidLaneAction(String str) {
        super(str)
    }

    @Override
    void actionPerformed(AnActionEvent anActionEvent) {

        project = anActionEvent.getData(PlatformDataKeys.PROJECT)
        def basePath = project.basePath + DROIDLANE_PATH

        def profile = getProfile(basePath)
        if (profile == null) {
            return
        }
        def data = getData(profile)
        if (data == null) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return
        }
        def password = Messages.showPasswordDialog(MSG_MASTER_KEY, TITLE)
        if (password == null) {
            return
        }
        generateTask(data, password)
    }

    abstract T getData(String path)

    abstract void generateTask(T data, String password)

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
        if (list.size() == 0) {
            Messages.showInfoMessage(MSG_INSTALL_HINT, TITLE)
            return null
        } else if (list.size() > 1) {
            def select = Messages.showChooseDialog(MSG_SELECT_PROJECT, TITLE, list, list[0], Messages.getInformationIcon())
            if (select != -1) {
                return path + list[select]
            } else {
                return null
            }
        } else {
            return path + list[0]
        }
    }

    static JSONObject getSecret(String client_id, String password) {
        def secretFile = new File(HOMEDIR + DROIDLANE_PATH + "secret/" + client_id)
        def secret
        if (secretFile.exists()) {
            if (password == null) {
                return null
            }
            secret = AESUtils.decrypt(secretFile.getText(), password)
            if (secret == null) {
                NotifyUtils.show(MSG_WRONG_PASSWORD, NotificationType.ERROR)
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

}