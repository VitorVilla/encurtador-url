package com.topaz.encurtador.exception.mapper;

import com.topaz.encurtador.dto.response.ErrorResponse;
import com.topaz.encurtador.exception.ShortUrlNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ShortUrlNotFoundExceptionMapper
        implements ExceptionMapper<ShortUrlNotFoundException> {

    @Override
    public Response toResponse(ShortUrlNotFoundException exception) {
        int status = Response.Status.NOT_FOUND.getStatusCode();
        return Response.status(status)
                .entity(new ErrorResponse(status, exception.getMessage()))
                .build();
    }
}
