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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bson.Document;
import org.springframework.stereotype.Component;

/**
 *
 * @author Loren Kratzke
 */
@Component
public class MongoDAL {
  
  public void importData(ParamBean params) {
    MongoClient mongoClient = null;
    CSVReader csvReader = null;
    int processed = 0;
    
    try {
      mongoClient = getMongoClient(params);
      MongoDatabase db = mongoClient.getDatabase(params.getDatabaseName());
      MongoCollection<Document> zipCollection = db.getCollection("zips");
      
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("free-zipcode-database.zip");
      ZipInputStream zis = new ZipInputStream(inputStream);
      csvReader = new CSVReader(new BufferedReader(new InputStreamReader(zis)));
      
      // Set the ZipInputStream to the position of the first and only entry 
      // in this zip file which is the csv file.
      ZipEntry zipEntry = zis.getNextEntry();
      
      String[] line;
      line = csvReader.readNext(); // discard header row

      while ((line = csvReader.readNext()) != null) {
        Document doc = new Document();
        doc.put("RECORD_NUM", line[0]);
        doc.put("ZIPCODE", line[1]);
        doc.put("ZIPCODE_TYPE", line[2]);
        doc.put("CITY", line[3]);
        doc.put("STATE", line[4]);
        doc.put("LOCATION_TYPE", line[5]);
        doc.put("LAT", line[6]);
        doc.put("LON", line[7]);
        doc.put("XAXIS", line[8]);
        doc.put("YAXIS", line[9]);
        doc.put("ZAXIS", line[10]);
        doc.put("WORLD_REGION", line[11]);
        doc.put("COUNTRY", line[12]);
        doc.put("LOCATION_TEXT", line[13]);
        doc.put("LOCATION", line[14]);
        doc.put("DECOM", line[15]);
        doc.put("TAXRETURNS_FILED", line[16]);
        doc.put("EST_POP", line[17]);
        doc.put("TOTAL_WAGES", line[18]);
        doc.put("NOTES", line[19]);
        
        zipCollection.insertOne(doc);
        if (++processed % 1000 == 0) {
          System.out.println("Processed " + processed + " records");
          System.out.println(doc.toJson());
        }
      }
    } catch(IOException e) {
      System.out.println(e);
    } finally {
      if (mongoClient != null) {
        mongoClient.close();
      }
      if (csvReader != null) {
        try {csvReader.close();} 
        catch (IOException e) {System.out.println(e);}
      }
    }
    
    System.out.println("Processed a total of " + processed + " records");
  }
  
  private MongoClient getMongoClient(ParamBean params) {
    MongoClient mongoClient = null;
    
    if (params.isUseAuth()) {
      MongoCredential creds = MongoCredential.createCredential(params.getUsername(), params.getAuthDB(), params.getPassword().toCharArray());
      ServerAddress serverAddress = new ServerAddress(params.getHost(), Integer.parseInt(params.getPort()));
      mongoClient = new MongoClient(serverAddress, creds, MongoClientOptions.builder().build());
    } else {
      mongoClient = new MongoClient(params.getHost(), Integer.parseInt(params.getPort()));
    }
    
    return mongoClient;
  }
  
}
