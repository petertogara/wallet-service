package com.petmuc.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlayerNotFoundException.class)
    public Mono<ProblemDetail> handlePlayerNotFoundException(PlayerNotFoundException ex) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ProblemDetail> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }


    @ExceptionHandler(TransactionNotFoundException.class)
    public Mono<ProblemDetail> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ProblemDetail> handleInvalidInputException(ServerWebInputException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid request payload or parameters.");
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handleGenericException(Exception ex) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private Mono<ProblemDetail> buildProblemDetail(HttpStatus status, String message) {
        return Mono.just(
                ProblemDetail.forStatusAndDetail(status, message)
        );
    }
}
