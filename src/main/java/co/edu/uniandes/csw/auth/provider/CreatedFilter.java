package co.edu.uniandes.csw.auth.provider;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Filter to change response status from OK to CREATED
 *
 * @author af.esguerra10
 */
@StatusCreated
@Provider
public class CreatedFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
        if (resp.getStatus() >= HttpServletResponse.SC_OK && resp.getStatus() < HttpServletResponse.SC_MULTIPLE_CHOICES) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
        }
    }
}
