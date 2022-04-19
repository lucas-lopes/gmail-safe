package com.axcient.gmailsafe.controller;

import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.exception.FileException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/exports")
@AllArgsConstructor
public class EmailController {

    private final BackupService backupService;

    @GetMapping(value = "/{backupId}/{label}")
    public ResponseEntity<StreamingResponseBody> extractBackupToFile(@PathVariable(value = "backupId") String backupId,
                                                                     @PathVariable(value = "label") String label,
                                                                     HttpServletResponse response) throws Exception {
        var emailsCompressed =
            backupService.exportBackupToFile(backupId, label, response.getOutputStream());

        buildResponse(response, backupId);

        return ResponseEntity.ok(emailsCompressed.orElseThrow(
            () -> new FileException("Error to try generate ZIP file")));
    }

    @GetMapping(path = "/{backupId}")
    public ResponseEntity<StreamingResponseBody> extractBackupToZipFile(@PathVariable(name = "backupId") String backupId,
                                                                        HttpServletResponse response) throws IOException {
        var emailsCompressed =
            backupService.exportBackupToFile(backupId, null, response.getOutputStream());

        buildResponse(response, backupId);

        return ResponseEntity.ok(emailsCompressed.orElseThrow(
            () -> new FileException("Error to try generate ZIP file")));
    }

    private void buildResponse(HttpServletResponse response, String backupId) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + backupId + ".zip");
    }

}
