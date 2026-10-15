package com.ems.service;

import com.ems.dto.EquipmentForm;
import com.ems.model.Equipment;
import com.ems.model.enums.EquipmentStatus;
import com.ems.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final SchoolRepository schoolRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, SchoolRepository schoolRepository,
                            CategoryRepository categoryRepository, LocationRepository locationRepository) {
        this.equipmentRepository = equipmentRepository;
        this.schoolRepository = schoolRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    /** List (or search) equipment, ALWAYS limited to the user's own school. */
    @Transactional(readOnly = true)
    public List<Equipment> list(Long schoolId, String query) {
        if (query == null || query.isBlank()) {
            return equipmentRepository.findBySchoolIdOrderByNameAsc(schoolId);
        }
        return equipmentRepository.search(schoolId, query.trim());
    }

    @Transactional(readOnly = true)
    public boolean assetTagExists(String assetTag) {
        return equipmentRepository.existsByAssetTag(assetTag);
    }

    @Transactional
    public Equipment create(EquipmentForm form, Long schoolId) {
        Equipment e = new Equipment();
        e.setSchool(schoolRepository.getReferenceById(schoolId));
        e.setCategory(categoryRepository.getReferenceById(form.getCategoryId()));
        if (form.getLocationId() != null) {
            e.setLocation(locationRepository.findByIdAndSchoolId(form.getLocationId(), schoolId)
                    .orElseThrow(() -> new IllegalArgumentException("Location not found")));
        }
        e.setAssetTag(form.getAssetTag().trim());
        e.setQrCodeValue(form.getAssetTag().trim());   // TODO RQ12: generate/print real QR images (ZXing)
        e.setName(form.getName().trim());
        e.setSerialNumber(form.getSerialNumber());
        e.setDescription(form.getDescription());
        e.setDateAcquired(form.getDateAcquired());
        e.setCost(form.getCost());
        e.setConditionState(form.getConditionState());
        e.setStatus(EquipmentStatus.AVAILABLE);
        return equipmentRepository.save(e);
    }
}
