package com.axcient.gmailsafe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axcient.gmailsafe.entity.Backup;
import com.axcient.gmailsafe.entity.Status;
import com.axcient.gmailsafe.repository.BackupRepository;
import com.axcient.gmailsafe.service.exception.AcceptedException;
import com.axcient.gmailsafe.service.impl.BackupServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class BackupServiceImplTest {

    BackupService backupService;

    @MockBean
    BackupRepository backupRepository;

    @BeforeEach
    public void setup() {
        this.backupService = new BackupServiceImpl(backupRepository);
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

}
