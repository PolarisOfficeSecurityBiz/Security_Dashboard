package com.polarisoffice.security.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RootingRequest {
	@NotNull private Long logEntryId;
	private String rootReason;
	private String path;
	private String androidId;
}
