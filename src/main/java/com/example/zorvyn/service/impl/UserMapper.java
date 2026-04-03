package com.example.zorvyn.service.impl;

import com.example.zorvyn.dto.response.UserResponse;
import com.example.zorvyn.model.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles",
            expression = "java(user.getRoles().stream()" +
                    ".map(r -> r.getName().name())" +
                    ".collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "status",
            expression = "java(user.getStatus().name())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
