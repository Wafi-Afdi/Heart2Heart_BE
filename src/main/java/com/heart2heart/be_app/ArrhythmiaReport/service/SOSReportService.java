package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.ArrhythmiaReport.repository.ArrhythmiaReportRepo;
import com.heart2heart.be_app.ecgextraction.repository.ECGSignalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SOSReportService {
    private final ArrhythmiaReportRepo arrhythmiaReportRepo;

    @Autowired
    public SOSReportService(ArrhythmiaReportRepo _arrhythmiaReportRepo) {
        this.arrhythmiaReportRepo = _arrhythmiaReportRepo;
    }

}
