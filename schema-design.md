# 🗄️ Smart Clinic Management System — Database Schema Design

---

## MySQL Database Design

### Table: patients
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(150), Not Null, Unique
- password: VARCHAR(255), Not Null
- date_of_birth: DATE, Not Null
- phone: VARCHAR(20)
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP

> Patients are the core users of the system. Email must be unique to avoid duplicate accounts.

---

### Table: doctors
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(150), Not Null, Unique
- password: VARCHAR(255), Not Null
- specialty: VARCHAR(100), Not Null
- phone: VARCHAR(20)
- available: BOOLEAN, Default TRUE
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP

> The `available` flag allows admins to deactivate a doctor without deleting their record or appointment history.

---

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id), On Delete CASCADE
- patient_id: INT, Foreign Key → patients(id), On Delete CASCADE
- appointment_date: DATE, Not Null
- appointment_time: TIME, Not Null
- status: ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED'), Default 'SCHEDULED'
- notes: TEXT
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP

> Cascading deletes ensure no orphaned appointments remain if a patient or doctor is removed. ENUM restricts status to valid values only. Overlapping appointments per doctor should be validated at the service layer.

---

### Table: admin
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(150), Not Null, Unique
- password: VARCHAR(255), Not Null
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP

> Admins manage system access and clinic operations. Stored separately from patients and doctors for role clarity.

---

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "appointmentId": 101,
  "patientId": 12,
  "doctorId": 5,
  "issuedAt": "2025-04-13T10:30:00Z",
  "diagnosis": "Seasonal allergic rhinitis",
  "medications": [
    {
      "name": "Loratadine",
      "dosage": "10mg",
      "frequency": "Once daily",
      "duration": "7 days"
    },
    {
      "name": "Fluticasone nasal spray",
      "dosage": "50mcg per nostril",
      "frequency": "Twice daily",
      "duration": "14 days"
    }
  ],
  "refillCount": 1,
  "pharmacy": {
    "name": "City Pharmacy",
    "location": "Downtown Clinic Street"
  },
  "doctorNotes": "Avoid exposure to pollen. Return if symptoms worsen after 7 days.",
  "followUpDate": "2025-04-27"
}
```

> Prescriptions are stored in MongoDB because their structure varies between specialties and visits. The `medications` array allows multiple drugs per document without join tables. IDs reference MySQL records without duplicating relational data. The flexible schema supports future additions like lab results or file attachments without migrations.

---

## Design Decisions Summary

| Decision | Justification |
|----------|--------------|
| MySQL for core entities | Structured data with strict relationships and constraints |
| MongoDB for prescriptions | Variable structure with nested arrays suits a document model |
| CASCADE on foreign keys | Deleting a patient or doctor removes related appointments automatically |
| ENUM for appointment status | Prevents invalid status values at the database level |
| `available` flag for doctors | Deactivates doctors without losing historical data |
| IDs only in MongoDB docs | Avoids data duplication — relational data stays in MySQL |

---

> 💡 *This hybrid approach leverages relational integrity for core clinic operations and document flexibility for evolving clinical records.*