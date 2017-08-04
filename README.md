# RevEngE
Welcome to the Github repo for the Reverse Engineering Engine—RevEngE.  The code contained here is a webapp which generates reverse engineering exercises for students to solve.  The instructions regarding how to use this software—and associated features such as data collection—run the system on a Tomcat webserver and read the instructions on the resulting web page.  The system is described in detail [here](https://www.usenix.org/conference/ase16/workshop-program/presentation/taylor).

## Requirements
This system is designed to run on Linux, and is tested on Ubuntu in particular.  However, the webapp is Java-based with a MySQL DBMS, and should run in any suitable Java server container.  The data collection software bundled with this webapp is designed to run on Linux operating systems with x86 or ARM hardware; students who are not using Linux containers will be unable to fully run this software.  Additional OS support is planned.
