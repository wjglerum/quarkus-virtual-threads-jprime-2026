package nl.wjglerum._03_virtual;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class FlakeyVirtualBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    public VirtualBeverage get() {
        Log.info("Warming up the flakey virtual coffee machine (50% chance of failure)");
        var response = coffeeMachine.brew();
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new RuntimeException("Coffee machine broke!");
        }
        return new VirtualBeverage("Flakey Virtual " + response.name());
    }
}
