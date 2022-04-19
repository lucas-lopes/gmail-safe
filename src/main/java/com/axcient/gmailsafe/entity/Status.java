package com.axcient.gmailsafe.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    OK("OK"),
    FAILED("Failed"),
    IN_PROGRESS("In Progress");

    private final String getKey;

}