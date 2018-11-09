package gov.nih.nlm.ner.metamap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.nls.ner.MetaMapLite;

/**
 * Implementation of MetaMapLite server program
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
 *
 */

public class MetaMapLiteServer {
	private static Logger log = Logger.getLogger(MetaMapLiteServer.class.getName());
	private static Map<String, String> semgroupMap = new HashMap<String, String>(); 
	
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
					      optionProps.setProperty ("metamaplite.index.dir.name",fields[1]);
				    } else if (fields[0].equals("--modelsdir")) {
				      optionProps.setProperty ("opennlp.models.dir",fields[1]);
				    }
				}
				i++;
			}
			
		}
		
		Properties finalProps = FileUtils.loadPropertiesFromFile(configFilename);
/*		File configFile = new File(configFilename);
     	if( configFile.exists() && !configFile.isDirectory()) {
			 finalProps.load(new FileReader(configFile));
		}
		finalProps.putAll(optionProps);*/
		
		System.getProperties().putAll(finalProps);
		MetaMapLite.expandIndexDir(finalProps, System.getProperty("metamaplite.index.dir.name", "data/ivf/2017AA/Base/strict"));
		 
		MetaMapLite metaMapLiteInst = new MetaMapLite(System.getProperties());
		int port = Integer.parseInt(System.getProperty("metamaplite.server.port", "12345"));
		
		initializeSemGroupMap();
			
		ServerSocket serverSocket = new ServerSocket(port); 
		log.info("MML Server initialized...");
		try {
			while(true) {
				Socket socket = serverSocket.accept();
				log.finest("MMLClient connected..");
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
				Thread t = new MetaMapLiteServerHandler(socket, bis, bos, metaMapLiteInst, semgroupMap);
				t.start();
			}
		} catch (Exception e) {
			log.severe("Unable to accept and process MML Client request.");
			e.printStackTrace();
		}
		serverSocket.close();

	}
	
	/**
	 * initialize a map in order to find semantic group info for each semantic type
	 * @throws IOException if semgroupinfo file is not found or cannot be read
	 */
	
	private static void initializeSemGroupMap() throws IOException {
		String filename = System.getProperty("semgroupinfo", "semgroupinfo.txt");
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while((line = br.readLine()) != null) {
			int start = line.indexOf('(');
			int end = line.indexOf(')');
			String[] tokens = line.substring(start+1, end).split(",");
			semgroupMap.put(tokens[0], tokens[1]);
		}
		br.close();
	}
}
