package com.axcient.gmailsafe.service.impl;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Email;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.repository.BackupRepository;
import com.axcient.gmailsafe.repository.EmailRepository;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.exception.AcceptedException;
import com.axcient.gmailsafe.service.exception.BadRequestException;
import com.axcient.gmailsafe.service.exception.ObjectNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@Service
@AllArgsConstructor
public class BackupServiceImpl implements BackupService {

    private static final String BACKUP_IN_PROGRESS = "Your first backup is in progress. Please, wait some minutes";
    private static final String BACKUP_ID_NOT_INFORMED = "The backupId is required to extract your backup";
    private static final String OBJECT_NOT_FOUND = "The backupId %s informed was not found. Please, check this information";
    private static final String EXTRACT_BACKUP = "Your backup does not finish yet. Please, wait some minutes to extract your backup";

    private final BackupRepository backupRepository;
    private final EmailRepository emailRepository;

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
        if (Optional.ofNullable(backupId).isPresent()) {
            log.info("[extractBackupInZipFile] Start generating zip file: {}.zip", backupId);
            var backup = backupRepository.findById(backupId);

            if (backup.isPresent()) {
                if (Status.OK.equals(backup.get().getStatus())) {

                }
                throw new AcceptedException(EXTRACT_BACKUP);
            }
            throw new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND, backupId));
        }
        throw new BadRequestException(BACKUP_ID_NOT_INFORMED);
    }

}
