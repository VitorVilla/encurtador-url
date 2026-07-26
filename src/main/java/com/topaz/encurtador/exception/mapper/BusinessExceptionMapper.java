package com.topaz.encurtador.exception.mapper;

import com.topaz.encurtador.dto.response.ErrorResponse;
import com.topaz.encurtador.exception.BusinessException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Traduz qualquer excecao de negocio para a resposta HTTP correspondente,
 * usando o status que a propria excecao carrega. O RESTEasy resolve subindo
 * a hierarquia (ex.: AliasAlreadyInUseException -> BusinessException), entao
 * um unico mapper cobre todos os casos de negocio.
 */
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException exception) {
        return Response.status(exception.getStatus())
                .entity(new ErrorResponse(exception.getStatus(), exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
