package gov.nih.nlm.ner.metamap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.process.TermAnnotator;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.semrepjava.core.UMLSConcept;

public class MetaMapLiteClient implements TermAnnotator{
	
	private int serverPort;
	private String serverName;
	
	private Socket setEnvironment(Properties props) {
		this.serverPort = Integer.parseInt(props.getProperty("server.port", "12345"));
		this.serverName = props.getProperty("server.name", "indsrv2");
		try {
			return new Socket(this.serverName, this.serverPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
        	String line = br.readLine();
        	do {
        		//System.out.println(line);
        		sb.append(line);
        		line = br.readLine();
        	}while(line != null);
        	bis.close();
        	br.close();

		} catch (IOException ioe) {
			System.err.println("Socket error");
		}
	      return sb.toString();
	}
	
	public void annotate(Document document, Properties props, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
		
		Socket s = setEnvironment(props);
		String inputText = document.getText();
		String answer = s == null ? null : queryServer(s, inputText);
		if (answer != null) {
			System.out.println(answer);
			String[] entities = answer.split(";;");
			String[] fields;
			String id;
			String name;
			UMLSConcept concept;
			String conceptString;
			double score;
			int start;
			int length;
			SpanList sl;
			LinkedHashSet<String> semTypes;
			LinkedHashSet<Ontology> onts;
			for(int i = 0; i < entities.length; i++) {
				onts = new LinkedHashSet<Ontology>();
				fields = entities[i].split(",,");
				if (fields.length < 5) {
					System.out.println("Error parsing server back string.");
					System.exit(3);
				}
				start = Integer.parseInt(fields[0]);
				length = Integer.parseInt(fields[1]);
				sl = new SpanList(start, start+length);
				int cursorIndex = 2;
				do {
					id = fields[cursorIndex];
					name = fields[cursorIndex + 1];
					conceptString = fields[cursorIndex + 2];
					System.out.println(name + " | " + conceptString);
					score = Double.parseDouble(fields[cursorIndex + 3]);
					semTypes = new LinkedHashSet<String>(Arrays.asList(fields[cursorIndex + 4].split("::")));
					concept = new UMLSConcept(id,name,semTypes,"metamaplite",conceptString,score);
					cursorIndex += 5;
					onts.add(concept);
				}while(cursorIndex < fields.length && !fields[cursorIndex].isEmpty());
				annotations.put(sl, onts);
			}
			System.out.println("Parse succeed.");
		}else {
			System.out.println("failed to create socket");
		}
		
	}
}

