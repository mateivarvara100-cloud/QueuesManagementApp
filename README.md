# QueuesManagementApp (Queues Management System)

Java desktop application that simulates a **multi-queue / multi-server** system (clients arrive over time and are dispatched to queues using different strategies). The simulation runs on a separate thread and displays live status in a Swing UI, while also writing results to log files.

---

## Features
- Swing UI for configuring simulation parameters (N, Q, arrival/service intervals, time limit)
- Two dispatch policies:
  - **Shortest Queue** (send client to the queue with the fewest tasks)
  - **Shortest Time** (send client to the queue with the smallest waiting time)
- Real-time simulation output in the GUI (current time, waiting clients, queue states)
- Stop simulation at any time
- Log files automatically generated after each run (and when force-stopped)

---

## Project structure (packages / folders)

> Note: this project uses top-level folders (not `src/main/java`).

### `gui` — GUI (Swing)
User interface and simulation controls.
- `SimulationFrame` — main window:
  - input fields for simulation configuration
  - policy selector (`SelectionPolicy`)
  - start/stop buttons
  - live log output area

### `business_logic` — Business Logic / Simulation engine
Core simulation workflow and dispatch logic.
- `SimulationManager` — main simulation loop (`Runnable`):
  - generates random tasks (clients)
  - dispatches tasks at their arrival time
  - tracks metrics (average waiting/service time, peak hour)
  - writes status + final results to a log file
  - supports force stopping (`stopSimulation`)
- `Scheduler` — manages the list of servers (queues) and dispatches tasks using the chosen strategy
- `SelectionPolicy` — enum defining the available scheduling policies
- `Strategy` — strategy interface used by the scheduler
- `ShortestQueueStrategy` — dispatch implementation for “fewest tasks”
- `ShortestTimeStrategy` — dispatch implementation for “smallest waiting time”

### `model` — Data model
Classes representing the simulation entities.
- `Task` — a client/task with arrival time and service/processing time (comparable for sorting)
- `Server` — a queue/server that processes tasks (typically on its own thread) and exposes:
  - current tasks
  - total waiting time
  - ability to stop processing

### `logs` — Example output logs
Contains sample logs from previous test runs (e.g. `log_test_N10.txt`, `log_test_N50.txt`, etc.).

> The simulation also creates a folder named `Logs/` at runtime (note the capital L) and writes new logs there.

---

## Requirements
- Java (JDK) installed (project is configured for **Java 25** in `pom.xml`)
- IntelliJ IDEA recommended (easy GUI run)

---

## How to run
### IntelliJ IDEA (recommended)
1. Open the project
2. Run the `Main` class (root-level `Main.java`)
3. In the UI:
   - Fill in input fields (N, Q, time limit, arrival/service ranges)
   - Choose a policy
   - Click **Start Simulation**
   - Click **Stop Simulation** to force-stop (optional)

### From terminal (simple)
If you’re not using Maven packaging, the easiest is still to run from IntelliJ.

---

## Notes
- Input validation is handled in the GUI (invalid values show a message dialog).
- Logs are written during the simulation and a final summary is appended at the end:
  - average waiting time
  - average service time
  - peak hour
