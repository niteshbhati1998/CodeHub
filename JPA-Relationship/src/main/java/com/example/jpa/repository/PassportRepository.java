package com.example.jpa.repository;

import com.example.jpa.entity.o2o.Passport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassportRepository extends JpaRepository<Passport, Long> {
}
