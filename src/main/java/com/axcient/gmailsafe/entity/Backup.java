package com.axcient.gmailsafe.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document
@JsonInclude(Include.NON_NULL)
public class Backup implements Serializable {

    private static final Long SERIAL_VERSION_UUID = 1L;

    @Id
    private String backupId;

    @Setter
    private String status;

    private LocalDateTime date;

}
