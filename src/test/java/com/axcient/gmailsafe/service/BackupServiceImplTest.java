package com.axcient.gmailsafe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Email;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.producer.BackupProducer;
import com.axcient.gmailsafe.repository.BackupRepository;
import com.axcient.gmailsafe.repository.EmailRepository;
import com.axcient.gmailsafe.service.exception.AcceptedException;
import com.axcient.gmailsafe.service.exception.BadRequestException;
import com.axcient.gmailsafe.service.exception.ObjectNotFoundException;
import com.axcient.gmailsafe.service.impl.BackupServiceImpl;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class BackupServiceImplTest {

    BackupService backupService;

    @MockBean
    BackupRepository backupRepository;

    @MockBean
    EmailRepository emailRepository;

    @MockBean
    BackupProducer backupProducer;

    @BeforeEach
    public void setup() {
        this.backupService = new BackupServiceImpl(backupRepository, emailRepository, backupProducer);
    }

    @Test
    @DisplayName("Should save a new hero with success")
    void itShouldSaveNewBackup() {
        var backupInProgress = Backup.builder()
            .backupId("625df11878aad7513a414091")
            .status(Status.IN_PROGRESS.getGetKey())
            .date(LocalDateTime.now())
            .build();

        when(backupRepository.findAll()).thenReturn(List.of());
        when(backupRepository.save(Mockito.any(Backup.class))).thenReturn(backupInProgress);

        var backup = backupService.saveBackup();

        assertThat(backup).isNotNull();
        assertThat(backup.getBackupId()).isEqualTo("625df11878aad7513a414091");
        assertThat(backup.getStatus()).isEqualTo(Status.IN_PROGRESS.getGetKey());
        assertThat(backup.getDate()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an AcceptedException")
    void itShouldThrowAcceptedException() {
        var backupInProgress = Backup.builder()
            .backupId("625df11878aad7513a414091")
            .status(Status.IN_PROGRESS.getGetKey())
            .date(LocalDateTime.now())
            .build();

        when(backupRepository.findAll()).thenReturn(Collections.singletonList(backupInProgress));

        Throwable exception = catchThrowable(() -> backupService.saveBackup());

        assertThat(exception)
            .isInstanceOf(AcceptedException.class)
            .hasMessage("Your first backup is in progress. Please, wait some minutes");

        verify(backupRepository, Mockito.never()).save(backupInProgress);
    }

    @Test
    @DisplayName("Should return a list of backup initiated and concluded")
    void itShouldReturnAList() {
        var backupInProgress = Backup.builder()
            .backupId("625df11878aad7513a414091")
            .status(Status.IN_PROGRESS.getGetKey())
            .date(LocalDateTime.now())
            .build();

        var backupOk = Backup.builder()
            .backupId("625df11878aad7513a414092")
            .status(Status.FAILED.getGetKey())
            .date(LocalDateTime.now())
            .build();

        List<Backup> backups = new ArrayList<>(Arrays.asList(backupInProgress, backupOk));

        when(backupRepository.findAll()).thenReturn(backups);

        var backupsSaved = backupService.findAllBackups();

        assertThat(backupsSaved).hasSize(2);

        for (int i = 0; i < backupsSaved.size(); i++) {
            assertThat(backupsSaved.get(i).getBackupId()).isEqualTo(backups.get(i).getBackupId());
            assertThat(backupsSaved.get(i).getStatus()).isEqualTo(backups.get(i).getStatus());
            assertThat(backupsSaved.get(i).getDate()).isEqualTo(backups.get(i).getDate());
        }
    }

    @Test
    @DisplayName("Should throw BadRequestException to try extract backup")
    void itShouldThrowBadRequestExceptionInExtractBackupFileByLabel() {
        Throwable exception =
            catchThrowable(() -> backupService.exportBackupToFile(null, null, null));

        assertThat(exception)
            .isInstanceOf(BadRequestException.class)
            .hasMessage("The backupId is required to extract your backup");
    }

    @Test
    @DisplayName("Should throw ObjectNotFoundException to try extract backup")
    void itShouldThrowObjectNotFoundExceptionInExtractBackupFileByLabel() {
        String backupId = "625df11878aad7513a414092";
        when(backupRepository.findById(backupId)).thenReturn(Optional.empty());

        Throwable exception =
            catchThrowable(() -> backupService.exportBackupToFile(backupId, null, null));

        assertThat(exception)
            .isInstanceOf(ObjectNotFoundException.class)
            .hasMessage("The backupId 625df11878aad7513a414092 informed was not found. Please, check this information");
    }

    @Test
    @DisplayName("Should throw AcceptedException to try extract backup")
    void itShouldThrowAcceptedExceptionInExtractBackupFileByLabel() {
        String backupId = "625df11878aad7513a414092";

        var backupInProgress = Backup.builder()
            .backupId(backupId)
            .status(Status.IN_PROGRESS.getGetKey())
            .date(LocalDateTime.now())
            .build();

        when(backupRepository.findById(backupId)).thenReturn(Optional.of(backupInProgress));

        Throwable exception =
            catchThrowable(() -> backupService.exportBackupToFile(backupId, null, null));

        assertThat(exception)
            .isInstanceOf(AcceptedException.class)
            .hasMessage("Your backup does not finish yet. Please, wait some minutes to extract your backup");
    }

    @Test
    @DisplayName("Should extract backup by label")
    void itShouldExtractBackupFileByLabel() throws IOException {
        String label = "IMPORTANT";
        String backupId = "625df11878aad7513a414092";
        List<Email> emails = buildEmailList(backupId);

        var backupInProgress = Backup.builder()
            .backupId(backupId)
            .status(Status.OK.getGetKey())
            .date(LocalDateTime.now())
            .build();

        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        var outputStream = httpServletResponse.getOutputStream();

        when(backupRepository.findById(backupId)).thenReturn(Optional.of(backupInProgress));
        when(emailRepository.findByLabelIdsAndBackupId(label, backupId)).thenReturn(emails);

        var response = backupService.exportBackupToFile(backupId, label, outputStream);

        assertThat(response)
            .isNotNull()
            .isInstanceOf(Optional.class);
    }

    @Test
    @DisplayName("Should extract all emails to ZIP file")
    void itShouldExtractBackupToZipFile() throws IOException {
        String backupId = "625df11878aad7513a414092";
        List<Email> emails = buildEmailList(backupId);

        var backupInProgress = Backup.builder()
            .backupId(backupId)
            .status(Status.OK.getGetKey())
            .date(LocalDateTime.now())
            .build();

        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        var outputStream = httpServletResponse.getOutputStream();

        when(backupRepository.findById(backupId)).thenReturn(Optional.of(backupInProgress));
        when(emailRepository.findByBackupId(backupId)).thenReturn(emails);

        var response = backupService.exportBackupToFile(backupId, null, outputStream);

        assertThat(response)
            .isNotNull()
            .isInstanceOf(Optional.class);
    }

    private List<Email> buildEmailList(String backupId) {
        Email email1 = Email.builder()
            .backupId(backupId)
            .historyId(BigInteger.valueOf(7005326))
            .internalDate(1649421445000L)
            .labelIds(Arrays.asList("IMPORTANT", "INBOX"))
            .raw("VG86IHVuc3Vic2NyaWJlLTk0YWNjZDVjOGRiNWQwZWM1MGE2MDlkZDFiNzIzYW")
            .sizeEstimate(474)
            .snippet("This message was automatically generated by Gmail.")
            .threadId("180092de95ddb2bc")
            .build();

        Email email2 = Email.builder()
            .backupId(backupId)
            .historyId(BigInteger.valueOf(2342344))
            .internalDate(82734628734687L)
            .labelIds(Arrays.asList("IMPORTANT", "CATEGORY_UPDATE"))
            .raw("VG86IHVuc3Vic2NyaWJlLTk0YWNjZDVjOGRiNWQwZWM1MGE2MDlkZDFiNzIzYW")
            .sizeEstimate(1287)
            .snippet("This message was automatically generated by Axcient Company.")
            .threadId("23kj4gh2jh3g424")
            .build();

        return Arrays.asList(email1, email2);
    }

    @Test
    @DisplayName("Should throw FileException to try extract backup")
    void itShouldThrowFileExceptionInExtractBackupFileByLabel() throws IOException {
        String label = "TEST";
        String backupId = "625df11878aad7513a414092";

        var backupInProgress = Backup.builder()
            .backupId(backupId)
            .status(Status.OK.getGetKey())
            .date(LocalDateTime.now())
            .build();

        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        var outputStream = httpServletResponse.getOutputStream();

        when(backupRepository.findById(backupId)).thenReturn(Optional.of(backupInProgress));
        when(emailRepository.findByBackupId(backupId)).thenReturn(List.of());

        Throwable exception =
            catchThrowable(() -> backupService.exportBackupToFile(backupId, label, outputStream));

        assertThat(exception)
            .isInstanceOf(ObjectNotFoundException.class)
            .hasMessage("The backupId 625df11878aad7513a414092 informed was not found. Please, check this information");
    }

}
