package com.example.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotNull(message = "firstName cannot be null")
    private String firstName;

    @Column(name = "last_name")
    @NotNull(message = "lastName cannot be null")
    private String lastName;

    @Column(name = "email_id")
    @Email(message = "invalid email format")
    @NotBlank(message = "email cannot be null or empty")
    private String email;

    @Min(value = 18, message = "minimum age should be 18")
    @Max(value = 35, message = "maximum age should be 35")
    private Integer age;

    @Pattern(regexp = "^\\d{10}$", message = "phone number must be 10 digits")
    private String phoneNumber;
}
