package nl.wjglerum.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/coffee-machine")
@RegisterRestClient(configKey = "coffee-machine")
public interface CoffeeMachineClient {

    @GET
    @Path("/brew")
    CoffeeMachineResponse brew();

    @GET
    @Path("/brew")
    Uni<CoffeeMachineResponse> brewAsync();
}
