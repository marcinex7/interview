package com.example.refactortask.exception;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import org.slf4j.event.Level;

import java.util.List;

/**
 * Global exception handler for gRPC services.
 * This class provides methods to handle different types of exceptions and convert them to appropriate gRPC status codes.
 */
@GrpcAdvice
@Slf4j
@AllArgsConstructor
public class GrpcExceptionHandler {

    private static final String LOG_PREFIX = "GRPC EXCEPTION ADVICE";
    private static final String FAILED_WITH_LOG_MESSAGE = "{}: failed with {}: {}";

    /**
     * Handle ResourceNotFoundException
     */
    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusRuntimeException handleResourceNotFoundException(ResourceNotFoundException ex) {
        logStandardException(ex, Level.WARN);
        var status = buildRpcStatus(ex, Code.NOT_FOUND);
        return StatusProto.toStatusRuntimeException(status.build());
    }

    /**
     * Handle IllegalArgumentException
     */
    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleIllegalArgumentException(IllegalArgumentException ex) {
        var status = buildRpcStatus(ex, Code.INVALID_ARGUMENT);
        logStandardException(ex, Level.ERROR);
        return StatusProto.toStatusRuntimeException(status.build());
    }

    /**
     * Handle ConstraintViolationException
     */
    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(ConstraintViolationException.class)
    public StatusRuntimeException handleConstraintViolationException(ConstraintViolationException ex) {
        BadRequest badRequest = buildViolationDetails(ex);
        logStandardException(ex, Level.ERROR);
        com.google.rpc.Status.Builder statusBuilder = buildRpcStatus(ex, Code.INVALID_ARGUMENT);
        statusBuilder.addDetails(Any.pack(badRequest));
        return StatusProto.toStatusRuntimeException(statusBuilder.build());
    }

    /**
     * Handle RuntimeException
     */
    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(RuntimeException.class)
    public StatusRuntimeException handleRuntimeException(RuntimeException ex) {
        logStandardException(ex, Level.ERROR);
        var status = buildRpcStatus(ex, Code.INTERNAL);
        return StatusProto.toStatusRuntimeException(status.build());
    }

    /**
     * Handle all other exceptions
     */
    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleException(Exception ex) {
        log.error(
            FAILED_WITH_LOG_MESSAGE, LOG_PREFIX, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        var status = buildRpcStatus(ex, Code.INTERNAL);
        return StatusProto.toStatusRuntimeException(status.build());
    }

    private com.google.rpc.Status.Builder buildRpcStatus(Exception ex, Code code) {
        return buildRpcStatus(ex, code, ex.getMessage());
    }

    private com.google.rpc.Status.Builder buildRpcStatus(
        Exception ex, Code code, String fullErrorMessage) {
        var errorInfo =
            ErrorInfo.newBuilder().setReason(fullErrorMessage != null ? fullErrorMessage : "").build();
        return com.google.rpc.Status.newBuilder()
            .setCode(code.getNumber())
            .setMessage(fullErrorMessage != null ? fullErrorMessage : "")
            .addDetails(Any.pack(errorInfo));
    }

    private static BadRequest buildViolationDetails(ConstraintViolationException ex) {
        List<BadRequest.FieldViolation> list =
            ex.getConstraintViolations().stream()
                .map(
                    constraintViolation ->
                        BadRequest.FieldViolation.newBuilder()
                            .setField(constraintViolation.getPropertyPath().toString())
                            .setDescription(constraintViolation.getMessage())
                            .build())
                .toList();
        return BadRequest.newBuilder().addAllFieldViolations(list).build();
    }

    private static void logStandardException(RuntimeException ex, Level level) {
        log.atLevel(level)
            .log(
                FAILED_WITH_LOG_MESSAGE,
                LOG_PREFIX,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);
    }
}
