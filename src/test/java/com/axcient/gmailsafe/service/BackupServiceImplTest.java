package com.axcient.gmailsafe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Email;
import com.axcient.gmailsafe.entity.Status;
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
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@ExtendWith(SpringExtension.class)
class BackupServiceImplTest {

    BackupService backupService;

    @MockBean
    BackupRepository backupRepository;

    @MockBean
    EmailRepository emailRepository;

    @BeforeEach
    public void setup() {
        this.backupService = new BackupServiceImpl(backupRepository, emailRepository);
    }

    @Test
    @DisplayName("Should save a new hero with success")
    void itShouldSaveNewBackup() {
        var backupInProgress = Backup.builder()
            .backupId("625df11878aad7513a414091")
            .status(Status.IN_PROGRESS)
            .date(LocalDateTime.now())
            .build();

        when(backupRepository.findAll()).thenReturn(List.of());
        when(backupRepository.save(Mockito.any(Backup.class))).thenReturn(backupInProgress);

        var backup = backupService.saveBackup();

        assertThat(backup).isNotNull();
        assertThat(backup.getBackupId()).isEqualTo("625df11878aad7513a414091");
        assertThat(backup.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(backup.getDate()).isNotNull();
    }

    @Test
    @DisplayName("Should throw an AcceptedException")
    void itShouldThrowAcceptedException() {
        var backupInProgress = Backup.builder()
            .backupId("625df11878aad7513a414091")
            .status(Status.IN_PROGRESS)
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
            .status(Status.IN_PROGRESS)
            .date(LocalDateTime.now())
            .build();

        var backupOk = Backup.builder()
            .backupId("625df11878aad7513a414092")
            .status(Status.FAILED)
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
            catchThrowable(() -> backupService.extractBackupToFile(null, null, null));

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
            catchThrowable(() -> backupService.extractBackupToFile(backupId, null, null));

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
            .status(Status.IN_PROGRESS)
            .date(LocalDateTime.now())
            .build();

        when(backupRepository.findById(backupId)).thenReturn(Optional.of(backupInProgress));

        Throwable exception =
            catchThrowable(() -> backupService.extractBackupToFile(backupId, null, null));

        assertThat(exception)
            .isInstanceOf(AcceptedException.class)
            .hasMessage("Your backup does not finish yet. Please, wait some minutes to extract your backup");
    }

}
