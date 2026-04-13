// adminDashboard.js - Admin Dashboard Logic

import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// Attach click listener to "Add Doctor" button
document.getElementById("addDocBtn").addEventListener("click", () => {
  openModal("addDoctor");
});

// Load doctor cards when DOM is ready
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});

// Fetch all doctors and display them as cards
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    doctors.forEach((doctor) => {
      const card = createDoctorCard(doctor);
      contentDiv.appendChild(card);
    });
  } catch (error) {
    console.error("Error loading doctor cards:", error);
  }
}

// Event listeners for search and filters
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);

// Filter doctors based on name, time, and specialty
async function filterDoctorsOnChange() {
  try {
    const name = document.getElementById("searchBar").value.trim() || null;
    const time = document.getElementById("filterTime").value || null;
    const specialty = document.getElementById("filterSpecialty").value || null;

    const data = await filterDoctors(name, time, specialty);
    const doctors = data.doctors || [];

    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    if (doctors.length === 0) {
      contentDiv.innerHTML = `<p class="noPatientRecord">No doctors found with the given filters.</p>`;
      return;
    }

    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Something went wrong while filtering doctors.");
  }
}

// Helper: render a list of doctor cards
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// Collect form data and add a new doctor
window.adminAddDoctor = async function () {
  const name      = document.getElementById("doctorName").value.trim();
  const email     = document.getElementById("doctorEmail").value.trim();
  const phone     = document.getElementById("doctorPhone").value.trim();
  const password  = document.getElementById("doctorPassword").value.trim();
  const specialty = document.getElementById("doctorSpecialty").value.trim();

  // Collect checked availability times
  const checkboxes = document.querySelectorAll("input[name='availableTime']:checked");
  const availableTimes = Array.from(checkboxes).map((cb) => cb.value);

  const token = localStorage.getItem("token");
  if (!token) {
    alert("Authentication token not found. Please log in again.");
    return;
  }

  const doctor = { name, email, phone, password, specialty, availableTimes };

  try {
    const result = await saveDoctor(doctor, token);

    if (result.success) {
      alert(result.message || "Doctor added successfully!");
      document.getElementById("modal").style.display = "none";
      loadDoctorCards();
    } else {
      alert(result.message || "Failed to add doctor.");
    }
  } catch (error) {
    console.error("Error adding doctor:", error);
    alert("An error occurred while adding the doctor.");
  }
};