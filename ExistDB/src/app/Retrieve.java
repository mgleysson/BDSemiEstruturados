package app;

import org.exist.xmldb.XQueryService;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;


public class Retrieve {

	protected static String driver = "org.exist.xmldb.DatabaseImpl";
	protected static String URI = "xmldb:exist://localhost:8080/exist/xmlrpc";
	protected static String collectionPath = "/db/agenda";
	protected static String resourceName = "agenda.xml";

	public static void main(String args[]) throws Exception {

		// initialize database drivers
		Class<?> cl = Class.forName(driver);
		Database database = (Database) cl.newInstance();
		DatabaseManager.registerDatabase(database);
		
		Collection col = DatabaseManager.getCollection(URI+collectionPath);
		String xquery = "for $c in //contact return $c/name/text()";
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
}
