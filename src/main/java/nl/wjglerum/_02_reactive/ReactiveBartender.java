package nl.wjglerum._02_reactive;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ReactiveBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    public Uni<ReactiveBeverage> get() {
        Log.info("Warming up the reactive coffee machine");
        return coffeeMachine.brewAsync()
                .map(response -> new ReactiveBeverage("Reactive " + response.name()));
    }
}
