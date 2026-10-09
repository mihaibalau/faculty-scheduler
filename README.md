# faculty-scheduler

Builds a weekly faculty timetable from rooms, teachers and the subjects that have to be taught. The same constraints are solved twice: once with Java threads, once with C++ and MPI.

## Overview

A subject needs a course, and sometimes a seminar and a lab, with given teachers and a split across groups. Rooms belong to a building and only fit some activity types. Teachers have hours they cannot teach. Both programs load that from JSON, refuse a timetable that breaks the hard rules, and write the placed sessions to a file.

## Key Features

- **Input**: `subjects`, `teachers`, `places` and `groups`, each in its own JSON file
- **Java threads**: subjects with more hours per week are scheduled first. A fixed pool, one thread per core, runs a backtracking placement per subject. Teacher, room and group occupancy sit in shared state
- **Checks before generation**: teacher definitions have to pass. Capacity, availability and slot collisions then run in parallel. A warning does not stop the run. A failure does
- **MPI**: one process keeps the work, the others ask for a batch, and each rank writes `schedule_output_<rank>.json`. The solver tags are request, assign, result, terminate and the final schedule
- **Sessions**: course, seminar or laboratory, weekly or on odd/even weeks

## Code structure

- `threads/` — Maven project. `TimetableInitializer` loads the repositories, `ValidationOrchestrator` runs the checks, `TimetableGenerator` and `SubjectScheduler` place the hours, `ScheduleExporter` writes the JSON. Config files are in `src/main/resources`
- `mpi/` — the same split into `model`, `repository` and `service`. `config/` holds the four JSON files. `main.cpp` calls `MPI_Init`, loads that folder and starts `Solver`

```bash
cd threads
mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=org.example.Main
```

```bash
cd mpi
mpicxx -std=c++17 -o scheduler main.cpp repository/*.cpp service/*.cpp
mpirun -n 4 ./scheduler
```
