/*
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.geezertechnet.zip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * This application parses a CSV file containing all zip codes in the US and
 * imports this data into either a MySQL or Mongo database. For MySQL it will
 * drop and then create a table named zips in the specified database. In Mongo
 * it will drop and then create a collection named zips in the specified database.
 * <p>
 * For MySQL, ensure you have created a test database and user ahead of time. </p>
 * 
 * <p><pre>
 * 
 * mysql -u root <-p password>
 * CREATE DATABASE zipcodetest;<br>
 * GRANT ALL PRIVILEGES ON zipcodetest.* to 'zipuser'@'%' IDENTIFIED BY 'zippass';
 * 
 * </pre></p>
 * <p>
 * For Mongo you don't need to do anything if you are running without authentication
 * enabled. Otherwise you will need to create a user and grant readWrite. The DB
 * will be created automatically on first insert. In the example below your DB
 * will be zipcodetest, authDB will be zipcodetest, user will be zipuser, and password
 * will be zippass.
 * </p>
 * <p><pre>
 * 
 * mongo -u admin -p admin --authenticationDatabase admin --port 27017
 * use zipcodeTest
 * db.createUser(
 *   {
 *     user: "zipuser",
 *     pwd: "zippass",
 *     roles: [ { role: "readWrite", db: "zipcodeTest" } ]
 *   }
 * )
 * 
 * </pre></p>
 * @author Loren Kratzke
 */

// Prevent Spring Boot from creating a default Mongo connection on localhost
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class ZipcodeDBImportUtilApp implements CommandLineRunner {
  
  // We could just instantiate these but will use Spring injection because it's fun.
  @Autowired
  private MysqlDAL mysqlDAL;
  
  @Autowired
  private MongoDAL mongoDAL;

  public static void main(String[] args) throws Exception {
    SpringApplication app = new SpringApplication(ZipcodeDBImportUtilApp.class);
    //Disable the Spring logo banner.
    app.setBannerMode(Banner.Mode.OFF);
    app.run(args);
  }

  @Override
  public void run(String... args) throws Exception {
    
    ParamBean params = new ParamBean();
    
    while (!params.isApproved()) {
      UserInput userInput = new UserInput();
      userInput.collectUserInput(params);
    }

    boolean success = false;
    
    if (params.isCreateImportFile()) {
      switch (params.getDatabaseType()) {
        case MYSQL:
          success = mysqlDAL.generateSQLImportFile();
          break;
        case MONGO:
          success = mongoDAL.generateJsonImportFile();
          break;
      }
    } else {
      switch (params.getDatabaseType()) {
        case MYSQL:
          success = mysqlDAL.importData(params);
          break;
        case MONGO:
          success = mongoDAL.importData(params);
          break;
      }
    }
    
    if (success) {
      System.out.println("Successfully imported/exported all data.");
    } else {
      System.out.println("Failed to import/export all data.");
    }
  }
}
