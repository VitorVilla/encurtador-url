package com.topaz.encurtador.exception.mapper;

import com.topaz.encurtador.dto.response.ErrorResponse;
import com.topaz.encurtador.exception.InvalidUrlException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidUrlExceptionMapper implements ExceptionMapper<InvalidUrlException> {

    @Override
    public Response toResponse(InvalidUrlException exception) {
        int status = Response.Status.BAD_REQUEST.getStatusCode();
        return Response.status(status)
                .entity(new ErrorResponse(status, exception.getMessage()))
                .build();
    }
}
