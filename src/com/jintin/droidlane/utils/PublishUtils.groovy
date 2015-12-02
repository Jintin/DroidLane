#!/usr/bin/env groovy

package com.jintin.droidlane.utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import org.json.JSONObject

import java.security.GeneralSecurityException

// source from https://github.com/googlesamples/android-play-publisher-api/tree/master/v2/java
class PublishUtils {

    /** Global instance of the JSON factory. */
    private static final def JSON_FACTORY = JacksonFactory.getDefaultInstance()

    /** Global instance of the HTTP transport. */
    private static def HTTP_TRANSPORT

    /** Installed application user ID. */
    private static final def INST_APP_USER_ID = "user"

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static Credential authorizeWithInstalledApplication(JSONObject object) throws IOException {

        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new ByteArrayInputStream(object.toString().getBytes())))
        // Ensure file has been filled out.
        checkClientSecretsFile(clientSecrets)

        File store = new File(System.getProperty("user.home") + "/.droidlane/store/" + object.getJSONObject("installed").getString("client_id"))
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(store)

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER)).setDataStoreFactory(dataStoreFactory).build()
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(INST_APP_USER_ID)
    }

    /**
     * Ensure the client secrets file has been filled out.
     *
     * @param clientSecrets the GoogleClientSecrets containing data from the
     *            file
     */
    private static void checkClientSecretsFile(GoogleClientSecrets clientSecrets) {
        if (clientSecrets.getDetails().getClientId().startsWith("[[INSERT") || clientSecrets.getDetails().getClientSecret().startsWith("[[INSERT")) {
            System.exit(1)
        }
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName the name of the application: com.example.app
     * @param serviceAccountEmail the Service Account Email (empty if using
     *            installed application)
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    static AndroidPublisher init(JSONObject secret) throws IOException, GeneralSecurityException {

        // Authorization.
        newTrustedTransport()
        Credential credential = authorizeWithInstalledApplication(secret)

        // Set up and return API client.
        return new AndroidPublisher.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build()
    }

    private static void newTrustedTransport() throws GeneralSecurityException, IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        }
    }
}
