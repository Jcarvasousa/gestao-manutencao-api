package br.com.joaovitor.gestaomanutencao.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(SolicitacaoJaAbertaException.class)
    public ResponseEntity<ErroResponseDTO> handleSolicitacaoJaAbertaException(SolicitacaoJaAbertaException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(SolicitacaoNaoPodeSerRecebidaException.class)
    public ResponseEntity<ErroResponseDTO> handleSolicitacaoNaoPodeSerRecebidaException(SolicitacaoNaoPodeSerRecebidaException exception) {
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

    @ExceptionHandler(OrcamentoJaExisteException.class)
    public ResponseEntity<ErroResponseDTO> handleOrcamentoJaExisteException(OrcamentoJaExisteException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ManutencaoSemResponsavelException.class)
    public ResponseEntity<ErroResponseDTO> handleManutencaoSemResponsavelException(ManutencaoSemResponsavelException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ManutencaoDadosConclusaoIncompletosException.class)
    public ResponseEntity<ErroResponseDTO> handleManutencaoDadosConclusaoIncompletosException(ManutencaoDadosConclusaoIncompletosException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AjusteEstoqueInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleAjusteEstoqueInvalidoException(AjusteEstoqueInvalidoException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(RelatorioParametrosInvalidosException.class)
    public ResponseEntity<ErroResponseDTO> handleRelatorioParametrosInvalidosException(RelatorioParametrosInvalidosException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception
    ) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                exception.getClass().getSimpleName(),
                "Operação viola uma restrição de integridade dos dados (ex: registro duplicado)."
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Corpo da requisição inválido ou malformado."
        );
        return ResponseEntity.status(status).body(body);
    }

    // Sem este handler, o handler genérico de Exception abaixo transformaria a falha
    // de credenciais em 500 antes que o Spring Security pudesse responder 401.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleAuthenticationException(AuthenticationException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Credenciais inválidas."
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleException(Exception exception) {
        LOGGER.error("Erro interno não tratado.", exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErroResponseDTO body = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Erro interno no servidor. Contate o suporte."
        );
        return ResponseEntity.status(status).body(body);
    }
}