package gov.nih.nlm.ner.wsd;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONObject;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Concept;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ner.metamap.ScoredUMLSConcept;
import gov.nih.nlm.semrep.utils.SemRepUtils;

/**
 * Implementation of client for word sense disambiguation(wsd) server for MetaMapLite
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
 *
 */

public class WSDClient {
	private static Logger log = Logger.getLogger(WSDClient.class.getName());	
	
	private int serverPort;
	private String serverName;
	
	public WSDClient(Properties props) {
		this.serverPort = Integer.parseInt(props.getProperty("wsd.server.port", "6789"));
		this.serverName = props.getProperty("wsd.server.name", "indsrv2");
	}
	
	/**
	 * Create a valid socket object with given properties
	 * 
	 * @param props appropriate properties for wsd server infos
	 * @return a valid socket object
	 */
	
/*	private Socket setEnvironment(Properties props) {
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
	}*/
	
	/**
	 * Query wsd server with the given socket and input string
	 * 
	 * @param socket the socket connected with the wsd server
	 * @param input string to be processed by wsd
	 * @return string returned by wsd server program
	 */
	
/*	private String queryServer(Socket socket,String input) {
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
        		if(line != null && line.isEmpty())
        			System.out.println("yes");
        	}while(line != null);
        	bis.close();
        	br.close();

		} catch (IOException ioe) {
			System.err.println("Socket error haha");
		}
	      return sb.toString();
	}*/
	
	/**
	 * Find the the best matched concept name from a list of preferred names
	 * 
	 * @param names the list of preferred names
	 * @param map the map from concept name to concept object
	 * @return the concept object which has the best matched concept name
	 */
	
	private ScoredUMLSConcept findBestMatchConcept(List<String> names, Map<String, ScoredUMLSConcept> map) {
		ScoredUMLSConcept bestConcept = map.get(names.get(0));
		for(String name : names) {
			if(map.get(name).getConceptString().compareTo(bestConcept.getConceptString()) < 0)
				bestConcept = map.get(name);
		}
		return bestConcept;
	}
	
	/**
	 * Disambiguate concepts names if there are more than one concept names associated with a string
	 * 
	 * @param doc the document object to be disambiguated
	 * @param props the appropriate properties to be used for the disambiguation
	 * @param annotations the map from spanlist object to ontologies set
	 */
/*	public void disambiguate(Document doc, Properties props, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
		String text = doc.getText();
		LinkedHashSet<Ontology> onts;
		Iterator<Ontology> itr;
		ScoredUMLSConcept sense,concept;
		Entity entity;
		Set<Concept> conceptSet = new HashSet<Concept>();
		JSONObject json,cuiJson;
		List<Word> wordList;
		SpanList headSpan;
		
		for(SpanList sl: annotations.keySet()) {
			Socket s = SemRepUtils.getSocket(serverName, serverPort);
			if (s == null) continue;
//			Socket s = setEnvironment(props);
			onts = annotations.get(sl);
			wordList = doc.getWordsInSpan(sl);
			headSpan = MultiWord.findHeadFromCategory(wordList).getSpan();
			itr = onts.iterator();
			if(onts.size() <= 1) {
				sense = (ScoredUMLSConcept) itr.next();
				conceptSet.add(sense);
				entity = doc.getSemanticItemFactory().newEntity(doc, sl, headSpan, sense.getSemtypes().toString(), conceptSet, sense);
			} else {
				cuiJson = new JSONObject();
				Map<String, ScoredUMLSConcept> nameConceptMap = new HashMap<String, ScoredUMLSConcept>();
				while(itr.hasNext()) {
					concept = (ScoredUMLSConcept) itr.next();
					nameConceptMap.put(concept.getName(), concept);
					cuiJson.put(concept.getId(), concept.getName());
					conceptSet.add(concept);
				}
				json = new JSONObject(); 
				json.put("text", text);
				json.put("cuis", cuiJson.toString());
				String answer = SemRepUtils. queryServer(s, json.toString());
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
				}

			}
			SemRepUtils.closeSocket(s);
		}

	}*/
	public Map<SpanList, LinkedHashSet<Ontology>> disambiguate2(Document doc, Properties props, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
		long wsdbeg = System.currentTimeMillis();
		log.finest("Disambiguating UMLS concepts..." + doc.getId());
		String text = doc.getText();
		LinkedHashSet<Ontology> onts;
		Iterator<Ontology> itr;
		ScoredUMLSConcept sense,concept;
		Set<Concept> conceptSet = new HashSet<Concept>();
		JSONObject json,cuiJson;
		Map<SpanList, LinkedHashSet<Ontology>> outAnnotations = new HashMap<>();
		for(SpanList sl: annotations.keySet()) {
			Socket s = SemRepUtils.getSocket(serverName, serverPort);
			if (s == null) { 
				outAnnotations.put(sl, annotations.get(sl));
				continue;
			}
			onts = annotations.get(sl);
			itr = onts.iterator();
			if(onts.size() <= 1) {
				outAnnotations.put(sl, annotations.get(sl));
				continue;
			} 
			cuiJson = new JSONObject();
			Map<String, ScoredUMLSConcept> nameConceptMap = new HashMap<String, ScoredUMLSConcept>();
			while(itr.hasNext()) {
				Ontology conc = itr.next();
				if (conc instanceof ScoredUMLSConcept) {
					concept = (ScoredUMLSConcept)conc;
					nameConceptMap.put(concept.getName(), concept);
					cuiJson.put(concept.getId(), concept.getName());
					conceptSet.add(concept);
				}
			}
			json = new JSONObject(); 
			json.put("text", text);
			json.put("cuis", cuiJson.toString());
			String answer = SemRepUtils. queryServer(s, json.toString());
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
					}else {
						sense = findBestMatchConcept(filteredNames, nameConceptMap);
					}
				LinkedHashSet<Ontology> disambiguated = new LinkedHashSet<>();
				itr = onts.iterator();
				while (itr.hasNext()) {
					Ontology conc = itr.next();
					if (conc.equals(sense)) disambiguated.add(conc);
					else if (conc instanceof ScoredUMLSConcept == false) disambiguated.add(conc);
				}
				outAnnotations.put(sl, disambiguated);
			}
			SemRepUtils.closeSocket(s);
		}
		long wsdend = System.currentTimeMillis();
		log.info("Completed disambiguating " + doc.getId() + " .. " +(wsdend-wsdbeg) + " msec.");
		return outAnnotations;

	}
}

