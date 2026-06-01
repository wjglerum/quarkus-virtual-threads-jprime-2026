package nl.wjglerum._01_blocking;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class BlockingBeverageRepository implements PanacheRepository<BlockingBeverageEntity> {

    void save(BlockingBeverage beverage) {
        Log.info("Persisting blocking beverage");
        var entity = new BlockingBeverageEntity(beverage.name());
        persist(entity);
    }

    void save(List<BlockingBeverage> beverages) {
        Log.info("Persisting blocking beverages");
        var entities = beverages.stream()
                .map(beverage -> new BlockingBeverageEntity(beverage.name()))
                .toList();
        persist(entities);
    }
}
