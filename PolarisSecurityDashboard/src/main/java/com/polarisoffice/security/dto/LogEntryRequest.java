// dto/LogEntryRequest.java
package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.LogType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LogEntryRequest {
    @NotNull private String licenseKey;
    private String domain;
    @NotNull private LogType logType;
    private Integer osVersion;
    private Integer appVersion;
    private String extra;
}
