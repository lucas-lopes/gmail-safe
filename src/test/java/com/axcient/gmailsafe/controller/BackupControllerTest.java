package com.axcient.gmailsafe.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axcient.gmailsafe.controller.exception.StandardError;
import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.GmailService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BackupController.class)
@AutoConfigureMockMvc
class BackupControllerTest {

    private final String BACKUP_API = "/backups";

    @Autowired
    MockMvc mvc;

    @MockBean
    BackupService backupService;

    @MockBean
    GmailService gmailService;

    @Test
    @DisplayName("Should initiate a backup")
    void testInitiateBackup_shouldCreateWithSuccess() throws Exception {
        var backup = Backup.builder()
            .backupId("625df11878aad7513a41409b")
            .status(Status.IN_PROGRESS)
            .date(LocalDateTime.now())
            .build();

        BDDMockito.given(backupService.saveBackup()).willReturn(backup);
        BDDMockito.doNothing().when(gmailService).downloadAllEmailsFromGmail(backup.getBackupId());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(BACKUP_API)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("backupId").value("625df11878aad7513a41409b"));
    }

    @Test
    @DisplayName("Should throw Unauthorized to try initiate a backup")
    void testInitiateBackup_shouldThrowUnauthorizedException() throws Exception {
        var standardError = StandardError.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .message("Was not possible finish the authentication on Gmail API")
            .error(HttpStatus.UNAUTHORIZED.name())
            .build();

        BDDMockito.given(backupService.saveBackup()).willReturn(null);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(BACKUP_API)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("status").value(standardError.getStatus()))
            .andExpect(jsonPath("message").value(standardError.getMessage()))
            .andExpect(jsonPath("error").value(standardError.getError()));
    }

}
