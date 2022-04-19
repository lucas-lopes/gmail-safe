package com.axcient.gmailsafe.service;

import com.axcient.gmailsafe.entity.Backup;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface BackupService {

    Backup saveBackup();

    List<Backup> findAllBackups();

    Optional<StreamingResponseBody> exportBackupToFile(String backupId, String label, ServletOutputStream outputStream);

}

