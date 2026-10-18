package com.vit.parkingappeal.repository;

import com.vit.parkingappeal.model.Appeal;
import com.vit.parkingappeal.model.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, Long> {

    Optional<Appeal> findByAppealRef(String appealRef);

    boolean existsByAppealRef(String appealRef);

    /** Used to block a second appeal while one is still open for the same ticket. */
    boolean existsByViolationTicketNumberAndStatusIn(String ticketNumber, Collection<AppealStatus> statuses);

    List<Appeal> findAllByOrderBySubmittedAtDesc();

    List<Appeal> findByStatusOrderBySubmittedAtDesc(AppealStatus status);

    long countByStatus(AppealStatus status);
}
