package com.medteams.catalog.infrastructure.web.error;

import com.medteams.commons.error.ErrorCode;
import com.medteams.commons.error.ErrorResponse;
import com.medteams.commons.error.FieldViolation;
import com.medteams.commons.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;
import java.util.List;

/**
 * Traduit toute exception en {@link ErrorResponse} : meme format pour toutes les erreurs, aucun detail
 * technique expose au client, toute erreur inattendue journalisee.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Toutes les exceptions de commons-api portent leur ErrorCode : un seul handler suffit.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return respond(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
                .toList();
        return respond(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, "Requete invalide", request.getRequestURI(), violations));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameters(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldViolation(result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
                .toList();
        return respond(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, "Parametres invalides", request.getRequestURI(), violations));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        return respond(ErrorResponse.of(ErrorCode.MALFORMED_REQUEST,
                "Requete illisible : verifiez le JSON envoye et le type des parametres", request.getRequestURI()));
    }

    // Modification concurrente (verrou optimiste) ou contrainte d'unicite violee entre la verification et l'ecriture.
    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataConflict(Exception ex, HttpServletRequest request) {
        log.warn("Conflit de donnees sur {} : {}", request.getRequestURI(), ex.getMessage());
        return respond(ErrorResponse.of(ErrorCode.CONFLICT,
                "La ressource a ete modifiee entre-temps ou existe deja, rechargez-la puis reessayez", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        // Erreurs standard de Spring MVC (route inconnue, methode non supportee...) : on conserve leur statut.
        if (ex instanceof org.springframework.web.ErrorResponse frameworkError) {
            ErrorCode code = ErrorCode.fromHttpStatus(frameworkError.getStatusCode().value());
            String detail = frameworkError.getBody().getDetail();
            return respond(ErrorResponse.of(code, detail != null ? detail : code.name(), request.getRequestURI()));
        }
        log.error("Erreur inattendue sur {}", request.getRequestURI(), ex);
        return respond(ErrorResponse.of(ErrorCode.INTERNAL_ERROR,
                "Erreur interne, l'incident a ete journalise", request.getRequestURI()));
    }

    private static ResponseEntity<ErrorResponse> respond(ErrorResponse body) {
        return ResponseEntity.status(body.status()).body(body);
    }
}
