package com.axcient.gmailsafe.repository;

import com.axcient.gmailsafe.entity.Backup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackupRepository extends MongoRepository<Backup, String> {

}
