package com.vit.parkingappeal.repository;

import com.vit.parkingappeal.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViolationRepository extends JpaRepository<Violation, Long> {

    Optional<Violation> findByTicketNumber(String ticketNumber);

    boolean existsByTicketNumber(String ticketNumber);
}
