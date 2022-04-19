package com.axcient.gmailsafe.service.impl;

import com.axcient.gmailsafe.entity.Email;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.GmailService;
import com.axcient.gmailsafe.service.exception.BadGatewayException;
import com.axcient.gmailsafe.service.exception.FileException;
import com.axcient.gmailsafe.service.exception.UnauthorizedException;
import com.axcient.gmailsafe.util.Constants;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Builder;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class GmailServiceImpl implements GmailService {

    private final BackupService backupService;

    @Override
    @Async("asyncExecutor")
    public void downloadAllEmailsFromGmail(String backupId) {
        if (Optional.ofNullable(backupId).isPresent()) {
            log.info("[downloadAllEmailsFromGmail] start downloading from Gmail - BackupId: {}", backupId);
            String pageToken = null;

            do {
                var response = listEmails(pageToken, backupId);
                // Uncomment the line below if you'd like to do the complete backup of the email
                //pageToken = response.getNextPageToken();

                response.getMessages().forEach(message -> {
                    var emailFromGmail = getEmailById(message.getId(), backupId);

                    if (emailFromGmail.isPresent()) {
                        var email = adaptMessageFromGmailToEmail(emailFromGmail.get(), backupId);
                        backupService.publishEmail(email);
                    }
                });
            } while (Optional.ofNullable(pageToken).isPresent());

            backupService.updateBackupStatus(backupId, Status.OK.getGetKey());
            log.info("[downloadAllEmailsFromGmail] end downloading from Gmail - BackupId: {}", backupId);
        }
    }

    private ListMessagesResponse listEmails(String pageToken, String backupId) {
        try {
            var gmailService = getGmailService(backupId)
                .users()
                .messages()
                .list(Constants.USER);

            return Optional.ofNullable(pageToken).isPresent()
                ? gmailService.setPageToken(pageToken).execute()
                : gmailService.execute();
        } catch (IOException e) {
            backupService.updateBackupStatus(backupId, Status.FAILED.getGetKey());
            throw new BadGatewayException(e.getMessage());
        }
    }

    private Optional<Message> getEmailById(String id, String backupId) {
        if (Optional.ofNullable(id).isPresent()) {
            try {
                Message message = getGmailService(backupId)
                    .users()
                    .messages()
                    .get(Constants.USER, id)
                    .setFormat("raw")
                    .execute();

                if (Optional.ofNullable(message.getRaw()).isPresent()) {
                    return Optional.of(message);
                }
            } catch (IOException e) {
                backupService.updateBackupStatus(backupId, Status.FAILED.getGetKey());
                throw new BadGatewayException(e.getMessage());
            }
        }
        return Optional.empty();
    }

    private Gmail getGmailService(String backupId) {
        try {
            final var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            return new Builder(httpTransport, Constants.JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
        } catch (IOException | GeneralSecurityException e) {
            backupService.updateBackupStatus(backupId, Status.FAILED.getGetKey());
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
        InputStream credentials = GmailServiceImpl.class.getResourceAsStream(Constants.CREDENTIALS_FILE_PATH);
        if (credentials == null) {
            throw new FileException("Error loading client secret file: " + Constants.CREDENTIALS_FILE_PATH);
        }
        return authorize(credentials, httpTransport);
    }

    private Credential authorize(final InputStream credentials, final NetHttpTransport httpTransport) throws IOException {
        var clientSecrets = GoogleClientSecrets.load(Constants.JSON_FACTORY, new InputStreamReader(credentials));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, Constants.JSON_FACTORY, clientSecrets, Constants.SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new File(Constants.TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(Constants.REDIRECT_URL_PORT).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(Constants.USER);
    }

    private Email adaptMessageFromGmailToEmail(Message message, String backgroundId) {
        return Email.builder()
            .backupId(backgroundId)
            .historyId(message.getHistoryId())
            .id(message.getId())
            .internalDate(message.getInternalDate())
            .labelIds(message.getLabelIds())
            .raw(message.getRaw())
            .sizeEstimate(message.getSizeEstimate())
            .snippet(message.getSnippet())
            .threadId(message.getThreadId())
            .build();
    }

}