package com.ibm.app.demo.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.apache.commons.text.StringEscapeUtils;

@Path("/client/service")
public class ServiceController {

    @GET
    @Path("/{parameter}")
    public String doSomething(@PathParam("parameter") String parameter) {
        String safeParameter = StringEscapeUtils.escapeHtml4(parameter);
        return String.format("Processed parameter value '%s'", safeParameter);
    }
}
