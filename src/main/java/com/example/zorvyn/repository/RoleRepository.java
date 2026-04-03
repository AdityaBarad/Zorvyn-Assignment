package com.example.zorvyn.repository;

import com.example.zorvyn.model.entity.Role;
import com.example.zorvyn.model.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}

