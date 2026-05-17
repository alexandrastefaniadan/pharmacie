package ma.pharmacie.lookup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Target population: nourrisson, enfant, adolescent, adulte, ... */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "age_group")
public class AgeGroup extends AbstractLookup {
}

