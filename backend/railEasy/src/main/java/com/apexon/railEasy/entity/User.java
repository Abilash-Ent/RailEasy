package com.apexon.railEasy.entity;

import com.apexon.railEasy.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Application user. Maps to the USERS table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private Long id;

    @Column("full_name")
    private String fullName;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("role")
    private Role role;

    @Column("created_at")
    private LocalDateTime createdAt;
}

