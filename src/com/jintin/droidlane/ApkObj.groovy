package com.jintin.droidlane

import com.intellij.openapi.project.Project
import org.json.JSONArray
import org.json.JSONObject

class ApkObj {
    String client_id
    String pkgName
    File apk
    String track
    static Map<String, String> changeList

    ApkObj(String path, JSONObject object) {
        client_id = object.getString("client_id")
        pkgName = object.getString("package")
        track = object.optString("track")

        def apkPath = object.getString("apk")
        if (apkPath.startsWith("~" + File.separator)) {
            apkPath = ApkAction.HOMEDIR + apkPath.substring(1);
        }
        if (apkPath.startsWith("/")) {
            apk = new File(apkPath)
        } else {
            apk = new File(path, apkPath)
        }
    }

    static ArrayList<ApkObj> fromFile(Project project,String path) {
        changeList = getChangeList(path + "/recentChange")

        try {
            def list = new ArrayList<ApkObj>()
            def text = new File(path + "/data.json").getText()
            if (text.startsWith("[")) { // array
                def array = new JSONArray(text)
                for (int i = 0; i < array.length(); i++) {
                    def lane = new ApkObj(project.basePath, array.getJSONObject(i))
                    list.add(lane)
                }
            } else { // obj
                def lane = new ApkObj(project.basePath, new JSONObject(text))
                list.add(lane)
            }
            return list
        } catch (Exception ignore) {
            return null
        }
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
