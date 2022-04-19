package com.axcient.gmailsafe.controller.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class StandardError implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer status;
    private final String message;
    private final String error;

    @Builder.Default
    private final Long timestamp = System.currentTimeMillis();

}
