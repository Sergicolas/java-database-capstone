package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // Get available time slots for a doctor on a given date
    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Appointment> booked = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        Set<String> bookedTimes = booked.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toSet());

        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) return new ArrayList<>();

        List<String> available = doctorOpt.get().getAvailableTimes();
        return available.stream()
                .filter(t -> !bookedTimes.contains(t))
                .collect(Collectors.toList());
    }

    // Save a new doctor
    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) return -1;
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            return 0;
        }
    }

    // Update an existing doctor
    public int updateDoctor(Doctor doctor) {
        try {
            Optional<Doctor> existing = doctorRepository.findById(doctor.getId());
            if (existing.isEmpty()) return -1;
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return 0;
        }
    }

    // Get all doctors
    @Transactional
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    // Delete a doctor and their appointments
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> existing = doctorRepository.findById(id);
            if (existing.isEmpty()) return -1;
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            return 0;
        }
    }

    // Validate doctor login
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());

        if (doctor == null) {
            response.put("message", "Doctor not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!doctor.getPassword().equals(login.getPassword())) {
            response.put("message", "Invalid credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = tokenService.generateToken(doctor.getEmail(), "doctor");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // Find doctors by partial name
    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        result.put("doctors", doctors);
        return result;
    }

    // Filter by name, specialty, and time
    @Transactional
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    // Filter by name and time
    @Transactional
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    // Filter by name and specialty
    @Transactional
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        result.put("doctors", doctors);
        return result;
    }

    // Filter by specialty and time
    @Transactional
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    // Filter by specialty only
    @Transactional
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        result.put("doctors", doctors);
        return result;
    }

    // Filter all doctors by time
    @Transactional
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> all = doctorRepository.findAll();
        result.put("doctors", filterDoctorByTime(all, amOrPm));
        return result;
    }

    // Private helper: filter doctors list by AM/PM
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
                .filter(doctor -> {
                    List<String> times = doctor.getAvailableTimes();
                    if (times == null) return false;
                    return times.stream().anyMatch(t -> {
                        try {
                            LocalTime time = LocalTime.parse(t);
                            if ("AM".equalsIgnoreCase(amOrPm)) {
                                return time.getHour() < 12;
                            } else if ("PM".equalsIgnoreCase(amOrPm)) {
                                return time.getHour() >= 12;
                            }
                        } catch (Exception e) {
                            return false;
                        }
                        return false;
                    });
                })
                .collect(Collectors.toList());
    }
}