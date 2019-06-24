package gov.nih.nlm.umls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.sleepycat.je.DatabaseException;

import gov.nih.nlm.ling.util.FileUtils;

public class HierarchyDBServer {
	
	private static Logger log = Logger.getLogger(HierarchyDBServer.class.getName());
	
	public static void main(String[] args) throws IOException {
		
		String configFilename = "semrepjava.properties";
		System.getProperties().putAll(FileUtils.loadPropertiesFromFile(configFilename));
		HierarchyDatabase hdb;
		try {
			hdb = new HierarchyDatabase(System.getProperty("hierarchy.home", "hierarchyDB"), true);	
			int port = Integer.parseInt(System.getProperty("hierarchyDB.server.port", "9876"));
			ServerSocket serverSocket = new ServerSocket(port); 
			log.info("Hierarchy Database Server initialized...");
			try {
				while(true) {
					Socket socket = serverSocket.accept();
					log.finest("Hierarchy Database Client connected..");
					BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
					BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
					Thread t = new HierarchyDBServerHandler(socket, bis, bos, hdb);
					t.start();
				}
			} catch (Exception e) {
				log.severe("Unable to accept and process hierarchy database client request.");
				e.printStackTrace();
			}
			serverSocket.close();
		} catch (DatabaseException e) {
				log.severe("Unable to open the UMLS concept hierarchy DB.");
		}
	}

}
