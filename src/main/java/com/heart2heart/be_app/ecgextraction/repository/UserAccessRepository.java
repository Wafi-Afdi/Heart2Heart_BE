package com.heart2heart.be_app.ecgextraction.repository;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.ecgextraction.model.CompositeKeyUserAccess;
import com.heart2heart.be_app.ecgextraction.model.UserAccessModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccessRepository extends JpaRepository<UserAccessModel, CompositeKeyUserAccess> {
    Optional<List<UserAccessModel>> findAllByObserverUser(User observerUser);

    Optional<List<UserAccessModel>> findAllByAmbulatoryUser(User ambulatoryUser);


}
