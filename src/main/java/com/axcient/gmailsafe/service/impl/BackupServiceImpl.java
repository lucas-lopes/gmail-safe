package com.axcient.gmailsafe.service.impl;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.repository.BackupRepository;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.exception.AcceptedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
@AllArgsConstructor
public class BackupServiceImpl implements BackupService {

    private static final String BACKUP_IN_PROGRESS = "Your first backup is in progress. Please, wait some minutes";

    private final BackupRepository backupRepository;

    @Override
    public Backup saveBackup() {
        if (backupRepository.findAll().stream().anyMatch(bkp -> Status.IN_PROGRESS.equals(bkp.getStatus()))) {
            throw new AcceptedException(BACKUP_IN_PROGRESS);
        }

        return backupRepository.save(
            Backup.builder()
                .status(Status.IN_PROGRESS)
                .date(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public List<Backup> findAllBackups() {
        return backupRepository.findAll();
    }

    @Override
    public Optional<StreamingResponseBody> extractBackupToFile(String backupId, String label, ServletOutputStream outputStream) {
        return Optional.empty();
    }

}
