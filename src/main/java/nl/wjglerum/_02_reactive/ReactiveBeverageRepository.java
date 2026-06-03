package nl.wjglerum._02_reactive;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

@ApplicationScoped
public class ReactiveBeverageRepository {

    @Inject
    Mutiny.Session session;

    Uni<ReactiveBeverage> save(ReactiveBeverage beverage) {
        Log.info("Persisting reactive beverage");
        var entity = new ReactiveBeverageEntity(beverage.name());
        return session.persist(entity).replaceWith(beverage);
    }

    Uni<List<ReactiveBeverage>> save(List<ReactiveBeverage> beverages) {
        Log.info("Persisting reactive beverages");
        var entities = beverages.stream()
                .map(beverage -> new ReactiveBeverageEntity(beverage.name()))
                .toArray();
        return session.persistAll(entities).replaceWith(beverages);
    }
}
