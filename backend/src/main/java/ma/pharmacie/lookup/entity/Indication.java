package ma.pharmacie.lookup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Indication / usage: toux sèche, fièvre, cicatrisation, ... */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "indication")
public class Indication extends AbstractLookup {
}

