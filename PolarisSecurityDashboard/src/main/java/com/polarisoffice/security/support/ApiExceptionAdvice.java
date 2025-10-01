package com.polarisoffice.security.support;

import org.springframework.web.bind.annotation.*;
import com.google.api.gax.rpc.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionAdvice {
  @ExceptionHandler(ApiException.class)
  @ResponseStatus(HttpStatus.BAD_GATEWAY) // 502로 내려서 프론트와 구분
  public Map<String,Object> handle(ApiException e) {
    return Map.of(
      "message", e.getMessage(),
      "statusCode", e.getStatusCode().getCode().name(),
      "retryable", e.isRetryable()
    );
  }
}