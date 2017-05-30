package couchbase;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;


public class Example {
	
    public static void main( String[] args ){
    	
    	// Initialize the Connection
        Cluster cluster = CouchbaseCluster.create("localhost");
        Bucket bucket = cluster.openBucket("departamento", "");

//        // Create a JSON Document
//        JsonObject data = JsonObject.create()
//            .put("codDepto", "1")
//            .put("name", "Limpeza")
//            .put("description", "Departamento que cuida da limpeza da organização")
//            .put("yearCreation", "1978");
//
//        // Store the Document
//        bucket.upsert(JsonDocument.create("u:limpeza", data));
//        
//       // Load the Document and print it
//       //Prints Content and Metadata of the stored Document
//       System.out.println(bucket.get("u:limpeza"));
//
//    // Create a N1QL Primary Index (but ignore if it exists)
//       bucket.bucketManager().createN1qlPrimaryIndex(true, false); 
    
    
        // Perform a N1QL Query
        N1qlQueryResult result = bucket.query(
            N1qlQuery.parameterized("SELECT * FROM departamento WHERE codDepto = $codDepto",
            JsonObject.create()
            .put("codDepto", "1")
        ));

        // Print each found Row
        for (N1qlQueryRow row : result) {
            System.out.println(row);
        }   
    }       
}
