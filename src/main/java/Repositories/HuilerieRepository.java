package Repositories;

import Models.Huilerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HuilerieRepository extends JpaRepository<Huilerie, Long> {
    Optional<Huilerie> findByNom(String nom);
    boolean existsByNom(String nom);
}
