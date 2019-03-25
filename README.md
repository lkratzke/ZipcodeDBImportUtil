# ZipcodeDBImportUtil

This is a demo project useful for learning a few handy technologies:

* SpringBoot
* MySQL/JDBC
* Mongo
* OpenCSV
* java.util.Scanner
* java.util.zip.ZipInputStream

This is a command line Java application containing a list of all US zip codes. The project contains a CSV file that can be extracted if you need CSV data. 

http://federalgovernmentzipcodes.us/

To run, compile the project `(mvn clean install)` and then run the executable jar.

When executed from the command line you will be asked if you want to create a SQL/JSON file `(F)` or perform an import into an existing database `(I)`.

If the import option is chosen, the app will collect database connection info from the user and perform the import.

## SQL/JSON File

Select `F` when prompted if you want to generate a File. The application will generate `zipcode.sql` or `zipcode.json` depending upon the type of database selected. The application then exits.

## MySQL

For MySQL import, ensure you have created a test database and user ahead of time. The applicaton will create a table named zipcodetest in the specified database and populate the table with zipcodes and their locations.

Example:
```
 mysql -u root -p
 CREATE DATABASE zipcodetest;
 GRANT ALL PRIVILEGES ON zipcodetest.* to 'zipuser'@'%' IDENTIFIED BY 'zippass';`
```

## Mongo

 For Mongo you don't need to do anything if you are running without authentication
 enabled. Otherwise you will need to create a user and grant readWrite. The DB
 will be created automatically on first insert. In the example below your DB
 will be zipcodeTest, authDB will be zipcodeTest, user will be zipuser, and password
 will be zippass.
 
 Example:
 ```
mongo -u admin -p admin --authenticationDatabase admin --port 27017
use zipcodeTest
db.createUser(
  {
    user: "zipuser",
    pwd: "zippass",
    roles: [ { role: "readWrite", db: "zipcodeTest" } ]
  }
)
 ```
 
Enjoy, learn, and have fun!
