package com.example.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "applicants_info", schema = "staging")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "first_name")
    @NotBlank(message = "firstName cannot be blank")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message = "lastName cannot be blank")
    private String lastName;

    @Column(name = "email_id", nullable = false, unique = true)
    @Email(message = "email format is invalid")
    @NotBlank(message = "email cannot be blank")
    private String email;
}
