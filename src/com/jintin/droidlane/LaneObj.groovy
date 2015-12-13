package com.jintin.droidlane

import com.intellij.openapi.project.Project
import org.json.JSONArray
import org.json.JSONObject

class LaneObj {
    def client_id
    def pkgName
    def apk
    def track

    LaneObj(Project project, JSONObject object) {
        client_id = object.getString("client_id")
        pkgName = object.getString("package")
        track = object.optString("track")

        def apkPath = object.getString("apk")
        if (apkPath.startsWith("~" + File.separator)) {
            apkPath = DroidLane.HOMEDIR + apkPath.substring(1);
        }
        if (apkPath.startsWith("/")) {
            apk = new File(apkPath)
        } else {
            apk = new File(project.basePath, apkPath)
        }
    }

    static ArrayList<LaneObj> fromFile(Project project, String path) {
        try {
            def list = new ArrayList<LaneObj>()
            def text = new File(path).getText()
            if (text.startsWith("[")) { // array
                def array = new JSONArray(text)
                for (int i = 0; i < array.length(); i++) {
                    def lane = new LaneObj(project, array.getJSONObject(i))
                    list.add(lane)
                }
            } else { // obj
                def lane = new LaneObj(project, new JSONObject(text))
                list.add(lane)
            }
            return list
        } catch (Exception ignore) {
            return null
        }
    }
}
