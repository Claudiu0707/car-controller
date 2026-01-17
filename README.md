# APEX Car Controller

## Introduction

**APEX Car Controller** is an application developed as a submission for the **Object-Oriented Programming Laboratory** project.

The application is designed as a controller that supports Bluetooth communication with a car device. It allows the configuration of the car’s **PID parameters**, where the car functions as a **line follower**, and supports multiple operation modes:
- **Driver Mode** – the user manually controls the car through the application  
- **Line Follower Mode** – the car follows a predefined path autonomously  
- **Setup Mode** – the car is ready to receive configuration instructions  

The project is built in accordance with **Object-Oriented Programming principles**.

The application supports driver authentication, allowing a registered driver to log in and operate the car. Circuits can be created by selecting the **location**, **city**, **circuit name**, **circuit type**, **segment difficulty**, etc.

Via a **Python API**, application data is further processed and stored in a **PostgreSQL database**, including driver data, car configuration data, race session data, circuit data, and related information.

The project will be further improved and refined in future iterations. This documentation provides a thorough overview and tutorial for using the application.
## Installation and Setup

This application targets **Android 16.0 (Baklava)** with **API level 36**.

### Project Setup

Clone the repository and open it in **Android Studio**:

```bash
git clone https://github.com/Claudiu0707/apex-car-controller
```
**Database Configuration**

A PostgreSQL database is required for data persistence. The database schema must match the one described later in this documentation. The Python API must be configured according to the instructions in the repository below:
```bash
git clone https://github.com/Claudiu0707/android-postgres-api
```

**Local Database Connection**

The default localhost address must be replaced with the IP address of the device hosting the Python API. Android applications cannot directly access the host machine’s localhost. The following configuration files must be updated with your hosting device ip

<td>https://github.com/Claudiu0707/apex-car-controller/blob/fb129fea5a48b653427130c876deee98f86afb97/app/src/main/java/com/example/carcontroller/api_package/ApiClient.java#L16-L17</td>
<td>https://github.com/Claudiu0707/apex-car-controller/blob/fb129fea5a48b653427130c876deee98f86afb97/app/src/main/java/com/example/carcontroller/api_package/CarConfigurationClient.java#L11-L11</td>


