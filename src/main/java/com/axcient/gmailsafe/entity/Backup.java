package com.axcient.gmailsafe.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;

@Getter
@Builder
public class Backup implements Serializable {

    private static final Long SERIAL_VERSION_UUID = 1L;

    @Id
    private String backupId;
    private Status status;
    private LocalDateTime date;

}
