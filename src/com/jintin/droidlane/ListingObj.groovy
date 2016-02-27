package com.jintin.droidlane

class ListingObj {
    String lang
    String title
    String fullDesc
    String shortDesc
    String video

    ListingObj(String path, String lang) {
        this.lang = lang
        if (new File(path + "/title/" + lang).exists()) {
            title = new File(path + "/title/" + lang).text
        }
        if (new File(path + "/fullDesc/" + lang).exists()) {
            fullDesc = new File(path + "/fullDesc/" + lang).text
        }
        if (new File(path + "/shortDesc/" + lang).exists()) {
            shortDesc = new File(path + "/shortDesc/" + lang).text
        }
        if (new File(path + "/video/" + lang).exists()) {
            video = new File(path + "/video/" + lang).text
        }
    }

    static HashMap<String, ListingObj> fromFile(String path) {
        try {
            def data = new HashMap<String, ListingObj>()
            def titles = new File(path + "/title").list()
            for (def title : titles) {
                data.put(title, new ListingObj(path, title))
            }
            if (data.size() == 0) {
                return null
            }
            return data
        } catch (Exception ignore) {
            return null
        }
    }

}
