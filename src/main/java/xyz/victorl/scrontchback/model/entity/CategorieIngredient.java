package xyz.victorl.scrontchback.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "categorie_ingredient")
public class CategorieIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie_ingredient")
    private Integer id;

    @Column(name = "lib_categorie_ingredient", nullable = false)
    private String libCategorieIngredient;

    @Column(name = "icone_categorie")
    private String iconeCategorie;

    @OneToMany(mappedBy = "categorieIngredient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Ingredient> ingredients = new LinkedHashSet<>();
}
