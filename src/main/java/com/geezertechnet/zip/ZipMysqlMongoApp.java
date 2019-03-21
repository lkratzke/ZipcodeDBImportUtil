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

import java.util.Scanner;
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
 * CREATE DATABASE ziptest;<br>
 * GRANT ALL PRIVILEGES ON ziptest to 'zip'@'%' IDENTIFIED BY 'zip';
 * 
 * </pre></p>
 * <p>
 * For Mongo you don't need to do anything if you are running without authentication
 * enabled. Otherwise you will need to create a user and grant readWrite. The DB
 * will be created automatically on first insert. In the example below your DB
 * will be zip, authDB will be zip, user will be zip, and pass will also be zip.
 * </p>
 * <p><pre>
 * 
 * use zip
 * db.createUser(
 *   {
 *     user: "zip",
 *     pwd: "zip",
 *     roles: [ { role: "readWrite", db: "zip" } ]
 *   }
 * )
 * 
 * </pre></p>
 * @author Loren Kratzke
 */

// Prevent Spring Boot from creating a default Mongo connection on localhost
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class ZipMysqlMongoApp implements CommandLineRunner {
  
  // We could just instantiate these but will use Spring injection because it's fun.
  @Autowired
  private MysqlDAL mysqlDAL;
  
  @Autowired
  private MongoDAL mongoDAL;

  public static void main(String[] args) throws Exception {
    //Disable the Spring logo banner.
    SpringApplication app = new SpringApplication(ZipMysqlMongoApp.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.run(args);
  }

  @Override
  public void run(String... args) throws Exception {
    ParamBean params = new ParamBean();
    
    while (!params.isApproved()) {
      getUserInput(params);
    }

    switch (params.getDatabaseType()) {
      case MYSQL:
        mysqlDAL.importData(params);
        break;
      case MONGO:
        mongoDAL.importData(params);
        break;
    }
  }
  
  private void getUserInput(ParamBean params) {
    Scanner input = new Scanner(System.in);

    params.setDatabaseType(getDatabaseType(input));
    params.setHost(getStringInput(input, "Host? ", params.getHost()));
    params.setPort(getStringInput(input, "Port? ", params.getPort()));
    params.setDatabaseName(getStringInput(input, "Database name? ", params.getDatabaseName()));
    
    if (params.getDatabaseType().equals(DatabaseType.MYSQL)) {
      params.setUseAuth(true);
    } else {
      params.setUseAuth(getUseAuth(input));
    }
    
    if (params.isUseAuth()) {
      if (params.getDatabaseType() == DatabaseType.MONGO) {
        params.setAuthDB(getStringInput(input, "Auth DB name? ", params.getAuthDB()));
      }
      params.setUsername(getStringInput(input, "User Name? ", params.getUsername()));
      params.setPassword(getStringInput(input, "Password? ", params.getPassword()));
    }
    
    System.out.println();
    System.out.println("Database Type: " + params.getDatabaseType());
    System.out.println("Host: " + params.getHost());
    System.out.println("Port: " + params.getPort());
    System.out.println("Database Name: " + params.getDatabaseName());
    System.out.println("Auth: " + params.isUseAuth());
    System.out.println("Username: " + params.getUsername());
    System.out.println("Password: " + params.getPassword());
    System.out.println("Auth DB (mongo only): " + params.getAuthDB());
    
    System.out.println();
    System.out.println("Is this correct? y/n");
    params.setApproved(input.nextLine().equalsIgnoreCase("y"));
  }
  
  private String getStringInput(Scanner input, String question, String defaultVal) {
    System.out.println();
    System.out.println(question);
    if (defaultVal != null && defaultVal.length() > 0) {
      System.out.print("(" + defaultVal + ") ");
    }
    String answer = input.nextLine().trim();
    if (answer.length() == 0) {
      return defaultVal;
    }
    return answer;
  }

  private boolean getUseAuth(Scanner input) {
    String useAuth = null;
    while (useAuth == null) {
      System.out.println();
      System.out.println("Is authentication required? y/n ");
      String s = input.nextLine();
      if (s.toLowerCase().equals("y") || s.toLowerCase().equals("n")) {
        useAuth = s;
      }
    }
    return useAuth.equals("y");
  }

  private DatabaseType getDatabaseType(Scanner input) {
    DatabaseType databaseType = null;

    while (databaseType == null) {
      System.out.println();
      System.out.println("Database type? ");
      System.out.println("1. MySQL (1)");
      System.out.println("2. Mongo (2)");
      String val = input.nextLine();
      try {
        int i = Integer.parseInt(val);
        DatabaseType t = DatabaseType.find(i);
        if (t != null) {
          databaseType = t;
        }
      } catch (Exception e) {
        System.out.println("Please input a valid value.");
      }
    }
    return databaseType;
  }

}
