package nl.wjglerum._03_virtual;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "beverage")
public class VirtualBeverageEntity extends PanacheEntity {

    public String name;

    public VirtualBeverageEntity() {
    }

    public VirtualBeverageEntity(String name) {
        this.name = name;
    }
}
