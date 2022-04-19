package com.axcient.gmailsafe.repository;

import com.axcient.gmailsafe.entity.Email;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailRepository extends MongoRepository<Email, String> {

    List<Email> findByLabelIdsAndBackupId(String label, String backupId);

    List<Email> findByBackupId(String backupId);

}
