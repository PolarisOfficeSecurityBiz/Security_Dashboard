package com.polarisoffice.security.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoteLogRequest {
	@NotNull private Long logEntryId;
	private String remoteType;
	private String packageName;
	private String path;
	private String androidId;
}
