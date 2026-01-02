# Employee Management System  

A full-stack Employee Management System built with **Spring Boot (backend)** and **Angular (frontend)**. The system supports secure role-based access for **Admin, HR, and Employee**, with authentication powered by JWT.  
## 🛠 Tech Stack
4️⃣ Official Google links to setup gmail app password for SMTP

- Enable 2-Step Verification: https://support.google.com/accounts/answer/185839?hl=en
- Create App Passwords: https://support.google.com/accounts/answer/185833?hl=en
- Gmail SMTP Settings: https://support.google.com/a/answer/176600?hl=en
- app passwords: https://support.google.com/accounts/answer/185833?hl=en

Gmail SMTP Settings
- *Frontend:* Angular, TypeScript
- *Database:* PostgreSQL


## 🛠 Tech Stack  
- *Backend:* Spring Boot
- *Frontend:* Angular, TypeScript
- *Database:* PostgreSQL  
  
## ✅ Use Cases

### Authentication & Security
- **Login** with email/password → returns **JWT access token**; **refresh token** set as HttpOnly cookie.
- **Forgot Password** → user submits email; system emails a **reset link** (token, ~15 min expiry).
- **Reset Password** → user sets a new password using the token.
- **Role-based access** (e.g., `ADMIN` guards on admin endpoints).
- **CORS** open for dev.

### Admin
- **Create user** (name, email, role, salary, optional profile pic) → sends **welcome email** with **temporary password**.
- **Edit user** (name, email, role, salary, status, profile pic).
- **Toggle user status** (`ACTIVE` ↔ `DEACTIVATED`).
- **Delete or view users** with optional filter by role/status
- **Override** employee attendance when needed.

### HR
- **Review & approve/reject employee leave requests** → sends **email notification** to employee.  
- **Monitor & track daily employee attendance logs** → view **attendance status** for all employees.  
- **View detailed employee profiles** → see **name, email, role, status, profile pic, and salary** (read-only for salary).  
- **Update employee information within HR’s permitted scope** → edit **name, email, role, profile pic**.

### Employee
- **View own profile** → see name, email, role, profile picture, and other personal details.  
- **Mark daily attendance** → record status (PRESENT / ABSENT / LEAVE).  
- **View personal attendance** → see all past attendance and approved leaves.  
- **Apply for leave** → submit leave request with start/end date and reason.  
- **View own leave requests** → track status (PENDING / APPROVED / REJECTED).

## Future Enhancements  
- Payroll Management (salary calculation + payslip download)   
- Real-Time Updates using WebSockets  
- Cloud Deployment (AWS/Docker)

## 🛠 FRONT END SETUP

##
Install recommended extensions (from Extensions Marketplace):

Angular Language Service
Angular Snippets (optional, for faster coding)
ESLint (for linting)
Prettier (for code formatting)

Summary

React/Next.js → npm run dev (because "dev" script is defined).

Angular → ng serve (or npm start, since "start" is in package.json).

So, for Angular:

Use ng serve --open (official way).

Or just npm start (shortcut).

React/Next.js → npm run build creates a .next/ or build/ folder.

Angular → npm run build creates a dist/ folder.

## Screenshots

  <img width="600" height="882" alt="Screenshot 2025-08-30 143419" src="https://github.com/user-attachments/assets/9bc023d0-8a59-414c-99dd-1fd55029b744" />
  
  <img width="600" height="882" alt="EM SS9" src="https://github.com/user-attachments/assets/160f76e7-37fd-4388-9647-a160590ad270" />
  
  <img width="600" height="894" alt="Screenshot 2025-08-30 144945" src="https://github.com/user-attachments/assets/2277396d-0fad-45a6-827d-86d5a27cfcd3" />
  
  <img width="600" height="960" alt="EM SS1" src="https://github.com/user-attachments/assets/5e362357-7553-440a-8e5b-1a380156c496" />
  
  <img width="600" height="858" alt="Screenshot 2025-08-30 142122" src="https://github.com/user-attachments/assets/a2f39966-d1a9-4f70-98b9-600ce47a82f2" />
  
  <img width="600" height="854" alt="Screenshot 2025-08-30 142206" src="https://github.com/user-attachments/assets/f58174a8-95ee-47fc-9ba4-95006f743e27" />
  
  <img width="600" height="884" alt="Screenshot 2025-08-30 142451" src="https://github.com/user-attachments/assets/05cd4bc7-26a9-4093-a9bd-23d122d17a69" />
  
  <img width="600" height="885" alt="Screenshot 2025-08-30 142339" src="https://github.com/user-attachments/assets/fb33463b-39d4-441d-85d3-3cfb89b5fa05" />
  
  <img width="600" height="884" alt="Screenshot 2025-08-30 143053" src="https://github.com/user-attachments/assets/ea324693-0b45-4428-b6d2-0735c649bc25" />

  <img width="600" height="881" alt="EM SS15" src="https://github.com/user-attachments/assets/0332a369-6ef8-4932-bbdd-57f688386f1d" />

  <img width="600" height="880" alt="EM SS18" src="https://github.com/user-attachments/assets/d6931443-159c-4f4a-9a26-8a8f68bff68f" />

  <img width="600" height="877" alt="EM SS17" src="https://github.com/user-attachments/assets/44b7de93-0cf2-4647-8167-b28631d872ea" />

  <img width="600" height="886" alt="EM SS11" src="https://github.com/user-attachments/assets/ac3247aa-89a2-45cc-b5fd-92a032659259" />

  <img width="600" height="885" alt="Screenshot 2025-08-30 163958" src="https://github.com/user-attachments/assets/5412c723-fe35-468d-8cc2-2bd808d33754" />

  <img width="600" height="882" alt="EM SS16" src="https://github.com/user-attachments/assets/50e04e20-1f16-4744-b70a-0381d024dd16" />

  <img width="600" height="893" alt="EM SS12" src="https://github.com/user-attachments/assets/6dbd97cc-0ce4-4e7c-96b3-21733459e728" />



