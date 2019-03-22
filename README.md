# ZipcodeDBImportUtil

This is a demo project useful for learning a few handy technologies:

* SpringBoot
* MySQL/JDBC
* Mongo
* OpenCSV
* java.util.Scanner

This is a command line Java application containing a list of all US zip codes. The project contains a CSV file that can be extracted if you need CSV data. The app can produce a SQL or JSON files that can be used to import into an existing database.
Optionally it can import the data directly into an existing MySQL or Mongo database.

To run, just compile the project and run the executable jar.

When executed from the command line you will be asked if you want to create a SQL/JSON file or perform an import.

If the import option is chosen, it will collect database connection info from the user and perform the import.

**SQL/JSON File**

Simply select `F` when prompted if you want to generate a File or perform an Import. The application will generate `zipcode.sql` or `zipcode.json` depending upon the type of database selected.

**MySQL**

For MySQL import, ensure you have created a test database and user ahead of time. The applicaton will create a table named zipcodetest in the specified database and populate the table with zipcodes and their locations.

Example:
```
 mysql -u root <-p password>
 CREATE DATABASE zipcodetest;
 GRANT ALL PRIVILEGES ON zipcodetest.* to 'zipuser'@'%' IDENTIFIED BY 'zippass';`
```

**Mongo**

 For Mongo you don't need to do anything if you are running without authentication
 enabled. Otherwise you will need to create a user and grant readWrite. The DB
 will be created automatically on first insert. In the example below your DB
 will be zipcodeTest, authDB will be zipcodetest, user will be zipuser, and password
 will be zippass.
 
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
