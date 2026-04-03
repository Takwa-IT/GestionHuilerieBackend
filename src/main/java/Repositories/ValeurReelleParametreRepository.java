package Repositories;

import Models.ValeurReelleParametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValeurReelleParametreRepository extends JpaRepository<ValeurReelleParametre, Long> {
}