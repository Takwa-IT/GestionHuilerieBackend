package Repositories;

import Models.ProductionLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionLotRepository extends JpaRepository<ProductionLot, Models.ProductionLotId> {
    //cherche les productions faites sur un lot
    List<ProductionLot> findByLot_IdLot(Long lotId);}
