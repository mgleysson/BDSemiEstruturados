package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// EXISTDB
import org.exist.xmldb.XQueryService;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;

// COUCHBASE
import com.couchbase.client.java.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;

public class Integration {

	protected static String driver = "org.exist.xmldb.DatabaseImpl";
	protected static String URI = "xmldb:exist://localhost:8080/exist/xmlrpc";
	protected static String collectionPath = "/db/agenda";
	protected static String resourceName = "agenda.xml";

	public static void main(String[] args) throws Exception{

		List<String> lista = new ArrayList<String>();
		Map<String, String> map = new HashMap<>();
		Map<String, Integer> counter = new HashMap<>(); 
		Scanner read = new Scanner(System.in);
		int opc;
		
		// Initialize the Connection in Couchbase
		Cluster cluster = CouchbaseCluster.create("localhost");
		Bucket bucket = cluster.openBucket("departamento", "");
		
		// initialize database drivers
		Class<?> cl = Class.forName(driver);
		Database database = (Database) cl.newInstance();
		DatabaseManager.registerDatabase(database);

		Collection col = DatabaseManager.getCollection(URI+collectionPath);

			System.out.println("\n\n----------");
			System.out.println("** MENU **");
			System.out.println("----------\n");
			System.out.println("1 - Consultar os dados dos empregados organizados por departamento");
			System.out.println("2 - Consultar os dados dos departamentos e sua respectiva quantidade de empregados");
			System.out.print("=> ");
			opc = read.nextInt();
		
		
		if(opc == 1){
		
		
			
			// Perform a N1QL Query
			N1qlQueryResult result2 = bucket.query(
					N1qlQuery.simple("SELECT codDepto FROM departamento"));


			// Print each found Row
			for (N1qlQueryRow row : result2) {
				JsonObject obj = row.value();

				lista.add(obj.getString("codDepto"));

			}

			for(int j = 0; j < lista.size(); j++){
				N1qlQueryResult result3 = bucket.query(
						N1qlQuery.parameterized("SELECT name FROM departamento WHERE codDepto = $codDepto", 
								JsonObject.create()
								.put("codDepto", lista.get(j))
								));

				for (N1qlQueryRow row : result3) {
					JsonObject obj2 = row.value();

					map.put(lista.get(j),obj2.getString("name"));


				}

			}

			

			/*  ExistDB */
			
			
			
			for(int j = 0; j<lista.size(); j++){

				System.out.println("-----------------------------------------------------------");
				System.out.println("\nDepartamento: "+lista.get(j)+" - "+map.get(lista.get(j))+"\n");
				System.out.println("Empregados:");
				System.out.println(" Nome - Telefone - Email");

				String xquery = "for $c in //contact where $c/depto="+lista.get(j)+" return concat($c/name,' - ',$c/phone,' - ',$c/email)";
				XQueryService service = (XQueryService) col.getService("XQueryService", "1.0");
				service.setProperty("indent", "yes");
				ResourceSet result = service.query(xquery);
				ResourceIterator i = result.getIterator();
				
				while (i.hasMoreResources()){
					
					Resource r = i.nextResource();
					String value = (String) r.getContent();
					System.out.println(value);
				}
				
			}
			
		}else{
			
			if(opc == 2){
				
				N1qlQueryResult result = bucket.query(
						N1qlQuery.simple("SELECT codDepto,name,description,yearCreation FROM departamento"));
				
				
				for (N1qlQueryRow row : result) {
					JsonObject obj = row.value();
					
					//lista.add(obj.getString("codDepto"));
					System.out.println("---------------------------------------------------------------------------------");
					System.out.println("\nDepartamento "+obj.getString("codDepto")+": "+obj.get("name"));
					System.out.println();
					System.out.println("Descrição: "+ obj.getString("description"));
					System.out.println("Ano de criação: "+obj.getString("yearCreation"));
					System.out.print("Quantidade de empregados: ");
					
					String xquery = "for $c in //contact where $c/depto="+(String)obj.getString("codDepto")+" return concat($c/name,' - ',$c/phone,' - ',$c/email)";
					XQueryService service = (XQueryService) col.getService("XQueryService", "1.0");
					service.setProperty("indent", "yes");
					ResourceSet result2 = service.query(xquery);
					ResourceIterator i = result2.getIterator();
					
					int contador = 0;
					
					while (i.hasMoreResources()){
						Resource r = i.nextResource();
						String value = (String) r.getContent();
						//System.out.println(value);
						contador++;
					}
					
					System.out.println(contador);
					
				}
			}
		}
	}
}

