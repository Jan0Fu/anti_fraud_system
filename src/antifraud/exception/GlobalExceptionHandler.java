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
    ResponseEntity<ErrorDto> errorHandler(IpNotFoundException exception) {
        return ResponseEntity.status(404).body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(NegativeNumberException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(IpDuplicateException exception) {
        return ResponseEntity.status(409)
                .body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> errorHandler(IncorrectIpInput exception) {
        return ResponseEntity.status(400).body(new ErrorDto(exception.getMessage()));
    }
}
