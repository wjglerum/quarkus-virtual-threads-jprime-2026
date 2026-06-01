package nl.wjglerum._05_pinning;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class PinningBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    // synchronized used to pin in Java 21–23; fixed by JEP 491 in Java 24
    public synchronized PinningBeverage get() {
        Log.info("Warming up the coffee machine (synchronized)");
        var response = coffeeMachine.brew();
        return new PinningBeverage("Pinning " + response.name());
    }
}
