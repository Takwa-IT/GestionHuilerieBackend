package Repositories;

import Models.Huilerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HuilerieRepository extends JpaRepository<Huilerie, Long> {
}