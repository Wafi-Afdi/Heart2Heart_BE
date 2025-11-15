package com.heart2heart.be_app.ecgextraction.dto;

import com.heart2heart.be_app.ecgextraction.model.UserAccessModel;
import lombok.Data;

import java.util.List;

@Data
public class UserContactsDTO {
    private List<ContactDTO> contactList;
}
