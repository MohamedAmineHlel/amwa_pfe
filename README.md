# Work Monitoring and Task Management Platform

## Overview
This platform is designed to facilitate work monitoring and task management within an organization. It provides different access levels for users based on their roles, allowing them to create, update, manage, and track tasks efficiently. The system includes notifications, priority settings, and reporting features to enhance productivity and collaboration.

## Technologies Used
- **Backend:** Spring Boot (Java)
- **Frontend:** Angular
- **Database:** PostgreSQL / MySQL
- **Authentication:** Spring Security + JWT
- **Notifications:** WebSockets / Email Notifications
- **File Management:** Spring Boot file upload

## Features
### User Roles & Permissions
1. **Operator:**
   - Get assigned tasks
   - Indicate task status (Started, Ongoing, Finished, Delayed)
   - Add comments
2. **Supervisor:**
   - Create, update, and get tasks
   - Assign tasks to operators
   - Indicate own task status
   - Add comments
3. **Manager:**
   - Create, update, and get tasks
   - Assign tasks to operators and supervisors
   - Indicate any task status
   - Add comments
   - Update team members
4. **Super Manager:**
   - View work status of all users
   - Create and assign tasks

### Task Management
- Supervisors and managers can create and assign tasks.
- Users can create their own tasks.
- Tasks can have priority levels (High/Medium/Low).
- Attachments (Excel files) can be uploaded to tasks.
- Status updates: **Dispatched, Started, Ongoing, Finished, Delayed**.

### Notifications
- Reminders when the deadline is approaching (1 day before).
- Notifications when a task is completed.
- Notifications when a comment is added.

### Work Status & Reporting
- Supervisors and managers can view task progress per user.
- Extract reports showing user work status (tasks finished, delayed, in progress).

### Additional Features
- Users can request:
  - A change of the target date (Approval required from Supervisor/Manager).
  - Supervisor’s help on a task.

## Installation Guide
### Prerequisites
- **Java 17+**
- **Node.js 16+**
- **Angular CLI**
- **Spring Boot & Maven**
- **PostgreSQL/MySQL**