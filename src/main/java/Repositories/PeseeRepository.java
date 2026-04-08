package Repositories;

import Models.Pesee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeseeRepository extends JpaRepository<Pesee, Long> {
    //cherche lot par pesee par id de lot
    List<Pesee> findByLot_IdLotOrderByDatePeseeDesc(Long lotId);

    List<Pesee> findAllByOrderByDatePeseeDesc();

    Optional<Pesee> findByReference(String reference);

    boolean existsByReference(String reference);
}
