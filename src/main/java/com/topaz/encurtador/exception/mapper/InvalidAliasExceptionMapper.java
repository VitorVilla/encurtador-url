package com.topaz.encurtador.exception.mapper;

import com.topaz.encurtador.dto.response.ErrorResponse;
import com.topaz.encurtador.exception.InvalidAliasException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidAliasExceptionMapper implements ExceptionMapper<InvalidAliasException> {

    @Override
    public Response toResponse(InvalidAliasException exception) {
        int status = Response.Status.BAD_REQUEST.getStatusCode();
        return Response.status(status)
                .entity(new ErrorResponse(status, exception.getMessage()))
                .build();
    }
}
