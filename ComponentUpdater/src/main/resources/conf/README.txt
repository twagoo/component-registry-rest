This utility will apply a style sheet to all component specifications in the
Component Registry database (versions 1.14.5, and higher including 2.x) that 
converts all specifications to CMDI 1.2.

Usage:
- configure the right database connection in datasource.properties
- make sure no other application is connected to the database
- make a database backup :D
- run the utility:

    java -jar ComponentSpecFixer-1.0-SNAPSHOT.jar

- inspect the output
- optionally: run the application
