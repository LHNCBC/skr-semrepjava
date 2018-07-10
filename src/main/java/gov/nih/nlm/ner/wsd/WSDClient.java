package gov.nih.nlm.ner.wsd;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.MultiWord;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.sem.Concept;
import gov.nih.nlm.ling.sem.Entity;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.nls.wsd.algorithms.MRD.CandidateCUI;
import gov.nih.nlm.semrepjava.core.UMLSConcept;

public class WSDClient {
	
	private int serverPort;
	private String serverName;
	
	private Socket setEnvironment(Properties props) {
		this.serverPort = Integer.parseInt(props.getProperty("wsd.server.port", "6789"));
		this.serverName = props.getProperty("wsd.server.name", "indsrv2");
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
	
	private UMLSConcept findBestMatchConcept(List<String> names, Map<String, UMLSConcept> map) {
		UMLSConcept bestConcept = map.get(names.get(0));
		for(String name : names) {
			if(map.get(name).getConceptString().compareTo(bestConcept.getConceptString()) < 0)
				bestConcept = map.get(name);
		}
		return bestConcept;
	}
	
	public void disambiguate(Document doc, Properties props, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
		Socket s = setEnvironment(props);
		String text = doc.getText();
		LinkedHashSet<Ontology> onts;
		Iterator<Ontology> itr;
		UMLSConcept sense,concept;
		Entity entity;
		Set<Concept> conceptSet = new HashSet<Concept>();
		JSONObject json,cuiJson;
		List<Word> wordList;
		SpanList headSpan;
		
		for(SpanList sl: annotations.keySet()) {
			onts = annotations.get(sl);
			wordList = doc.getWordsInSpan(sl);
			headSpan = MultiWord.findHeadFromCategory(wordList).getSpan();
			itr = onts.iterator();
			if(onts.size() <= 1) {
				sense = (UMLSConcept) itr.next();
				conceptSet.add(sense);
				entity = doc.getSemanticItemFactory().newEntity(doc, sl, headSpan, sense.getSemtypes().toString(), conceptSet, sense);
			}else {
				cuiJson = new JSONObject();
				Map<String, UMLSConcept> nameConceptMap = new HashMap<String, UMLSConcept>();
				while(itr.hasNext()) {
					concept = (UMLSConcept) itr.next();
					nameConceptMap.put(concept.getName(), concept);
					cuiJson.put(concept.getId(), concept.getName());
					conceptSet.add(concept);
				}
				json = new JSONObject(); 
				json.put("text", text);
				json.put("cuis", cuiJson.toString());
				String answer = s == null ? null : queryServer(s, json.toString());
				if(answer != null) {
					List<String> filteredNames = new ArrayList<String>();
					json = new JSONObject(answer);
			    	Iterator<String> keys = json.keys();
			    	while(keys.hasNext()) {
			    		String key = keys.next();
			    		filteredNames.add(key);
			    	}
					if (filteredNames.size() == 1) {
						sense = nameConceptMap.get(filteredNames.get(0));
						entity = doc.getSemanticItemFactory().newEntity(doc, sl, headSpan, sense.getSemtypes().toString(), conceptSet, sense);
					}else {
						sense = findBestMatchConcept(filteredNames, nameConceptMap);
						entity = doc.getSemanticItemFactory().newEntity(doc, sl, headSpan, sense.getSemtypes().toString(), conceptSet, sense);
					}
				}else {
					System.out.println("failed to create socket");
				}

			}
		}
		
	}
}

