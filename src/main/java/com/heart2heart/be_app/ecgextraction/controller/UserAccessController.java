package com.heart2heart.be_app.ecgextraction.controller;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.auth.user.repository.UserRepository;
import com.heart2heart.be_app.ecgextraction.dto.ContactDTO;
import com.heart2heart.be_app.ecgextraction.dto.UserAccessRequestDTO;
import com.heart2heart.be_app.ecgextraction.dto.UserContactsDTO;
import com.heart2heart.be_app.ecgextraction.model.CompositeKeyUserAccess;
import com.heart2heart.be_app.ecgextraction.model.UserAccessModel;
import com.heart2heart.be_app.ecgextraction.repository.UserAccessRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/access")
@CrossOrigin
public class UserAccessController {
    private final UserRepository userRepository;
    private final UserAccessRepository userAccessRepository;

    private final EntityManager entityManager;


    @Autowired
    public UserAccessController(UserAccessRepository userAccessRepository, UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.userAccessRepository = userAccessRepository;
        this.entityManager = entityManager;
    }

    @PostMapping("/user")
    public ResponseEntity<?> addUserAccess(@AuthenticationPrincipal User user, @RequestBody UserAccessRequestDTO userAccessRequestDTO) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        Optional<User> userDataOpt = userRepository.findByEmail(userAccessRequestDTO.getEmail());
        if(userDataOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User observerUser = userDataOpt.get();
        User ambulatoryProxy = entityManager.getReference(User.class, user.getId());

        UserAccessModel accessModel = new UserAccessModel();
        accessModel.setAmbulatoryUser(ambulatoryProxy);
        accessModel.setObserverUser(observerUser);
        userAccessRepository.save(accessModel);

        User userRes = new User();
        userRes.setId(observerUser.getId());
        userRes.setEmail(observerUser.getEmail());
        userRes.setRole(observerUser.getRole());
        userRes.setName(observerUser.getName());
        userRes.setPhoneNumber(observerUser.getPhoneNumber());

        return ResponseEntity.ok(userRes);
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUserAccess(@AuthenticationPrincipal User user, @RequestParam(name="userId") String userId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        User observerUser = entityManager.getReference(User.class, UUID.fromString(userId));

        CompositeKeyUserAccess compositeKey = new CompositeKeyUserAccess(user.getId(), observerUser.getId());

        userAccessRepository.deleteById(compositeKey);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUsersAccess(@AuthenticationPrincipal User user, @RequestParam(name="isAmbulatory") Boolean isAmbulatory) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        User userRef = entityManager.getReference(User.class, user.getId());
        Optional<List<UserAccessModel>> listOpt;
        if(isAmbulatory) {
            listOpt = userAccessRepository.findAllByAmbulatoryUser(userRef);
        } else {
            listOpt = userAccessRepository.findAllByObserverUser(userRef);
        }
        if (listOpt.isEmpty()) {
            UserContactsDTO userContactsDTO = new UserContactsDTO();
            userContactsDTO.setContactList(Collections.emptyList());
            return ResponseEntity.ok(userContactsDTO);
        } else {
            List<UserAccessModel> userAccessLists = listOpt.get();
            List<ContactDTO> contactListDTO = userAccessLists.stream()
                    .map(entity -> {
                        ContactDTO temp = new ContactDTO();
                        if (isAmbulatory) {
                            User observer = entity.getObserverUser();
                            temp.setEmail(observer.getEmail());
                            temp.setPhone(observer.getPhoneNumber());
                            temp.setUserId(observer.getId());
                            temp.setName(observer.getName());
                        } else {
                            User ambulatory = entity.getAmbulatoryUser();
                            temp.setEmail(ambulatory.getEmail());
                            temp.setPhone(ambulatory.getPhoneNumber());
                            temp.setUserId(ambulatory.getId());
                            temp.setName(ambulatory.getName());
                        }
                        return temp;
                    }).collect(Collectors.toList());
            UserContactsDTO userContactsDTO = new UserContactsDTO();
            userContactsDTO.setContactList(contactListDTO);
            return ResponseEntity.ok(userContactsDTO);
        }
    }
}
