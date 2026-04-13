package Repositories;

import Models.Huilerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuilerieRepository extends JpaRepository<Huilerie, Long> {
    Optional<Huilerie> findByNom(String nom);
    Optional<Huilerie> findByNomAndEntreprise_IdEntreprise(String nom, Long entrepriseId);
    Optional<Huilerie> findFirstByEntreprise_IdEntrepriseIsNotNullOrderByIdHuilerieAsc();
    List<Huilerie> findByEntreprise_IdEntreprise(Long entrepriseId);
    boolean existsByIdHuilerieAndEntreprise_IdEntreprise(Long idHuilerie, Long entrepriseId);
    boolean existsByNom(String nom);
}
