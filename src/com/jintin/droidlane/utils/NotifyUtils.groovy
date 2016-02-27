package com.jintin.droidlane.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager

class NotifyUtils {

    static def TITLE = "DroidLane"

    static void show(String message, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notification notification = NotificationGroup.balloonGroup(TITLE).createNotification(TITLE, message, type, null)
                Notifications.Bus.notify(notification)
            }
        })
    }
}
