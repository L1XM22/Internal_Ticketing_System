# Internal Ticketing System (INTER SUPPORT)

A desktop-based IT support ticketing and resolution platform developed using **Compose Multiplatform (Kotlin)**, **MS SQL Server**, and the **JavaMail API**. The application serves as an internal ticketing tool for submitting tech support queries, tracking issue resolution, and managing configurations via an Admin Dashboard.


(Built With AI ASSISTANCE)
---

## 🚀 Key Features

- **User Ticketing Portal**: 
  - Submit new tickets with detailed descriptions and optional screenshot attachments.
  - Track existing tickets in real time using your unique Ticket ID.
- **Admin Dashboard**: 
  - Manage ticket lifecycles (Open $\rightarrow$ In Progress $\rightarrow$ Closed).
  - Assign ticket priority levels (e.g., Low, Medium, High).
  - Manage employees and technician assignments.
  - Directly configure SMTP settings and Email Template layouts for automated alerts.
- **Automatic Database Setup**: Creates and updates tables automatically on startup.
- **Automated Email Notifications**: Uses SMTP to send mail templates to users/admins on ticket submission and resolution.

---

## 🛠️ Technology Stack

- **Framework**: Compose Multiplatform (Compose for Desktop)
- **Language**: Kotlin 1.9.22
- **Database**: Microsoft SQL Server (connected via JDBC driver)
- **Email Dispatcher**: JavaMail API for SMTP delivery
- **Build System**: Gradle Kotlin DSL

---

## 📦 Directory Structure

```
INTER SUPPORT/
├── src/
│   └── com/intersupport/ticketingsystem/
│       ├── Main.kt                # Application entry point & DB connection check
│       ├── MainActivity.kt        # Component initializer
│       ├── AppNavigation.kt       # Screen routing & state management
│       ├── data/                  # Repositories & database connector
│       │   ├── DatabaseConnector.kt
│       │   ├── EmailRepository.kt
│       │   ├── TicketRepository.kt
│       │   └── UserRepository.kt
│       └── ui/                    # Presentation UI layer & theme
│           ├── theme/             # Styling & InterSupport Color Palette
│           └── screens/           # SubmitTicket, TicketStatus, AdminDashboard, AdminLogin
├── build.gradle.kts               # Dependency & package config
└── settings.gradle.kts            # Project settings
```

---

## 💾 Database Setup

The application connects to a Microsoft SQL Server database. By default, it expects:
* **Server Address**: `##########\SQLEXPRESS`
* **Database Name**: `##########`
* **Username**: `##`
* **Password**: `##########`

Connection settings are defined inside `DatabaseConnector.kt`.

### Automatic Schema Generation
On start, the application tests the connection and executes DDL scripts to verify/create the following schema:
- **`dbo.TicketEmployees`**: User credentials, roles, and emails.
- **`dbo.Tickets`**: Active and archived IT requests.
- **`dbo.TicketAttachments`**: Paths to file attachments/screenshots.
- **`dbo.SmtpSettings`**: Config storage for the mail server integration.
- **`dbo.EmailTemplates`**: Layout templates (e.g. `NewTicket`, `TicketClosed`).

---

## ⚙️ SMTP & Mail Configuration

For automated notifications to work:
1. Log in to the **Admin Dashboard** (using an administrator account in `dbo.TicketEmployees`).
2. Navigate to the **Email & System Settings** tab.
3. Configure the following properties:
   - **Host** (e.g., `smtp.gmail.com` or your internal SMTP relay)
   - **Port** (e.g., `587` or `465`)
   - **User** / **Password**
   - **Sender & Default Recipient Email**
   - **Attachment Storage Path** (Network directory path where attachments should be archived/saved)

---

## 🏁 Getting Started

### Prerequisites
- JDK 17 or higher
- Access to the target SQL Server instance

### Run the Application locally
Use the Gradle wrapper to build and execute the application:
```bash
./gradlew run
```

### Build Executables/Installers
To generate a standalone native Windows installer (`.msi` format):
```bash
./gradlew packageMsi
```
The output package will be placed inside `build/compose/binaries/main/msi/`.
