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

import java.io.IOException;
import static java.sql.Types.BIGINT;
import static java.sql.Types.VARCHAR;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

/**
 *
 * @author Loren Kratzke
 */
@Component
public class MysqlDAL {
  
  private static final String CREATE_TABLE = 
          "CREATE TABLE zipcodetest (\n" +
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
  
  private static final String DROP_TABLE = "drop table if exists zipcodetest";
  
  private static final String INSERT_ROW = 
          "INSERT INTO zipcodetest (RECORD_NUM, ZIPCODE, ZIPCODE_TYPE, CITY, STATE, " + 
          "LOCATION_TYPE, LAT, LON, XAXIS, YAXIS, ZAXIS, WORLD_REGION, COUNTRY, " + 
          "LOCATION_TEXT, LOCATION, DECOM, TAXRETURNS_FILED, EST_POP, TOTAL_WAGES, " + 
          "NOTES) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
  private static final int[] SQL_TYPES = new int[]{
    VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
    VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
    BIGINT,  BIGINT,  BIGINT,  VARCHAR};
  
  public boolean importData(ParamBean params) {
    boolean success = true;
    SingleConnectionDataSource  dataSource = new SingleConnectionDataSource();
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    
    // My DB connection broke when we switched to daylight savings time. (PDT)
    // The error was 
    //     java.sql.SQLException: The server timezone value 'UTC' is unrecognized 
    //     or represents more than one timezone. You must configure either the server 
    //     or JDBC driver (via the serverTimezone configuration property) to use 
    //     a more specifc timezone value if you want to utilize timezone support.
    // The extra params below found on stackoverflow seem to fix the issue.
    
    dataSource.setUrl("jdbc:mysql://" + params.getHost() + ":" + params.getPort() + 
            "/" + params.getDatabaseName() + 
            "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
    dataSource.setUsername(params.getUsername());
    dataSource.setPassword(params.getPassword());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    
    CsvFileReader cvsFileReader = new CsvFileReader();
    int processed = 0;
    
    try {
      // Make the process repeatable to support hacking and tweaking
      jdbcTemplate.execute(DROP_TABLE);
      jdbcTemplate.execute(CREATE_TABLE);
    
      ZipCodeBean z = null;
      while ((z = cvsFileReader.nextZipCode()) != null) {
        Object[] args = new Object[20];
        args[0] = z.getRecordNumber();
        args[1] = z.getZipCode();
        args[2] = z.getZipCodeType();
        args[3] = z.getCity();
        args[4] = z.getState();
        args[5] = z.getLocationType();
        args[6] = z.getLat();
        args[7] = z.getLon();
        args[8] = z.getxAxis();
        args[9] = z.getyAxis();
        args[10] = z.getzAxis();
        args[11] = z.getWorldRegion();
        args[12] = z.getCountry();
        args[13] = z.getLocationText();
        args[14] = z.getLocation();
        args[15] = z.getDecom();
        args[16] = z.getTaxReturnsFiled();
        args[17] = z.getEstimatedPopulation();
        args[18] = z.getTotalWages();
        args[19] = z.getNotes();
        
        // Insert row of data
        jdbcTemplate.update(INSERT_ROW, args, SQL_TYPES);
        
        if (++processed % 1000 == 0) {
          System.out.println("Processed " + processed + " records");
        }
      } 
    } catch (IOException | DataAccessException e) {
      System.out.println(e.getMessage());
      success = false;
    } finally {
      cvsFileReader.close();
      dataSource.destroy();
    }
    
    System.out.println("Processed a total of " + processed + " records");
    return success;
  }
}
