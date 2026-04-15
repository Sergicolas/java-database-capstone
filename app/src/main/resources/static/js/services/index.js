// index.js - Role-Based Login Handling

import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

// Expõe as APIs globalmente para os handlers
window.ADMIN_API = API_BASE_URL + "/admin";
window.DOCTOR_API = API_BASE_URL + "/doctor/login";

// Setup button event listeners after DOM is loaded
window.onload = function () {
  const adminBtn = document.getElementById("adminLogin");
  if (adminBtn) {
    adminBtn.addEventListener("click", () => openModal("adminLogin"));
  }

  const doctorBtn = document.getElementById("doctorLogin");
  if (doctorBtn) {
    doctorBtn.addEventListener("click", () => openModal("doctorLogin"));
  }
};

// Admin Login Handler
window.adminLoginHandler = async function () {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const admin = { username, password };

  try {
    const response = await fetch(window.ADMIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(admin),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      selectRole("admin");
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Admin login error:", error);
    alert("An error occurred. Please try again.");
  }
};

// Doctor Login Handler
window.doctorLoginHandler = async function () {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  const doctor = { email, password };

  try {
    const response = await fetch(window.DOCTOR_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      selectRole("doctor");
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Doctor login error:", error);
    alert("An error occurred. Please try again.");
  }
};