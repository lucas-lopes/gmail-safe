package com.axcient.gmailsafe.util;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String FOLDER_DEFAULT = "output/";

    public static final String USER = "me";
    public static final Integer REDIRECT_URL_PORT = 9002;
    public static final String APPLICATION_NAME = "Gmail Safe";
    public static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);

    public static final String EXCHANGE = "gmail-safe-exchange";
    public static final String ROUTING_KEY = "gmail-safe-routing-key";

}
