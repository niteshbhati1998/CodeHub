package com.cognizant.assessment.vault.exception.handler;


import com.cognizant.assessment.vault.exception.BadRequestException;
import com.cognizant.assessment.vault.exception.VaultException;
import com.cognizant.assessment.vault.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleBadRequest(BadRequestException ex) {
        return new ErrorResponse(400, ex.getMessage());
    }

    @ExceptionHandler(value = VaultException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleVaultException(VaultException ex) {
        String errorMessage = ex.getMessage();
        if (errorMessage.contains("encryption key not found")) {
           return new ErrorResponse(500, "encryption key not found.");
        }
        if (errorMessage.contains("sealed")) {
            return new ErrorResponse(500, "failed to unseal, check with vault Admin");
        }
        return new ErrorResponse(500, ex.getMessage());
    }
}
