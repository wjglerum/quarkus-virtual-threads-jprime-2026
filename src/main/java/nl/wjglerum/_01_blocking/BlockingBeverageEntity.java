package nl.wjglerum._01_blocking;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "beverage")
public class BlockingBeverageEntity extends PanacheEntity {

    public String name;

    public BlockingBeverageEntity() {
    }

    public BlockingBeverageEntity(String name) {
        this.name = name;
    }
}
