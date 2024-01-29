package com.github.dactiv.service.gateway;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.service.gateway.config.ApplicationConfig;
import lombok.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * rest result 形式的 sentinel 异常响应
 *
 * @author maurice.chen
 */
public class RestResultGatewayBlockExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    private final ApplicationConfig applicationConfig;

    public RestResultGatewayBlockExceptionHandler(ObjectMapper objectMapper, ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public @NonNull Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }

        return exchange.getResponse().writeWith(Mono.create(dataBuffer -> {

            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String message = applicationConfig.getDefaultReasonPhrase();
            int statusValue = HttpStatus.INTERNAL_SERVER_ERROR.value();

            ServerHttpResponse response = exchange.getResponse();
            if (Objects.nonNull(response.getStatusCode())) {
                HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
                message = status.getReasonPhrase();
                statusValue = status.value();
            }

            RestResult<Object> result = RestResult.of(
                    message,
                    statusValue,
                    ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                    new LinkedHashMap<>()
            );

            byte[] bytes;

            try {
                bytes = objectMapper.writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                bytes = e.getMessage().getBytes();
            }

            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            dataBuffer.success(buffer);
        }));

    }

}
