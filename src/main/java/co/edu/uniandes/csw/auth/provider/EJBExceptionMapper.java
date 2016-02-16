package co.edu.uniandes.csw.auth.provider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ejb.EJBException;

/**
 * Exception mapper for any exception wrapped in EJBException.
 * All RuntimeExceptions raise within an EJB are wrapped in {@link EJBException}.
 * This mapper changes the response to show the error message as the body of the response
 * and returns an INTERNAL_SERVER_ERROR (500).
 * @author af.esguerra10
 */
@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    @Override
    public Response toResponse(EJBException exception) {
        return Response.serverError()
                .entity(getInitCause(exception).getLocalizedMessage())
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

    /**
     * Recursively retrieves the root cause of an exception.
     * @param e Thrown exception
     * @return Root cause
     */
    private Throwable getInitCause(Throwable e) {
        if (e.getCause() != null) {
            return getInitCause(e.getCause());
        } else {
            return e;
        }
    }
}
