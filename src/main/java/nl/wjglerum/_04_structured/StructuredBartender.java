package nl.wjglerum._04_structured;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class StructuredBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    public StructuredBeverage get() {
        Log.info("Warming up the structured coffee machine");
        var response = coffeeMachine.brew();
        return new StructuredBeverage("Structured " + response.name());
    }
}
