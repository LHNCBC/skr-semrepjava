package gov.nih.nlm.ner.metamap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.ner.MetaMapLite;

public class MetaMapLiteServer {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException {
		 
		 File configFile = new File("semrepjava.properties");
		 if( configFile.exists() && !configFile.isDirectory()) {
			 FileReader reader = new FileReader(configFile);
			 Properties props = new Properties(System.getProperties());
			 props.load(reader);
			 System.setProperties(props);
			 MetaMapLite.expandIndexDir(props, System.getProperty("index.dir.name", "data/ivf/2017AA/Base/strict"));
		 }
		 
		 MetaMapLite metaMapLiteInst = new MetaMapLite(System.getProperties());
		 
		 int port = Integer.parseInt(System.getProperty("server.port", "12345"));
			
		 ServerSocket serverSocket = new ServerSocket(port); 

		 try {
	     while(true) {
	    	 Socket socket = serverSocket.accept();
	    	 System.out.println("Client connected");
	    	 BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
	    	 BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
	    	 Thread t = new MetaMapLiteServerHandler(socket, bis, bos, metaMapLiteInst);
	    	 t.start();
	     }
		 } catch(Exception e) {
			 e.printStackTrace();
		 }

	}
}
