function selectRole(role) {
    setRole(role);
    const token = localStorage.getItem("token");
  
    if (role === "admin") {
      if (token) {
        window.location.href = `/adminDashboard/${token}`;
      } else {
        openModal('adminLogin');  // abre modal de login
      }
    } else if (role === "patient") {
      window.location.href = "/pages/patientDashboard.html";
    } else if (role === "doctor") {
      if (token) {
        window.location.href = `/doctorDashboard/${token}`;
      } else {
        openModal('doctorLogin');  // abre modal de login
      }
    } else if (role === "loggedPatient") {
      window.location.href = "/pages/loggedPatientDashboard.html";
    }
  }

  function renderContent() {
    const role = getRole();
    if (!role) {
      window.location.href = "/";
      return;
    }
  }
  
  
  window.renderContent = renderContent;
  window.selectRole = selectRole;