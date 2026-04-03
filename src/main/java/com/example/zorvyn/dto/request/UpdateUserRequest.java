package com.example.zorvyn.dto.request;

import com.example.zorvyn.model.enums.RoleName;
import com.example.zorvyn.model.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100)
    private String fullName;

    private UserStatus status;

    private RoleName roleName;
}

