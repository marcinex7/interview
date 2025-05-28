package com.example.refactortask.exception;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import org.slf4j.event.Level;

import java.util.List;

@GrpcAdvice
@Slf4j
public class GrpcExceptionHandler {

    private static final String LOG_PREFIX = "GRPC EXCEPTION ADVICE";
    private static final String FAILED_WITH_LOG_MESSAGE = "{}: failed with {}: {}";

    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusRuntimeException handleResourceNotFoundException(ResourceNotFoundException ex) {
        logStandardException(ex, Level.WARN);
        var status = buildRpcStatus(ex, Code.NOT_FOUND);
        return StatusProto.toStatusRuntimeException(status.build());
    }

    @net.devh.boot.grpc.server.advice.GrpcExceptionHandler(RuntimeException.class)
    public StatusRuntimeException handleRuntimeException(RuntimeException ex) {
        logStandardException(ex, Level.ERROR);
        var status = buildRpcStatus(ex, Code.INTERNAL);
        return StatusProto.toStatusRuntimeException(status.build());
    }

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
