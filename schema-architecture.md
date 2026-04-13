🏥 Smart Clinic Management System — Architecture Documentation

Section 1: Architecture Summary
This Spring Boot application uses both MVC and REST controllers to serve different types of users and clients. Thymeleaf templates render server-side HTML pages for the Admin and Doctor dashboards, while REST APIs handle all other modules such as Appointments, Patient Dashboard, and Patient Records — returning JSON responses to external clients like mobile apps or frontend applications.
The application interacts with two databases:
DatabaseTechnologyUsed ForMySQLSpring Data JPAPatients, Doctors, Appointments, AdminsMongoDBSpring Data MongoDBPrescriptions (document-based)
All controllers route requests through a shared Service Layer, which centralizes business logic, applies validations, and coordinates workflows. The service layer delegates data access to repositories, which abstract the underlying database operations. The final response is either a rendered HTML page (MVC flow) or a serialized JSON object (REST flow).

Section 2: Numbered Flow of Data and Control

User Interface — The user accesses the application through a browser (AdminDashboard or DoctorDashboard) or via a REST client such as a mobile app (Appointments, PatientDashboard, PatientRecord).
Controller Layer — The request is routed to the appropriate controller: a Thymeleaf Controller for server-rendered pages, or a REST Controller for API-based interactions.
Service Layer — The controller delegates to the Service Layer, which applies business rules, validates input, and coordinates logic across multiple entities.
Repository Layer — The service calls the appropriate repository: a MySQL Repository (via Spring Data JPA) or a MongoDB Repository (via Spring Data MongoDB).
Database Access — The repository communicates directly with the database: MySQL for structured relational data, or MongoDB for flexible document-based records.
Model Binding — Retrieved data is mapped into Java model classes: JPA entities (@Entity) for MySQL, or document objects (@Document) for MongoDB.
Response — The bound models build the final output: Thymeleaf templates render HTML in MVC flows, or models are serialized into JSON and returned to the client in REST flows.



💡 This architecture follows a clean three-tier separation: Presentation, Application, and Data layers — enabling scalability, maintainability, and CI/CD compatibility.