package com.ems.repository;

import com.ems.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findBySchoolIdOrderByBuildingAscRoomAsc(Long schoolId);
    Optional<Location> findByIdAndSchoolId(Long id, Long schoolId);
}
