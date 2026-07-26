package com.topaz.encurtador.exception.mapper;

import com.topaz.encurtador.dto.response.ErrorResponse;
import com.topaz.encurtador.exception.AliasAlreadyInUseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class AliasAlreadyInUseExceptionMapper
        implements ExceptionMapper<AliasAlreadyInUseException> {

    @Override
    public Response toResponse(AliasAlreadyInUseException exception) {
        int status = Response.Status.CONFLICT.getStatusCode();
        return Response.status(status)
                .entity(new ErrorResponse(status, exception.getMessage()))
                .build();
    }
}
