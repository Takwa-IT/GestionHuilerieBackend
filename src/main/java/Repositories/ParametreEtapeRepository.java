package Repositories;

import Models.ParametreEtape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametreEtapeRepository extends JpaRepository<ParametreEtape, Long> {
}


