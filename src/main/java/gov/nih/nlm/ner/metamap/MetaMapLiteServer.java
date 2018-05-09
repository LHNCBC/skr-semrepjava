package gov.nih.nlm.ner.metamap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.ner.MetaMapLite;

public class MetaMapLiteServer {

	public static Map<String,String> setOutputExtensionMap(){
		Map<String,String> outputExtensionMap = new HashMap<String,String>();
	    outputExtensionMap.put("bioc",".bioc");
	    outputExtensionMap.put("brat",".ann");
	    outputExtensionMap.put("mmi",".mmi");
	    outputExtensionMap.put("cdi",".cdi");
	    outputExtensionMap.put("cuilist",".cuis");
	    return outputExtensionMap;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException {
		
		
		Properties  optionProps = new Properties(System.getProperties());
		Map<String,String> outputExtensionMap = setOutputExtensionMap();
		String configFilename = "semrepjava.properties";
		if(args.length > 0) {
			
			int i = 0;
			while( i < args.length) {
				if (args[i].substring(0, 2).equals("--")) {
					String[] fields = args[i].split("=");
					if(fields[0].equals("--configfile")) {
						configFilename = fields[1];
					}else if (fields[0].equals("--inputformat")) {
						optionProps.setProperty("metamaplite.document.inputtype", fields[1]);
					}else if (fields[0].equals("--freetext")) {
						optionProps.setProperty("metamaplite.document.inputtype", "freetext");
					}else if (fields[0].equals("--bioc") || 
						       fields[0].equals("--cdi") || 
						       fields[0].equals("--bc") || 
						       fields[0].equals("--bcevaluate")) {
					      optionProps.setProperty("metamaplite.outputformat","bioc");
					      optionProps.setProperty("metamaplite.outputextension",
									       (outputExtensionMap.containsKey("cdi") ?
										outputExtensionMap.get("cdi") :
										".ann"));
					}else if (fields[0].equals("--brat") || 
						       fields[0].equals("--BRAT")) {
					      optionProps.setProperty("metamaplite.outputformat","brat");
					      optionProps.setProperty("metamaplite.outputextension",
									       (outputExtensionMap.containsKey("brat") ?
										outputExtensionMap.get("brat") :
										".ann"));
					}else if (fields[0].equals("--mmi") || 
						       fields[0].equals("--mmilike")) {
					      optionProps.setProperty("metamaplite.outputformat","mmi");
					      optionProps.setProperty("metamaplite.outputextension",
									       (outputExtensionMap.containsKey("mmi") ?
										outputExtensionMap.get("mmi") :
										".mmi"));
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
		System.setProperties(finalProps);
		MetaMapLite.expandIndexDir(finalProps, System.getProperty("index.dir.name", "data/ivf/2017AA/Base/strict"));
		 
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		serverSocket.close();

	}
}
