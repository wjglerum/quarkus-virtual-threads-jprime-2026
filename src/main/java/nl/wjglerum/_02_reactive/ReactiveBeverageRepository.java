package nl.wjglerum._02_reactive;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ReactiveBeverageRepository implements PanacheRepository<ReactiveBeverageEntity> {

    Uni<ReactiveBeverage> save(ReactiveBeverage beverage) {
        Log.info("Persisting reactive beverage");
        var entity = new ReactiveBeverageEntity(beverage.name());
        return persist(entity).replaceWith(beverage);
    }

    Uni<List<ReactiveBeverage>> save(List<ReactiveBeverage> beverages) {
        Log.info("Persisting reactive beverages");
        var entities = beverages.stream()
                .map(beverage -> new ReactiveBeverageEntity(beverage.name()))
                .toList();
        return persist(entities).replaceWith(beverages);
    }
}
