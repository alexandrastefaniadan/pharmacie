package ma.pharmacie.lookup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Pharmaceutical form (forme galénique): tablet, syrup, cream, ovule, ... */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pharmaceutical_form")
public class PharmaceuticalForm extends AbstractLookup {
}

