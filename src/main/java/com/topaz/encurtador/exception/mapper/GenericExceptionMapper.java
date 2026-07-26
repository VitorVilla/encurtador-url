package com.topaz.encurtador.exception.mapper;

import com.topaz.encurtador.dto.response.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;


@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        LOGGER.log(Level.SEVERE, "Erro interno nao tratado", exception);

        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        return Response.status(status)
                .entity(new ErrorResponse(status, "Erro interno inesperado."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
