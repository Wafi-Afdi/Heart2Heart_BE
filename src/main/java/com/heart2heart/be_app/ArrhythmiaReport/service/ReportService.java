package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.ArrhythmiaReport.repository.ArrhythmiaReportRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportService {
    private final ArrhythmiaReportRepo arrhythmiaReportRepo;

    @Autowired
    public ReportService(ArrhythmiaReportRepo arrhythmiaReportRepo) {
        this.arrhythmiaReportRepo = arrhythmiaReportRepo;
    }

    @Transactional
    public UUID saveNewReport(ArrhythmiaReportModel entity) {
        ArrhythmiaReportModel savedTask = arrhythmiaReportRepo.save(entity);

        return savedTask.getId();
    }
}
