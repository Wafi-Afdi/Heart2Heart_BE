package com.heart2heart.be_app.ecgextraction.model;

import com.heart2heart.be_app.auth.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Data
@Entity
@Table(
        name = "ecgSignal",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_timestamp", // This is a good name for the constraint
                        columnNames = {"userId", "timestamp"}
                )
        },
        indexes = {
                @Index(name = "idx_ecgSignal_user_id", columnList = "userId")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ECGSignalModel {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    private Float signal;

    @Column(nullable = false)
    private Boolean asystole;

    @Column(nullable = false)
    private Boolean rPeak;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
