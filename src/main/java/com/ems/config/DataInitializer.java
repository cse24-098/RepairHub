package com.ems.config;

import com.ems.model.*;
import com.ems.model.enums.ConditionState;
import com.ems.model.enums.LocationType;
import com.ems.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DEVELOPMENT ONLY: creates a demo school, users and equipment the first time the app starts
 * (only when the users table is empty). Remove or restrict with @Profile before real deployment.
 * Demo password for every account: password123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final SchoolRepository schools;
    private final RoleRepository roles;
    private final UserRepository users;
    private final LocationRepository locations;
    private final CategoryRepository categories;
    private final EquipmentRepository equipmentRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(SchoolRepository schools, RoleRepository roles, UserRepository users,
                           LocationRepository locations, CategoryRepository categories,
                           EquipmentRepository equipmentRepo, PasswordEncoder encoder) {
        this.schools = schools;
        this.roles = roles;
        this.users = users;
        this.locations = locations;
        this.categories = categories;
        this.equipmentRepo = equipmentRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;

        School school = new School();
        school.setName("Demo Secondary School");
        school.setAddress("1 Example Road");
        school.setContactEmail("office@demo-school.test");
        school = schools.save(school);

        createUser(school, "TEACHER", "Tina Teacher", "teacher@school.test");
        createUser(school, "TECHNICIAN", "Tom Technician", "tech@school.test");
        createUser(school, "SCHOOL_ADMIN", "Ada Admin", "admin@school.test");

        Location lab = createLocation(school, "Science Block", "Lab 2", LocationType.LABORATORY);
        Location room = createLocation(school, "Admin Block", "Room 101", LocationType.CLASSROOM);

        createEquipment(school, "Computers", lab, "ASSET-0001", "Dell Desktop PC");
        createEquipment(school, "Projectors", room, "ASSET-0002", "Epson Projector");
        createEquipment(school, "Printers", room, "ASSET-0003", "HP LaserJet Printer");
    }

    private void createUser(School school, String roleName, String fullName, String email) {
        User u = new User();
        u.setSchool(school);
        u.setRole(roles.findByName(roleName).orElseThrow());
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode("password123"));
        users.save(u);
    }

    private Location createLocation(School school, String building, String room, LocationType type) {
        Location l = new Location();
        l.setSchool(school);
        l.setBuilding(building);
        l.setRoom(room);
        l.setLocationType(type);
        return locations.save(l);
    }

    private void createEquipment(School school, String category, Location location, String tag, String name) {
        Equipment e = new Equipment();
        e.setSchool(school);
        e.setCategory(categories.findByName(category).orElseThrow());
        e.setLocation(location);
        e.setAssetTag(tag);
        e.setQrCodeValue(tag);
        e.setName(name);
        e.setConditionState(ConditionState.GOOD);
        equipmentRepo.save(e);
    }
}
