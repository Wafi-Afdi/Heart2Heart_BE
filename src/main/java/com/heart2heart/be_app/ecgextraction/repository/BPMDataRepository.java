package com.heart2heart.be_app.ecgextraction.repository;

import com.heart2heart.be_app.ecgextraction.model.BPMDataModel;
import com.heart2heart.be_app.ecgextraction.model.ECGSignalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BPMDataRepository extends JpaRepository<BPMDataModel, UUID> {
    @Query("SELECT e FROM BPMDataModel e WHERE e.user.id = :userId AND e.timestamp BETWEEN :start AND :end ORDER BY e.timestamp ASC")
    Optional<List<BPMDataModel>> findBPMRecordsWithRangeTime(
            @Param("userId") UUID userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
