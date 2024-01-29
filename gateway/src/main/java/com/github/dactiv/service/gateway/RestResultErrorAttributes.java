package com.github.dactiv.service.gateway;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * rest 格式的全局错误实现
 *
 * @author maurice.chenf
 */
@Component
public class RestResultErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);

        MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
                .from(
                        error.getClass(),
                        MergedAnnotations.SearchStrategy.TYPE_HIERARCHY
                )
                .get(ResponseStatus.class);

        HttpStatus status = determineHttpStatus(error, responseStatusAnnotation);

        RestResult<Object> result = RestResult.of(
                status.getReasonPhrase(),
                status.value(),
                ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                new LinkedHashMap<>()
        );

        if (error instanceof BindingResult) {
            BindingResult bindingResult = Casts.cast(error, BindingResult.class);
            if (bindingResult.hasErrors()) {
                result.setData(bindingResult.getAllErrors());
            }
        } else if (error instanceof ErrorCodeException) {
            ErrorCodeException errorCodeException = Casts.cast(error, ErrorCodeException.class);

            result.setExecuteCode(errorCodeException.getErrorCode());
            result.setMessage(errorCodeException.getMessage());
        }

        return Casts.convertValue(result, Casts.MAP_TYPE_REFERENCE);
    }

    private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof ResponseStatusException) {
            ResponseStatusException status = Casts.cast(error);
            return HttpStatus.valueOf(status.getStatusCode().value());
        }
        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
