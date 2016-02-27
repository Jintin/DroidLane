package com.jintin.droidlane

import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.Listing
import com.intellij.openapi.project.Project
import com.jintin.droidlane.framework.DroidLaneTask
import org.json.JSONObject

//https://github.com/googlesamples/android-play-publisher-api
class ListingTask extends DroidLaneTask<HashMap<String, ListingObj>> {

    ListingTask(Project project, String pkgName, HashMap<String, ListingObj> data, JSONObject secret) {
        super(project, data, secret)
        setPkgName(pkgName)
    }

    @Override
    void upload(AndroidPublisher.Edits edits, String editId) {
        // Update listing
        for (def lang : obj.keySet()) {
            def newListing = new Listing()
            def listing = obj.get(lang)
            newListing.setTitle(listing.title)
                    .setFullDescription(listing.fullDesc)
                    .setShortDescription(listing.shortDesc)
                    .setVideo(listing.video)

            def updateUSListingsRequest = edits.listings()
                    .update(pkgName, editId, lang, newListing)
            updateUSListingsRequest.execute()
        }
    }
}
