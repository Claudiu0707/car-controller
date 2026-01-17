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

It is very important that these lines are modified accordingly, otherwise the connection to the API will fail silently and no data will be saved. Also, the URL must provide 8000 as the port.

**Car and Checkpoints devices**

For the car device, follow the instructions provided in the documentation available in the readme.md from the [car project](https://github.com/Claudiu0707/zamfirel-2) to construct and program it. Same thing is valid for the [checkpoint devices](https://github.com/Claudiu0707/checkpoint-example)

## Main Structure

This chapter focuses on the main **Object-Oriented Programming (OOP)**–related classes. It explains the design rationale behind them, how they are structured, and the roles they fulfill within the application.

### Device (Abstract Class)

The `Device` class represents a generic Bluetooth-enabled device within the application. It serves as an abstract base class for all concrete device implementations and defines the common structure and behavior required for Bluetooth communication.

This class implements the `BluetoothDataListener` interface and is responsible for registering itself with the shared `BluetoothService` instance to receive incoming data associated with its device address.

#### Abstract Methods
The class does not implement connection logic directly. Instead, it defines a set of abstract methods (`connect`, `disconnect`, `sendData`, and `isConnected`) that must be implemented by concrete subclasses. 

- `connect()` – Initializes the device connection and data stream
- `disconnect()` – Closes the connection and updates the device status
- `sendData(String data)` – Sends data to the device
- `isConnected()` – Checks the current connection state

Overall, this class centralizes common device-related behavior and enforces a clear structure.


### CheckpointDevice

The `CheckpointDevice` class represents a physical checkpoint that communicates with the application over Bluetooth. It extends the abstract `Device` class and implements the specific logic required to detect when a car passes a checkpoint during a race session.

Each checkpoint maintains its own Bluetooth socket, an index that identifies its position within a circuit, and internal state used to track whether a car has already been detected. This prevents multiple detections of the same car during a single race session.

Incoming Bluetooth data is handled through the `onDataReceived` callback. When a detection command is received, the checkpoint records the detection timestamp and forwards this information to the current race session managed by the `SessionManager`. This allows checkpoint timing data to be collected in a centralized way without coupling the checkpoint directly to session logic.

The class also provides utility methods for resetting its internal state when a new race session starts, as well as basic logging methods used during debugging and development.


### CarDevice

The `CarDevice` class represents the controllable car and is responsible for handling all Bluetooth communication related to car operation, configuration, and control modes. It extends the abstract `Device` class and provides concrete implementations for connection handling and data transmission.

The car supports multiple operation modes (`SETUP`, `DRIVE`, and `LINE_FOLLOWER`), which determine how incoming commands are interpreted and which actions are allowed. Mode changes are translated into predefined commands and sent directly to the car over Bluetooth.

Configuration of the car is handled through the nested `CarConfiguration` class. This class stores PID calibration values and base motor speeds used in line-following mode. Before a configuration is created, input values are validated to ensure they fall within acceptable ranges. PID values are uploaded to the car using a structured command sequence that accounts for the length of each parameter.

The `CarDevice` class also centralizes higher-level actions such as starting or stopping line following and uploading configuration data, while keeping low-level Bluetooth operations abstracted through the shared `BluetoothService`.

### DeviceManager

The `DeviceManager` class acts as a central point for creating, storing, and managing all Bluetooth devices used by the application. Any component that needs to interact with the car or checkpoint devices accesses them through this manager.

The class is implemented as a singleton to ensure that only one device manager exists at runtime. This avoids inconsistencies that could appear if multiple parts of the application attempted to manage Bluetooth devices independently.

`DeviceManager` is responsible for registering both the car device and checkpoint devices. Each registered device is stored internally, allowing the manager to keep track of all active connections and perform collective operations, such as disconnecting all devices at once.

Checkpoint devices are indexed based on their order of connection and placement on the circuit. This index is used as a stable reference throughout a race session, making it easier to associate timing data with the correct checkpoint.

The class also provides helper methods for querying device availability, retrieving specific devices, and checking whether a car or checkpoint device is currently connected. By centralizing these responsibilities, `DeviceManager` simplifies device handling logic across the application and keeps Bluetooth-related state management consistent.
