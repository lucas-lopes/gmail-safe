package com.axcient.gmailsafe.service;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Email;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface BackupService {

    Backup saveBackup();

    List<Backup> findAllBackups();

    Optional<StreamingResponseBody> exportBackupToFile(String backupId, String label, ServletOutputStream outputStream);

    void publishEmail(Email email);

    void saveEmail(Email email);

    Backup updateBackupStatus(String backupId, String status);

}

