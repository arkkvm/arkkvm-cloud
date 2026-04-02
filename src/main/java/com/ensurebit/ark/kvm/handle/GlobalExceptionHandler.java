package com.ensurebit.ark.kvm.handle;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.ensurebit.ark.kvm.vo.BaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public BaseResult<?> defaultExceptionHandler(Exception ex) {
        logger.error("defaultExceptionHandler", ex);
        return BaseResult.fail(500, ex.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<BaseResult<?>> notLoginExceptionHandler(NotLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<BaseResult<?>> notPermissionExceptionHandler(NotPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResult.fail(ex.getMessage()));
    }

}
