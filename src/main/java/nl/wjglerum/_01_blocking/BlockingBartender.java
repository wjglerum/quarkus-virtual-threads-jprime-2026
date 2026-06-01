package nl.wjglerum._01_blocking;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class BlockingBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    public BlockingBeverage get() {
        Log.info("Warming up the blocking coffee machine");
        var response = coffeeMachine.brew();
        return new BlockingBeverage("Blocking " + response.name());
    }
}
