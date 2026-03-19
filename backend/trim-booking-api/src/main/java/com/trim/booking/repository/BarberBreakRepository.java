package com.trim.booking.repository;

import com.trim.booking.entity.BarberBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberBreakRepository extends JpaRepository<BarberBreak, Long> {
    // Business-filtered methods for multi-tenancy
    List<BarberBreak> findByBusinessIdAndBarberId(Long businessId, Long barberId);

    @Query("SELECT bb FROM BarberBreak bb WHERE bb.business.id = :businessId AND bb.barber.id = :barberId AND (bb.dayOfWeek IS NULL OR bb.dayOfWeek = :dayOfWeek)")
    List<BarberBreak> findByBusinessIdAndBarberIdForDay(@Param("businessId") Long businessId, @Param("barberId") Long barberId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT bb FROM BarberBreak bb WHERE bb.id = :breakId AND bb.barber.business.id = :businessId")
    Optional<BarberBreak> findByIdAndBusinessId(@Param("breakId") Long breakId, @Param("businessId") Long businessId);

    @Modifying
    @Query("DELETE FROM BarberBreak bb WHERE bb.id = :breakId AND bb.barber.business.id = :businessId")
    int deleteByIdAndBusinessId(@Param("breakId") Long breakId, @Param("businessId") Long businessId);
}

