package com.heart2heart.be_app.ecgextraction.repository;

import com.heart2heart.be_app.ecgextraction.model.ECGSignalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ECGSignalRepository extends JpaRepository<ECGSignalModel, UUID> {
    List<ECGSignalModel> findByUserIdOrderByTimestampAsc(UUID userId);

    @Query("SELECT e FROM ECGSignalModel e WHERE e.user.id = :userId AND e.timestamp BETWEEN :start AND :end ORDER BY e.timestamp ASC")
    Optional<List<ECGSignalModel>> findECGRecordsWithRangeTime(
            @Param("userId") UUID userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT * FROM ecg_signal e " +
            "WHERE e.user_id = :userId " +
            "AND e.timestamp >= " +
            "  (SELECT MAX(latest.timestamp) FROM ecg_signal latest WHERE latest.user_id = :userId) " +
            "  - (:seconds * INTERVAL '1' SECOND) " + // PostgreSQL/H2 compatible subtraction
            "ORDER BY e.timestamp ASC",
            nativeQuery = true)
    Optional<List<ECGSignalModel>> findLatestCustomSecondsOfDataByUserId(
            @Param("userId") UUID userId,
            @Param("seconds") int seconds
    );
}
