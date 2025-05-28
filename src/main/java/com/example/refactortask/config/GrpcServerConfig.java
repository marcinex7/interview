package com.example.refactortask.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServerConfig {

    @Value("${grpc.server.port}")
    private int grpcPort;

    @Bean
    public Server grpcServer(
            // Will be injected by Spring
            com.example.refactortask.grpc.ProductServiceImpl productService,
            com.example.refactortask.grpc.CategoryGrpcServiceImpl categoryService) throws IOException {
        
        // Create and start the gRPC server
        Server server = ServerBuilder.forPort(grpcPort)
                .addService(productService)
                .addService(categoryService)
                .build();
        
        // Start the server in a separate thread
        new Thread(() -> {
            try {
                server.start();
                log.info("gRPC Server started on port {}", grpcPort);
                server.awaitTermination();
            } catch (IOException e) {
                log.error("Failed to start gRPC server", e);
            } catch (InterruptedException e) {
                log.error("gRPC server interrupted", e);
            }
        }).start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            if (server != null) {
                server.shutdown();
            }
            log.info("gRPC server shut down successfully");
        }));
        
        return server;
    }
}