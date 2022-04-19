package com.axcient.gmailsafe.controller;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.GmailService;
import com.axcient.gmailsafe.service.exception.FileException;
import com.axcient.gmailsafe.service.exception.UnauthorizedException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/backups")
@AllArgsConstructor
public class BackupController {

    private final GmailService gmailService;
    private final BackupService backupService;

    @PostMapping
    public ResponseEntity<Backup> initiateBackup() {
        var backup = backupService.saveBackup();

        if (Optional.ofNullable(backup).isPresent()) {
            gmailService.downloadAllEmailsFromGmail(backup.getBackupId());
            return ResponseEntity.ok().body(Backup.builder().backupId(backup.getBackupId()).build());
        }

        throw new UnauthorizedException("Was not possible finish the authentication on Gmail API");
    }

    @GetMapping
    public ResponseEntity<List<Backup>> findAllBackups() {
        var backups = backupService.findAllBackups();
        return ResponseEntity.ok().body(backups);
    }

    @GetMapping(value = "/exports/{backupId}/{label}")
    public ResponseEntity<StreamingResponseBody> extractBackupToFile(@PathVariable(value = "backupId") String backupId,
                                                                     @PathVariable(value = "label") String label,
                                                                     HttpServletResponse response) throws Exception {
        var emailsCompressed =
            backupService.extractBackupToFile(backupId, label, response.getOutputStream());

        buildResponse(response, backupId);

        return ResponseEntity.ok(emailsCompressed.orElseThrow(
            () -> new FileException("Error to try generate ZIP file")));
    }

    @GetMapping(path = "/exports/{backupId}")
    public ResponseEntity<StreamingResponseBody> extractBackupToZipFile(@PathVariable(name = "backupId") String backupId,
                                                                        HttpServletResponse response) throws IOException {
        var emailsCompressed =
            backupService.extractBackupToFile(backupId, null, response.getOutputStream());

        buildResponse(response, backupId);

        return ResponseEntity.ok(emailsCompressed.orElseThrow(
            () -> new FileException("Error to try generate ZIP file")));
    }

    private void buildResponse(HttpServletResponse response, String backupId) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + backupId + ".zip");
    }

}
