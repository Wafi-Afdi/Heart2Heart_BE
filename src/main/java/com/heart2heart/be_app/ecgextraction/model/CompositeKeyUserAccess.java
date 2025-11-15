package com.heart2heart.be_app.ecgextraction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CompositeKeyUserAccess {
    private UUID ambulatoryUser;

    private UUID observerUser;
}
