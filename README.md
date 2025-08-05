# Concurrent and Distributed Programming - Scala Part

This repository contains the Scala implementation for Assignment 3 of the Concurrent and Distributed Programming course. The project focuses on demonstrating key concepts in concurrent and distributed systems using Scala.

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [Code Explanation](#code-explanation)
- [Contributors](#contributors)
- [License](#license)

## Overview

The project showcases various aspects of concurrent and distributed programming, including:

- **Concurrency** -> implementing multiple threads to perform tasks simultaneously
- **Distributed Systems** -> simulating a network of nodes communicating over a distributed environment
- **Synchronization** -> managing shared resources to prevent conflicts in a multi-threaded context.

## Project Structure

The repository is organized as follows:


.
├── src/
│ └── main/
│ └── scala/
│ └── <your_project_files>.scala
├── .gitignore
├── .scalafmt.conf
└── build.sbt



- `src/main/scala/` -> contains the main Scala source files
- `.gitignore` -> specifies files and directories to be ignored by Git
- `.scalafmt.conf` -> configuration file for Scala code formatting
- `build.sbt` -> SBT build configuration file.

## Setup and Installation

To set up the project locally, follow these steps:

1. **Clone the repository**:

   ```bash
   git clone https://github.com/FabioNotaro2001/ConcurrentAndDistributedProgramming-Assignment3-ScalaPart.git
   cd ConcurrentAndDistributedProgramming-Assignment3-ScalaPart

Install SBT: Ensure that Scala Build Tool (SBT) is installed on your system. You can download it from https://www.scala-sbt.org/download.html.

Compile the project:
sbt compile

Run the application:
sbt run

Running the Application

After setting up the project, you can run the application using SBT. The application will execute the main class defined in the build.sbt file.
sbt run


Ensure that you have the necessary environment to support Scala applications, including Java Development Kit (JDK) 8 or higher.
Code Explanation

The main components of the project include:

    Actor Model: Utilized for managing concurrency and communication between different parts of the system.

    Futures and Promises: Employed for handling asynchronous computations and managing results.

    Akka Toolkit: If applicable, used for building concurrent, distributed, and resilient message-driven applications.

Each component is designed to demonstrate specific principles of concurrent and distributed programming, ensuring a comprehensive understanding of these concepts.
Contributors

    FabioNotaro2001

License

This project is licensed under the MIT License - see the LICENSE file for details.
