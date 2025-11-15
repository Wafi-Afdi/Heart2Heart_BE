package com.heart2heart.be_app.ArrhythmiaReport.repository;

import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArrhythmiaReportRepo extends JpaRepository<ArrhythmiaReportModel, UUID> {
    @Query("SELECT r FROM ArrhythmiaReportModel r WHERE r.user.id = :userId ORDER BY r.timestamp DESC")
    Optional<List<ArrhythmiaReportModel>> getReportByUserId(@Param("userId") UUID userId);

    @Query(
            value = "SELECT * FROM report r WHERE r.id = :reportId LIMIT 1",
            nativeQuery = true
    )
    Optional<ArrhythmiaReportModel> getReportById(@Param("reportId") UUID reportId);
}
