package com.axcient.gmailsafe.service.impl;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.service.BackupService;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
public class BackupServiceImpl implements BackupService {

    @Override
    public Backup saveBackup() {
        return null;
    }

    @Override
    public List<Backup> findAllBackups() {
        return null;
    }

    @Override
    public Optional<StreamingResponseBody> extractBackupToFile(String backupId, String label, ServletOutputStream outputStream) {
        return Optional.empty();
    }

}
