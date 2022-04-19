package com.axcient.gmailsafe.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document
@AllArgsConstructor
public class Email implements Serializable {

    private static final Long SERIAL_VERSION_UUID = 1L;

    @Id
    private String emailId;

    private String id;
    private String backupId;
    private BigInteger historyId;
    private Long internalDate;
    private List<String> labelIds;
    private String raw;
    private Integer sizeEstimate;
    private String snippet;
    private String threadId;

    @Override
    public String toString() {
        return "{" +
            "\"id\": \"" + id + "\"" +
            ",\"historyId\": \"" + historyId + "\"" +
            ",\"internalDate\": \"" + internalDate + "\"" +
            ",\"labelIds\": \"" + labelIds + "\"" +
            ",\"raw\": \"" + raw + "\"" +
            ",\"sizeEstimate\": \"" + sizeEstimate + "\"" +
            ",\"snippet\": \"" + snippet + "\"" +
            ",\"threadId\": \"" + threadId + "\"" +
            "}";
    }

}
