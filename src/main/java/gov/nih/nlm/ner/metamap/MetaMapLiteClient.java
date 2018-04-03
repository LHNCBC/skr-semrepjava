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
import java.util.Properties;

public class MetaMapLiteClient {
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
		
		File configFile = new File("semrepjava.properties");
		if( configFile.exists() && !configFile.isDirectory()) {
			FileReader reader = new FileReader(configFile);
			Properties props = new Properties(System.getProperties());
			props.load(reader);
			System.setProperties(props);
		}
		
		int serverPort = Integer.parseInt(System.getProperty("server.port", "12345"));
		String serverName = System.getProperty("server.name", "indsrv2");
		String fileName = args[0];
		MetaMapLiteClient client = new MetaMapLiteClient();
		String inputText = new String(Files.readAllBytes(Paths.get(fileName)));
		System.out.println(inputText);
		Socket s = new Socket(serverName, serverPort);
		String answer = client.queryServer(s, inputText);
		//System.out.println(answer);
		
   }
}

