package nl.wjglerum._02_reactive;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beverage")
public class ReactiveBeverageEntity {

    @Id
    @GeneratedValue
    public Long id;

    public String name;

    public ReactiveBeverageEntity() {
    }

    public ReactiveBeverageEntity(String name) {
        this.name = name;
    }
}
