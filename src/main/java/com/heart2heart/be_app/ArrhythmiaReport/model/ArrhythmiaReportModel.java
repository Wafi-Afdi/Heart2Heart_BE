package com.heart2heart.be_app.ArrhythmiaReport.model;

import com.heart2heart.be_app.auth.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Data
@Entity
@Table(
        name = "report"
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArrhythmiaReportModel {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public enum ReportType {
        AFib,
        VT,
        VFib,
        SOS,
        Bradycardia,
        Tachycardia,
        Unknown,
        Asystole,
        Normal,
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "reportType")
    private ReportType reportType;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ECGSegment segment;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
