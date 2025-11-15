package com.heart2heart.be_app.ecgextraction.model;

import com.heart2heart.be_app.auth.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Data
@Entity
@Table(
        name = "user_access"
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CompositeKeyUserAccess.class)
public class UserAccessModel {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulatoryUser", nullable = false)
    private User ambulatoryUser;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observerUser", nullable = false)
    private User observerUser;
}
