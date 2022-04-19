package com.axcient.gmailsafe.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.axcient.gmailsafe.controller.exception.StandardError;
import com.axcient.gmailsafe.service.BackupService;
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
@WebMvcTest(controllers = EmailController.class)
@AutoConfigureMockMvc
class EmailControllerTest {

    private final String EMAIL_API = "/exports";

    @Autowired
    MockMvc mvc;

    @MockBean
    BackupService backupService;

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
            .get(EMAIL_API + "/{backupId}/{label}", "625df11878aad7513a414091", "IMPORTANT")
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
            .get(EMAIL_API + "/{backupId}/{label}", "625df11878aad7513a414093", "SENT")
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
            .get(EMAIL_API + "/{backupId}", "625df11878aad7513a414091")
            .contentType(MediaType.valueOf("application/zip"));

        mvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content()
                .contentType(MediaType.valueOf("application/zip")));
    }

}
