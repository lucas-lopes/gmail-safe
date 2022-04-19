package com.axcient.gmailsafe.config;

import com.axcient.gmailsafe.repository.BackupRepository;
import com.axcient.gmailsafe.repository.EmailRepository;
import com.axcient.gmailsafe.util.Constants;
import com.axcient.gmailsafe.util.FileUtil;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class GmailSafeConfiguration implements CommandLineRunner {

    private final EmailRepository emailRepository;
    private final BackupRepository backupRepository;

    @Override
    public void run(String... args) {
        emailRepository.deleteAll();
        backupRepository.deleteAll();
        FileUtil.createOutputFolder(Constants.FOLDER_DEFAULT);
    }
}
