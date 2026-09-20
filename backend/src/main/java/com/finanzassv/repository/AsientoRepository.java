package com.finanzassv.repository;

import com.finanzassv.entity.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    Optional<Asiento> findFirstByOrderByNumeroAsientoDesc();

    boolean existsByEstado(com.finanzassv.enums.EstadoAsiento estado);
}
