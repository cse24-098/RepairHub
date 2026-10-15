package com.ems.repository;

import com.ems.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findBySchoolIdOrderByNameAsc(Long schoolId);

    Optional<Equipment> findByIdAndSchoolId(Long id, Long schoolId);

    boolean existsByAssetTag(String assetTag);

    @Query("""
           select e from Equipment e
           where e.school.id = :schoolId
             and (lower(e.name) like lower(concat('%', :q, '%'))
               or lower(e.assetTag) like lower(concat('%', :q, '%')))
           order by e.name
           """)
    List<Equipment> search(@Param("schoolId") Long schoolId, @Param("q") String q);
}
