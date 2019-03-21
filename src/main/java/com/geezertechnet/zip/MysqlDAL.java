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

import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.sql.Types.BIGINT;
import static java.sql.Types.VARCHAR;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

/**
 *
 * @author Loren Kratzke
 */
@Component
public class MysqlDAL {
  
  private static final String createTable = 
          "CREATE TABLE zips (\n" +
          "  ID               BIGINT        NOT NULL AUTO_INCREMENT,\n" +
          "  RECORD_NUM       VARCHAR(255)  NOT NULL,\n" +
          "  ZIPCODE          VARCHAR(255)  NOT NULL,\n" +
          "  ZIPCODE_TYPE     VARCHAR(255)  NOT NULL,\n" +
          "  CITY             VARCHAR(255)  NOT NULL,\n" +
          "  STATE            VARCHAR(255)  NOT NULL,\n" +
          "  LOCATION_TYPE    VARCHAR(255)  NOT NULL,\n" +
          "  LAT              VARCHAR(255)  NOT NULL,\n" +
          "  LON              VARCHAR(255)  NOT NULL,\n" +
          "  XAXIS            VARCHAR(255)  NOT NULL,\n" +
          "  YAXIS            VARCHAR(255)  NOT NULL,\n" +
          "  ZAXIS            VARCHAR(255)  NOT NULL,\n" +
          "  WORLD_REGION     VARCHAR(255)  NOT NULL,\n" +
          "  COUNTRY          VARCHAR(255)  NOT NULL,\n" +
          "  LOCATION_TEXT    VARCHAR(255)  NOT NULL,\n" +
          "  LOCATION         VARCHAR(255)  NOT NULL,\n" +
          "  DECOM            VARCHAR(255)  NOT NULL,\n" +
          "  TAXRETURNS_FILED BIGINT        NOT NULL,\n" +
          "  EST_POP          BIGINT        NOT NULL,\n" +
          "  TOTAL_WAGES      BIGINT        NOT NULL,\n" +
          "  NOTES            VARCHAR(255)  NOT NULL,\n" +
          "  PRIMARY KEY (`ID`)\n" +
          ")";
  
  private static String dropTable = "drop table if exists zips";
  
  private static final String insertRow = 
          "INSERT INTO zips (RECORD_NUM, ZIPCODE, ZIPCODE_TYPE, CITY, STATE, " + 
          "LOCATION_TYPE, LAT, LON, XAXIS, YAXIS, ZAXIS, WORLD_REGION, COUNTRY, " + 
          "LOCATION_TEXT, LOCATION, DECOM, TAXRETURNS_FILED, EST_POP, TOTAL_WAGES, " + 
          "NOTES) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
  private static final int[] sqlTypes = new int[]{
    VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
    VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
    BIGINT,  BIGINT,  BIGINT,  VARCHAR};
  
  public void importData(ParamBean params) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(params);
    
    // Make the process repeatable to support hacking and tweaking
    jdbcTemplate.execute(dropTable);
    jdbcTemplate.execute(createTable);
    
    // Read lines from the CSV file and convert them into rows in the MySQL database.
    CSVReader cvsReader = null;
    int processed = 0;
    
    try {
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("free-zipcode-database.zip");
      ZipInputStream zis = new ZipInputStream(inputStream);
      cvsReader = new CSVReader(new BufferedReader(new InputStreamReader(zis)));
      
      // Set the ZipInputStream to the position of the first and only entry 
      // in this zip file which is the csv file.
      ZipEntry zipEntry = zis.getNextEntry();
      
      String[] line;
      line = cvsReader.readNext(); // header row
      
      while ((line = cvsReader.readNext()) != null) {
        
        // Move character and numeric data into object array
        Object[] args = new Object[20];
        System.arraycopy(line, 0, args, 0, 16);
        
        // Some elements need to be converted to Integers
        args[16] = (line[16].isEmpty() ? 0 : Integer.parseInt(line[16]));
        args[17] = (line[17].isEmpty() ? 0 : Integer.parseInt(line[17]));
        args[18] = (line[18].isEmpty() ? 0 : Integer.parseInt(line[18]));
        args[19] = line[19];
        
        // Insert row of data
        jdbcTemplate.update(insertRow, args, sqlTypes);
        if (++processed % 1000 == 0) {
          System.out.println("Processed " + processed + " records");
        }
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      if (cvsReader != null) {
        try {cvsReader.close();} 
        catch (IOException e) {}
      }
    }
    
    System.out.println("Processed a total of " + processed + " records");
  }
  
  private JdbcTemplate getJdbcTemplate(ParamBean params) {
    // Dynamically create a JdbcTemplate using connection params provided by user.
    SingleConnectionDataSource  dataSource = new SingleConnectionDataSource();
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://" + params.getHost() + ":" + params.getPort() + "/" + params.getDatabaseName());
    dataSource.setUsername(params.getUsername());
    dataSource.setPassword(params.getPassword());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return jdbcTemplate;
  }
}
