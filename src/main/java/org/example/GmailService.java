package org.example;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class GmailService {

    private static final String APPLICATION_NAME = "Gmail Unsubscriber";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\ssbod\\IdeaProjects\\Gmail-Unsubscribe-App\\credentials.json";


    public static Gmail getGmailService() throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        ).setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
        ).setApplicationName(APPLICATION_NAME).build();
    }
}