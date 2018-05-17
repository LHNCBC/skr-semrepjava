package gov.nih.nlm.ner.metamap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import gov.nih.nlm.nls.ner.MetaMapLite;

public class MetaMapLiteServer {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException {
		
		
		Properties  optionProps = new Properties();
		String configFilename = "semrepjava.properties";
		if(args.length > 0) {
			int i = 0;
			while( i < args.length) {
				if (args[i].substring(0, 2).equals("--")) {
					String[] fields = args[i].split("=");
					if(fields[0].equals("--configfile")) {
						configFilename = fields[1];
					}else if (fields[0].equals("--indexdir")) {
					      optionProps.setProperty ("index.dir.name",fields[1]);
				    } else if (fields[0].equals("--modelsdir")) {
				      optionProps.setProperty ("opennlp.models.dir",fields[1]);
				    }
				}
				i++;
			}
			
		}
		
		
		Properties finalProps = new Properties();
		File configFile = new File(configFilename);
		if( configFile.exists() && !configFile.isDirectory()) {
			 finalProps.load(new FileReader(configFile));
		}
		finalProps.putAll(optionProps);
		System.getProperties().putAll(finalProps);
		MetaMapLite.expandIndexDir(finalProps, System.getProperty("index.dir.name", "data/ivf/2017AA/Base/strict"));
		 
		MetaMapLite metaMapLiteInst = new MetaMapLite(System.getProperties());
		 
		int port = Integer.parseInt(System.getProperty("server.port", "12345"));
			
		ServerSocket serverSocket = new ServerSocket(port); 
		
		try {
			System.out.println("Server is running.");
			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("Client connected");
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
				Thread t = new MetaMapLiteServerHandler(socket, bis, bos, metaMapLiteInst);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		serverSocket.close();

	}
}
