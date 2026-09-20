package com.finanzassv.repository;

import com.finanzassv.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Cuenta> findAllByOrderByCodigoAsc();

    List<Cuenta> findByCuentaPadreIdOrderByCodigoAsc(Long cuentaPadreId);

    boolean existsByCuentaPadreId(Long cuentaPadreId);

    List<Cuenta> findByAceptaMovimientosTrueAndActivoTrueOrderByCodigoAsc();
}
