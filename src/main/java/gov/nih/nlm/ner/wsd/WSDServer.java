package gov.nih.nlm.ner.wsd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.nls.wsd.algorithms.AEC.AECMethod;

/**
 * Implementation of word sense disambiguation server program
 * 
 * @author Zeshan Peng
 *
 */

public class WSDServer {
	private static Logger log = Logger.getLogger(WSDServer.class.getName());	
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
		 
		int port = Integer.parseInt(System.getProperty("wsd.server.port", "6789"));
		AECMethod disambiguationMethod = new AECMethod();
		ServerSocket serverSocket = new ServerSocket(port); 
		
		try {
			System.out.println("WSD Server is running.");
			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("Client connected");
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
				Thread t = new WSDServerHandler(socket, bis, bos, disambiguationMethod);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		serverSocket.close();

	}
}

