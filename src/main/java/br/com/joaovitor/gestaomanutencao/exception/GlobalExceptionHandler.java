package br.com.joaovitor.gestaomanutencao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponseDTO(
            LocalDateTime timestamp,
            Integer status,
            String erro,
            String mensagem
    ) {
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleRecursoNaoEncontradoException(RecursoNaoEncontradoException exception) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(CompraDesnecessariaException.class)
    public ResponseEntity<ErroResponseDTO> handleCompraDesnecessariaException(CompraDesnecessariaException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErroResponseDTO> handleEstoqueInsuficienteException(EstoqueInsuficienteException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ManutencaoNaoEstaAbertaException.class)
    public ResponseEntity<ErroResponseDTO> handleManutencaoNaoEstaAbertaException(ManutencaoNaoEstaAbertaException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }
}