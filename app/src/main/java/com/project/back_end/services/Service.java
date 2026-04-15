package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // Validate token for a given user role
    public Map<String, Object> validateToken(String token, String user) {
        Map<String, Object> result = new HashMap<>();
        boolean valid = tokenService.validateToken(token, user);
        if (!valid) {
            result.put("message", "Invalid or expired token.");
        }
        return result;
    }

    // Validate admin login credentials
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null) {
                response.put("message", "Admin not found.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = tokenService.generateToken(admin.getUsername(), "admin");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error validating admin: " + e.getMessage());
            response.put("message", "Internal server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Filter doctors by name, specialty, and time
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName      = name != null && !name.equals("null") && !name.isBlank();
        boolean hasSpecialty = specialty != null && !specialty.equals("null") && !specialty.isBlank();
        boolean hasTime      = time != null && !time.equals("null") && !time.isBlank();

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("doctors", doctorService.getDoctors());
            return result;
        }
    }

    // Validate appointment time against doctor availability
    public Map<String, Object> validateAppointment(Appointment appointment) {
        Map<String, Object> result = new HashMap<>();

        Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
        if (doctorOpt.isEmpty()) {
            result.put("message", "Doctor not found.");
            return result;
        }

        LocalDate date = appointment.getAppointmentTime().toLocalDate();
        List<String> available = doctorService.getDoctorAvailability(
                appointment.getDoctor().getId(), date);

        LocalTime requestedTime = appointment.getAppointmentTime().toLocalTime();
        boolean isAvailable = available.stream().anyMatch(t -> {
            try {
                return LocalTime.parse(t).equals(requestedTime);
            } catch (Exception e) {
                return false;
            }
        });

        if (!isAvailable) {
            result.put("message", "Appointment time is not available.");
        }

        return result;
    }

    // Check if patient already exists
    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(
                patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    // Validate patient login
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getEmail());
            if (patient == null) {
                response.put("message", "Patient not found.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = tokenService.generateToken(patient.getEmail(), "patient");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error validating patient login: " + e.getMessage());
            response.put("message", "Internal server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Filter patient appointments
    public ResponseEntity<Map<String, Object>> filterPatient(String condition,
                                                              String name,
                                                              String token) {
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Long patientId = patient.getId();
        boolean hasCondition = condition != null && !condition.equals("null") && !condition.isBlank();
        boolean hasName      = name != null && !name.equals("null") && !name.isBlank();

        if (hasCondition && hasName) {
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        } else if (hasCondition) {
            return patientService.filterByCondition(condition, patientId);
        } else if (hasName) {
            return patientService.filterByDoctor(name, patientId);
        } else {
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}