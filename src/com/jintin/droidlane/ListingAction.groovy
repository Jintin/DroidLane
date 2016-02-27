#!/usr/bin/env groovy

package com.jintin.droidlane

import com.intellij.openapi.progress.ProgressManager
import com.jintin.droidlane.framework.DroidLaneAction

class ListingAction extends DroidLaneAction<ArrayList<HashMap<String, ListingObj>>> {

    ArrayList<ApkObj> apkList

    ListingAction() {
        super("DroidLane Listing")
    }

    @Override
    ArrayList<HashMap<String, ListingObj>> getData(String path) {
        ArrayList<ApkObj> list = ApkObj.fromFile(project, path)
        if (list.size() == 0) {
            return null
        }
        ArrayList<HashMap<String, ListingObj>> data = new ArrayList<>()
        apkList = new ArrayList<>()
        if (list.size() == 1) {
            apkList.add(list.get(0))
            def listing = ListingObj.fromFile(path)
            if (listing == null) {
                return null
            }
            data.add(listing)
        } else {
            for (int i = 0; i < list.size(); i++) {
                def apkObj = list.get(i)
                apkList.add(apkObj)
                def listing = ListingObj.fromFile(path + "/" + apkObj.pkgName)
                if (listing == null) {
                    return null
                }
                data.add(listing)
            }
        }
        return data
    }

    @Override
    void generateTask(ArrayList<HashMap<String, ListingObj>> datas, String password) {
        for (int i = 0; i < datas.size(); i++) {
            def apkObj = apkList.get(i)
            def data = datas.get(i)
            def secret = getSecret(apkObj.client_id, password)
            def task = new ListingTask(project, apkObj.pkgName, data, secret)
            ProgressManager.getInstance().run(task)
        }
    }
}