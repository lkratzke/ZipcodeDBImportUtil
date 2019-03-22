/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geezertechnet.zip;

import java.util.Scanner;

/**
 *
 * @author loren
 */
public class UserInput {
  
  public void collectUserInput(ParamBean params) {
    Scanner input = new Scanner(System.in);

    try {
      // If we are creating a file then we only need to know the type of database.
      params.setCreateImportFile(getCreateImportFile(input));
      params.setDatabaseType(getDatabaseType(input));
      if (params.isCreateImportFile()) {
        params.setApproved(true);
        return;
      }

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
    } finally {
      input.close();
    }
  }
  
  private boolean getCreateImportFile(Scanner input) {
    String createImportFile = null;
    
    while (createImportFile == null) {
      System.out.println();
      System.out.println("Create SQL/JSON file for import into database? (F)");
      System.out.println("Or import directly into database? (I)");
      String answer = input.nextLine().toUpperCase();
      if (answer.equals("F") || answer.equals("I")) {
        createImportFile = answer;
      }
    }
    return createImportFile.equals("F");
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
      String s = input.nextLine().toLowerCase();
      if (s.equals("y") || s.equals("n")) {
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
