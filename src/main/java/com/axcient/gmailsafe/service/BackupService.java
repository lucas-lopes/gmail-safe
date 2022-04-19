package com.axcient.gmailsafe.service;

import com.axcient.gmailsafe.entity.Backup;
import java.util.List;

public interface BackupService {

    Backup saveBackup();

    List<Backup> findAllBackups();

}
