package com.cognizant.lineage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cognizant.lineage.upload.model.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = LineageBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleBadRequest(LineageBusinessException ex) {
        return new ErrorResponse(400, ex.getMessage());
    }
 
    @ExceptionHandler(value = LineageRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleLineageException(LineageRuntimeException ex) {
        String errorMessage = ex.getMessage();
        return new ErrorResponse(500, errorMessage);
    }

    @ExceptionHandler(value = SQLRecordPersistException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleSQLPersisException(SQLRecordPersistException ex) {
        String errorMessage = ex.getMessage();
        return new ErrorResponse(500, "Error during persis : " + errorMessage);
    }
}
