package com.axcient.gmailsafe.service.impl;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Email;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.producer.BackupProducer;
import com.axcient.gmailsafe.repository.BackupRepository;
import com.axcient.gmailsafe.repository.EmailRepository;
import com.axcient.gmailsafe.service.BackupService;
import com.axcient.gmailsafe.service.exception.AcceptedException;
import com.axcient.gmailsafe.service.exception.BadRequestException;
import com.axcient.gmailsafe.service.exception.FileException;
import com.axcient.gmailsafe.service.exception.ObjectNotFoundException;
import com.axcient.gmailsafe.util.Constants;
import com.axcient.gmailsafe.util.FileUtil;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final BackupProducer backupProducer;

    @Override
    public Backup saveBackup() {
        if (backupRepository.findAll().stream().anyMatch(bkp -> Status.IN_PROGRESS.getGetKey().equals(bkp.getStatus()))) {
            throw new AcceptedException(BACKUP_IN_PROGRESS);
        }

        return backupRepository.save(
            Backup.builder()
                .status(Status.IN_PROGRESS.getGetKey())
                .date(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public List<Backup> findAllBackups() {
        return backupRepository.findAll();
    }

    @Override
    public Optional<StreamingResponseBody> exportBackupToFile(String backupId, String label, ServletOutputStream outputStream) {
        if (Optional.ofNullable(backupId).isPresent()) {
            log.info("[extractBackupInZipFile] Start generating zip file: {}.zip", backupId);
            var backup = backupRepository.findById(backupId);

            if (backup.isPresent()) {
                if (Status.OK.getGetKey().equals(backup.get().getStatus())) {
                    List<Email> emails;

                    if (Optional.ofNullable(label).isPresent()) {
                        emails = emailRepository.findByLabelIdsAndBackupId(label.toUpperCase(), backupId);
                    } else {
                        emails = emailRepository.findByBackupId(backupId);
                    }

                    var responseBody = generateCompressedFile(emails, backupId, outputStream)
                        .orElseThrow(() -> new FileException("Error to try generate ZIP file"));

                    return Optional.ofNullable(responseBody);
                }
                throw new AcceptedException(EXTRACT_BACKUP);
            }
            throw new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND, backupId));
        }
        throw new BadRequestException(BACKUP_ID_NOT_INFORMED);
    }

    @Override
    public void publishEmail(Email email) {
        backupProducer.publisher(email, Constants.EXCHANGE , Constants.ROUTING_KEY);
        log.info("Producing emailId {} to queue", email.getId());
    }

    @Override
    public void saveEmail(Email email) {
        if (Optional.ofNullable(email).isEmpty()) {
            throw new BadRequestException("Was not possible save the email");
        }
        emailRepository.save(email);
    }

    private Optional<StreamingResponseBody> generateCompressedFile(List<Email> emails, String backupId, ServletOutputStream outputStream) {
        List<String> filesToBeZipped = new ArrayList<>();

        if (!emails.isEmpty()) {
            emails.forEach(email -> {
                try {
                    FileUtil.generateFolderStructure(backupId, email.getId(), email.toString());
                    filesToBeZipped.add(email.getId() + ".txt");
                } catch (IOException e) {
                    throw new FileException(e.getMessage());
                }
            });
            return Optional.of(FileUtil.generateZipFile(emails.get(0).getBackupId(), filesToBeZipped, outputStream));
        }
        throw new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND, backupId));
    }

    @Override
    public Backup updateBackupStatus(String backupId, String status) {
        var backupOptional = backupRepository.findById(backupId);

        if (backupOptional.isPresent()) {
            Backup backupSaved = backupOptional.get();
            log.info("[updateBackupStatus] updating backup status: {}", backupSaved.getBackupId());
            backupSaved.setStatus(status);
            log.info("[updateBackupStatus] updated backup status to {}: {}", backupSaved.getStatus(), backupSaved.getBackupId());
            return backupRepository.save(backupSaved);
        }
        throw new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND, backupId));
    }

}
