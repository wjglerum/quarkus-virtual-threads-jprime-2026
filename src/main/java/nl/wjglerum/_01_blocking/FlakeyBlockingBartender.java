package nl.wjglerum._01_blocking;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class FlakeyBlockingBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    public BlockingBeverage get() {
        Log.info("Warming up the flakey blocking coffee machine (50% chance of failure)");
        var response = coffeeMachine.brew();
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new RuntimeException("Coffee machine broke!");
        }
        return new BlockingBeverage("Flakey Blocking " + response.name());
    }
}
