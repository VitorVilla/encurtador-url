package com.topaz.encurtador.exception;

import javax.ejb.ApplicationException;

/**
 * Base das excecoes de negocio. Carrega o status HTTP correspondente, o que
 * permite um unico ExceptionMapper (BusinessExceptionMapper) atender todas.
 *
 * @ApplicationException(inherited = true): faz o container EJB tratar esta
 * excecao (e suas subclasses) como excecao de negocio - ou seja, propaga ela
 * intacta em vez de embrulhar em EJBException, e marca a transacao para rollback.
 */
@ApplicationException(rollback = true, inherited = true)
public abstract class BusinessException extends RuntimeException {

    private final int status;

    protected BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
