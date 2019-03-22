/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geezertechnet.zip;

import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author loren
 */
public class CsvFileReader {
  
  private CSVReader cvsReader;
  
  public ZipCodeBean nextZipCode() throws IOException {
    if (cvsReader == null) {
      createReader();
    }
    
    String[] line = cvsReader.readNext();
    if (line == null) {
      return null;
    }
    
    ZipCodeBean b = new ZipCodeBean();
    
    b.setRecordNumber(Long.parseLong(line[0]));
    b.setZipCode(line[1]);
    b.setZipCodeType(line[2]);
    b.setCity(line[3]);
    b.setState(line[4]);
    b.setLocationType(line[5]);
    b.setLat(line[6]);
    b.setLon(line[7]);
    b.setxAxis(line[8]);
    b.setyAxis(line[9]);
    b.setzAxis(line[10]);
    b.setWorldRegion(line[11]);
    b.setCountry(line[12]);
    b.setLocationText(line[13]);
    b.setLocation(line[14]);
    b.setDecom(line[15]);
    b.setTaxReturnsFiled((line[16].isEmpty() ? 0 : Integer.parseInt(line[16])));
    b.setEstimatedPopulation((line[17].isEmpty() ? 0 : Integer.parseInt(line[17])));
    b.setTotalWages((line[18].isEmpty() ? 0 : Integer.parseInt(line[18])));
    b.setNotes(line[19]);
    
    return b;
  }
  
  private void createReader() throws IOException {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("free-zipcode-database.zip");
    ZipInputStream zis = new ZipInputStream(inputStream);
    cvsReader = new CSVReader(new BufferedReader(new InputStreamReader(zis)));

    // Set the ZipInputStream to the position of the first and only  
    // entry in this zip file which is the csv file.
    ZipEntry zipEntry = zis.getNextEntry();

    cvsReader.readNext(); // header row
  }
  
  public void close() {
    if (cvsReader != null) {
      try {
        cvsReader.close();
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
  
}
