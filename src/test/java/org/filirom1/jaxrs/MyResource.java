package org.filirom1.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class MyResource{
    public static final String CONTENT = "This is an easy resource (as plain text)";

    @GET
    @Produces("text/plain")
    public String getPlain() {
        return CONTENT;
    }
}
