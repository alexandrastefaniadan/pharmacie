package ma.pharmacie.lookup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Therapeutic class: antibiotique, antalgique, vitamine, ... */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "therapeutic_class")
public class TherapeuticClass extends AbstractLookup {
}

