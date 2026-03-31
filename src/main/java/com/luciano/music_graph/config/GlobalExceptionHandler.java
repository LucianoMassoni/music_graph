package com.luciano.music_graph.config;

import com.luciano.music_graph.dto.ExceptionHandlerDto;
import com.luciano.music_graph.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ExceptionHandlerDto> buildResponse(HttpStatus status, String message){
        return ResponseEntity.status(status).body(new ExceptionHandlerDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now()
        ));
    }

    @ExceptionHandler({ArtistNotFoundException.class, EmailNotFoundException.class, LastFmArtistNotFoundException.class,
    RefreshTokenNotFoundException.class, UserArtistNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ExceptionHandlerDto> handleNotFound(RuntimeException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({LastFmApiException.class, UserAlreadyExistsException.class, UsernameAlreadyExistsException.class})
    public ResponseEntity<ExceptionHandlerDto> handlerBadRequest(RuntimeException ex){
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpirationException.class)
    public ResponseEntity<ExceptionHandlerDto> refreshTokenExpiration(RefreshTokenExpirationException ex){
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionHandlerDto> genericHandler(Exception ex){
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
