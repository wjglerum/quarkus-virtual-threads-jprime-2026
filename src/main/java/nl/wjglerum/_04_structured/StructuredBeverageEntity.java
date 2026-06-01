package nl.wjglerum._04_structured;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "beverage")
public class StructuredBeverageEntity extends PanacheEntity {

    public String name;

    public StructuredBeverageEntity() {
    }

    public StructuredBeverageEntity(String name) {
        this.name = name;
    }
}
