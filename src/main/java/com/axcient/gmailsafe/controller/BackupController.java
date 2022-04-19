package com.axcient.gmailsafe.controller;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.GmailService;
import com.axcient.gmailsafe.service.exception.UnauthorizedException;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
