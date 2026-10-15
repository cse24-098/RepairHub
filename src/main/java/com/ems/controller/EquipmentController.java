package com.ems.controller;

import com.ems.dto.EquipmentForm;
import com.ems.model.enums.ConditionState;
import com.ems.repository.CategoryRepository;
import com.ems.repository.LocationRepository;
import com.ems.security.AppUserDetails;
import com.ems.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public EquipmentController(EquipmentService equipmentService, CategoryRepository categoryRepository,
                               LocationRepository locationRepository) {
        this.equipmentService = equipmentService;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    /** List + search (RQ09). Available to every logged-in user. */
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("equipmentList", equipmentService.list(user.getSchoolId(), q));
        model.addAttribute("q", q);
        return "equipment/list";
    }

    /** Registration form (staff only, see SecurityConfig). */
    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("equipmentForm", new EquipmentForm());
        addFormData(model, user);
        return "equipment/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("equipmentForm") EquipmentForm form,
                       BindingResult result,
                       @AuthenticationPrincipal AppUserDetails user,
                       Model model, RedirectAttributes redirect) {
        if (form.getAssetTag() != null && equipmentService.assetTagExists(form.getAssetTag().trim())) {
            result.rejectValue("assetTag", "duplicate", "This asset tag is already in use");
        }
        if (result.hasErrors()) {
            addFormData(model, user);
            return "equipment/form";
        }
        equipmentService.create(form, user.getSchoolId());
        redirect.addFlashAttribute("success", "Equipment registered.");
        return "redirect:/equipment";
    }

    private void addFormData(Model model, AppUserDetails user) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("locations", locationRepository.findBySchoolIdOrderByBuildingAscRoomAsc(user.getSchoolId()));
        model.addAttribute("conditions", ConditionState.values());
    }
}
