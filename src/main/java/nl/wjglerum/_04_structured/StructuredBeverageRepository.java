package nl.wjglerum._04_structured;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StructuredBeverageRepository implements PanacheRepository<StructuredBeverageEntity> {

    void save(StructuredBeverage beverage) {
        Log.info("Persisting structured beverage");
        persist(new StructuredBeverageEntity(beverage.name()));
    }

    void save(List<StructuredBeverage> beverages) {
        Log.info("Persisting structured beverages");
        var entities = beverages.stream()
                .map(beverage -> new StructuredBeverageEntity(beverage.name()))
                .toList();
        persist(entities);
    }
}
