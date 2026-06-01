package nl.wjglerum._02_reactive;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class FlakeyReactiveBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    public Uni<ReactiveBeverage> get() {
        Log.info("Warming up the flakey reactive coffee machine (50% chance of failure)");
        return coffeeMachine.brewAsync()
                .map(response -> {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        throw new RuntimeException("Coffee machine broke!");
                    }
                    return new ReactiveBeverage("Flakey Reactive " + response.name());
                });
    }
}
