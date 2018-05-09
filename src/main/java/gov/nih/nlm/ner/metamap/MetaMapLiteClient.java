package gov.nih.nlm.ner.metamap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MetaMapLiteClient {
	
	public static Map<String,String> setOutputExtensionMap(){
		Map<String,String> outputExtensionMap = new HashMap<String,String>();
	    outputExtensionMap.put("bioc",".bioc");
	    outputExtensionMap.put("brat",".ann");
	    outputExtensionMap.put("mmi",".mmi");
	    outputExtensionMap.put("cdi",".cdi");
	    outputExtensionMap.put("cuilist",".cuis");
	    return outputExtensionMap;
	}
	
	private String queryServer(Socket socket,String input) {
		 StringBuilder sb = new StringBuilder();  
		try {
			// write text to the socket
			DataInputStream bis = new DataInputStream(socket.getInputStream());
        	BufferedReader br = new BufferedReader(new InputStreamReader(bis));
    	 	PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
        	bw.println(input);
        	bw.flush();
        	String line;
        	while ((line = br.readLine()) != null) {
        		System.out.println(line);
        	}
        	bis.close();
        	br.close();

		} catch (IOException ioe) {
			System.err.println("Socket error");
		}
	      return sb.toString();
	}
	
	  
	public static void main(String args[]) throws IOException {
		
		Properties  optionProps = new Properties(System.getProperties());
		Map<String,String> outputExtensionMap = setOutputExtensionMap();
		String configFilename = "semrepjava.properties";
		String inputFilename = null;
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
			inputFilename = args[i];
		}
		
		File inputFile = new File(inputFilename);
		if(!inputFile.exists()) {
			System.out.println("Input file path is invalid or not provided.");
			System.exit(1);
		}else {
			Properties finalProps = new Properties();
			File configFile = new File(configFilename);
			if( configFile.exists() && !configFile.isDirectory()) {
				 finalProps.load(new FileReader(configFile));
			}
			finalProps.putAll(optionProps);
			System.setProperties(finalProps);
			
			int serverPort = Integer.parseInt(System.getProperty("server.port", "12345"));
			String serverName = System.getProperty("server.name", "indsrv2");
			MetaMapLiteClient client = new MetaMapLiteClient();
			String inputText = new String(Files.readAllBytes(Paths.get(inputFilename)));
			System.out.println(inputText);
			Socket s = new Socket(serverName, serverPort);
			String answer = client.queryServer(s, inputText);
			//System.out.println(answer);
		}
		
   }
}

