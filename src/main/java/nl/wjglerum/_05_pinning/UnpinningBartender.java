package nl.wjglerum._05_pinning;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.wjglerum.client.CoffeeMachineClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
public class UnpinningBartender {

    @Inject
    @RestClient
    CoffeeMachineClient coffeeMachine;

    // ReentrantLock never pinned — the recommended alternative before JEP 491
    private final ReentrantLock lock = new ReentrantLock();

    public UnpinningBeverage get() {
        Log.info("Warming up the coffee machine (ReentrantLock)");
        lock.lock();
        try {
            var response = coffeeMachine.brew();
            return new UnpinningBeverage("Unpinning " + response.name());
        } finally {
            lock.unlock();
        }
    }
}
