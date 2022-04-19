package com.axcient.gmailsafe.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axcient.gmailsafe.controller.exception.StandardError;
import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.GmailService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
            .status(Status.IN_PROGRESS.getGetKey())
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
            .andExpect(jsonPath("error").value(standardError.getError()))
            .andExpect(jsonPath("timestamp", is(notNullValue())));
    }

    @Test
    @DisplayName("Should return a list of backups initiated")
    void testFindAllBackups_shouldReturnAllBackupInitiated() throws Exception {
        var backupInProgress = Backup.builder()
            .backupId("625df11878aad7513a414091")
            .status(Status.IN_PROGRESS.getGetKey())
            .date(LocalDateTime.now())
            .build();

        var backupOk = Backup.builder()
            .backupId("625df11878aad7513a414092")
            .status(Status.OK.getGetKey())
            .date(LocalDateTime.now())
            .build();

        List<Backup> backups = new ArrayList<>(Arrays.asList(backupInProgress, backupOk));

        BDDMockito.given(backupService.findAllBackups()).willReturn(backups);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(BACKUP_API)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("[*].backupId", containsInAnyOrder("625df11878aad7513a414091", "625df11878aad7513a414092")))
            .andExpect(jsonPath("[*].status", containsInAnyOrder(Status.IN_PROGRESS.getGetKey(), Status.OK.getGetKey())))
            .andExpect(jsonPath("[*].date", is(notNullValue())));
    }

    @Test
    @Disabled
    @DisplayName("Should return a file compressed filtering by label")
    void testListEmailsByLabel_shouldReturnAFileCompressed() throws Exception {
        String label = "IMPORTANT";
        String backupId = "625df11878aad7513a414091";

        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        var outputStream = httpServletResponse.getOutputStream();

        StreamingResponseBody responseBody = BDDMockito.mock(StreamingResponseBody.class);
        BDDMockito.given(backupService.exportBackupToFile(backupId, label, outputStream)).willReturn(Optional.of(responseBody));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(BACKUP_API + "/exports/{backupId}/{label}", "625df11878aad7513a414091", "IMPORTANT")
            .contentType(MediaType.APPLICATION_JSON_VALUE);

        mvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content()
                .contentType(MediaType.valueOf("application/zip")));
    }

    @Test
    @Disabled
    @DisplayName("Should throw FileException to try extract a backup")
    void testExtractBackupToFile_shouldThrowFileException() throws Exception {
        String label = "SENT";
        String backupId = "625df11878aad7513a414093";

        var standardError = StandardError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Error to try generate ZIP file")
            .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
            .build();

        BDDMockito.given(backupService.exportBackupToFile(backupId, label, null)).willReturn(null);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(BACKUP_API + "/exports/{backupId}/{label}", "625df11878aad7513a414093", "SENT")
            .contentType(MediaType.valueOf("application/zip"));

        mvc.perform(request)
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("status").value(standardError.getStatus()))
            .andExpect(jsonPath("message").value(standardError.getMessage()))
            .andExpect(jsonPath("error").value(standardError.getError()))
            .andExpect(jsonPath("timestamp", is(notNullValue())));
    }

    @Test
    @Disabled
    @DisplayName("Should return a file compressed filtering by label")
    void testExtractBackupToZipFile_shouldReturnAFileCompressed() throws Exception {
        String backupId = "625df11878aad7513a414091";

        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        var outputStream = httpServletResponse.getOutputStream();

        StreamingResponseBody responseBody = BDDMockito.mock(StreamingResponseBody.class);
        BDDMockito.given(backupService.exportBackupToFile(backupId, null, outputStream)).willReturn(Optional.of(responseBody));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(BACKUP_API + "/exports/{backupId}", "625df11878aad7513a414091")
            .contentType(MediaType.valueOf("application/zip"));

        mvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content()
                .contentType(MediaType.valueOf("application/zip")));
    }

}
