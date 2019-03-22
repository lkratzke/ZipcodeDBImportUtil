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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.bson.Document;
import org.springframework.stereotype.Component;

/**
 *
 * @author Loren Kratzke
 */
@Component
public class MongoDAL {
  
  public boolean importData(ParamBean params) {
    boolean success = true;
    MongoClient mongoClient = getMongoClient(params);
    CsvFileReader cvsFileReader = new CsvFileReader();
    int processed = 0;
    
    try {
      MongoDatabase db = mongoClient.getDatabase(params.getDatabaseName());
      MongoCollection<Document> zipCollection = db.getCollection("zipcodeTestCollection");
      // Make the test repeatable
      zipCollection.drop();
      ZipCodeBean z = null;
      
      while ((z = cvsFileReader.nextZipCode()) != null) {
        Document doc = new Document();
        doc.put("recordNumber", z.getRecordNumber());
        doc.put("zipcode", z.getZipCode());
        doc.put("zipcodeType", z.getZipCodeType());
        doc.put("city", z.getCity());
        doc.put("state", z.getState());
        doc.put("locationType", z.getLocationType());
        doc.put("lat", z.getLat());
        doc.put("lon", z.getLon());
        doc.put("xAxis", z.getxAxis());
        doc.put("yAxis", z.getyAxis());
        doc.put("zAxis", z.getzAxis());
        doc.put("worldRegion", z.getWorldRegion());
        doc.put("country", z.getCountry());
        doc.put("locationText", z.getLocationText());
        doc.put("location", z.getLocation());
        doc.put("decom", z.getDecom());
        doc.put("taxReturnFiled", z.getTaxReturnsFiled());
        doc.put("estPop", z.getEstimatedPopulation());
        doc.put("totalWages", z.getTotalWages());
        doc.put("notes", z.getNotes());
        
        zipCollection.insertOne(doc);
        
        if (++processed % 1000 == 0) {
          System.out.println("Processed " + processed + " records");
        }
      }
    } catch(IOException | MongoException | IllegalArgumentException e) {
      System.out.println(e.getMessage());
      success = false;
    } finally {
      cvsFileReader.close();
      mongoClient.close();
    }
    
    System.out.println("Processed a total of " + processed + " records");
    return success;
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
  
  public boolean generateJsonImportFile() {
    boolean success = true;
    CsvFileReader cvsFileReader = new CsvFileReader();
    int processed = 0;
    PrintWriter out = null;
    
    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    // Uncomment for pretty JSON data.
//    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    
    try {
      out = new PrintWriter(new FileWriter("zipcode.json"));
      ZipCodeBean z = null;
      
      while ((z = cvsFileReader.nextZipCode()) != null) {
        out.println(mapper.writeValueAsString(z));
        processed++;
      }
      
    } catch (IOException e) {
      System.out.println(e.getMessage());
      success = false;
    } finally {
      if (out != null) {
        out.close();
      }
    }
    System.out.println("Processed " + processed + " records");
    return success;
  }
}