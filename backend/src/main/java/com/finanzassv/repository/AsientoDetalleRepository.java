package com.finanzassv.repository;

import com.finanzassv.entity.AsientoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AsientoDetalleRepository extends JpaRepository<AsientoDetalle, Long> {

    List<AsientoDetalle> findByAsientoIdOrderByOrdenLineaAsc(Long asientoId);

    List<AsientoDetalle> findByCuentaId(Long cuentaId);

    List<AsientoDetalle> findByCuentaIdIn(Collection<Long> cuentaIds);
}
