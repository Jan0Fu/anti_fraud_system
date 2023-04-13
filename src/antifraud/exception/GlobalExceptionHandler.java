package antifraud.exception;

import antifraud.exception.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(BusinessException exception) {
        return ResponseEntity.badRequest().body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(IpNotFound exception) {
        return ResponseEntity.status(406).body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(IpDuplicate exception) {
        return ResponseEntity.status(409)
                .body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(IncorrectIp exception) {
        return ResponseEntity.status(400).body(new ErrorDto(exception.getMessage()));
    }
}
