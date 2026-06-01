package nl.wjglerum._03_virtual;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class VirtualBeverageRepository implements PanacheRepository<VirtualBeverageEntity> {

    void save(VirtualBeverage beverage) {
        Log.info("Persisting virtual beverage");
        var entity = new VirtualBeverageEntity(beverage.name());
        this.persist(entity);
    }

    void save(List<VirtualBeverage> beverages) {
        Log.info("Persisting virtual beverages");
        var entities = beverages.stream()
                .map(beverage -> new VirtualBeverageEntity(beverage.name()))
                .toList();
        this.persist(entities);
    }
}
