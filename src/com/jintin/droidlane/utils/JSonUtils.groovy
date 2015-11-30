package com.jintin.droidlane.utils

import org.json.JSONObject

class JSonUtils {

    static JSONObject fromFile(String path) {
        try {
            return new JSONObject((new File(path).getText()))
        } catch (Exception ignore) {
            return null
        }
    }
}
