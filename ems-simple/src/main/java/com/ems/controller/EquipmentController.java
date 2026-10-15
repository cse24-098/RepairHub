package com.ems.controller;

import com.ems.dao.EquipmentDao;
import com.ems.dao.LookupDao;
import com.ems.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EquipmentController {

    private static final List<String> CONDITIONS = List.of("NEW", "GOOD", "FAIR", "POOR");

    private final EquipmentDao equipmentDao;
    private final LookupDao lookupDao;

    public EquipmentController(EquipmentDao equipmentDao, LookupDao lookupDao) {
        this.equipmentDao = equipmentDao;
        this.lookupDao = lookupDao;
    }

    /** Equipment list with search (RQ09). Everybody who is logged in can see it. */
    @GetMapping("/equipment")
    public String list(@RequestParam(required = false) String q,
                       @SessionAttribute("user") User user, Model model) {
        model.addAttribute("equipmentList", equipmentDao.findBySchool(user.getSchoolId(), q));
        model.addAttribute("q", q);
        return "equipment/list";
    }

    /** Empty registration form (staff only, checked in LoginInterceptor). */
    @GetMapping("/equipment/new")
    public String newForm(@SessionAttribute("user") User user, Model model) {
        return showForm(user, model, new HashMap<>(), null);
    }

    /** "form" holds everything the user typed: field name -> value. */
    @PostMapping("/equipment/save")
    public String save(@RequestParam Map<String, String> form,
                       @SessionAttribute("user") User user,
                       Model model, RedirectAttributes redirect) {

        String assetTag = form.getOrDefault("assetTag", "").trim();
        String name = form.getOrDefault("name", "").trim();
        String categoryId = form.getOrDefault("categoryId", "");
        String locationId = form.getOrDefault("locationId", "");
        String condition = form.getOrDefault("conditionState", "GOOD");

        String error = null;
        if (assetTag.isEmpty() || name.isEmpty() || categoryId.isEmpty()) {
            error = "Asset tag, name and category are required.";
        } else if (!CONDITIONS.contains(condition)) {
            error = "Invalid condition.";
        } else if (equipmentDao.assetTagExists(assetTag)) {
            error = "This asset tag is already in use.";
        }

        if (error != null) {
            return showForm(user, model, form, error);     // show the form again with the message
        }

        equipmentDao.add(user.getSchoolId(), Long.parseLong(categoryId),
                locationId.isEmpty() ? null : Long.parseLong(locationId),
                assetTag, name, form.get("serialNumber"), condition);

        redirect.addFlashAttribute("success", "Equipment registered.");
        return "redirect:/equipment";
    }

    private String showForm(User user, Model model, Map<String, String> form, String error) {
        model.addAttribute("form", form);
        model.addAttribute("error", error);
        model.addAttribute("categories", lookupDao.categories());
        model.addAttribute("locations", lookupDao.locations(user.getSchoolId()));
        model.addAttribute("conditions", CONDITIONS);
        return "equipment/form";
    }
}
