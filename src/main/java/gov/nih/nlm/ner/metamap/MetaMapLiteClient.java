package gov.nih.nlm.ner.metamap;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.process.TermAnnotator;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ner.wsd.WSDClient;
import gov.nih.nlm.semrep.utils.SemRepUtils;

/**
 * Implementation of client for MetaMapLite(mml) server.
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
 *
 */

public class MetaMapLiteClient implements TermAnnotator{
	private static Logger log = Logger.getLogger(MetaMapLiteClient.class.getName());	
	
	private int mmlServerPort;
	private String mmlServerName;
	private static WSDClient wsd  =null;
	
	public MetaMapLiteClient(Properties props) {
		this.mmlServerPort = Integer.parseInt(props.getProperty("metamaplite.server.port", "12345"));
		this.mmlServerName = props.getProperty("metamaplite.server.name", "indsrv2");
		boolean useWsd = Boolean.parseBoolean(props.getProperty("metamaplite.useWsd","true"));
		if (useWsd) wsd = new WSDClient(props);
	}
	
	/**
	 * Create a valid socket object with given properties
	 * 
	 * @param props appropriate properties for mml server infos
	 * @return a valid socket object
	 */
	
/*	private Socket setEnvironment(Properties props) {
		this.serverPort = Integer.parseInt(props.getProperty("metamaplite.server.port", "12345"));
		this.serverName = props.getProperty("metamaplite.server.name", "indsrv2");
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
	 * Implementation of annotate function in TermAnnotator interface
	 */
	
	public void annotate(Document document, Properties props, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
		Socket s = 	SemRepUtils.getSocket(mmlServerName, mmlServerPort);
		if (s == null) return;
		log.finest("Processing document with MetaMapLite..." + document.getId());
		long mmbeg = System.currentTimeMillis();
		Map<SpanList, LinkedHashSet<Ontology>> temp = new HashMap<>();
		String inputText = document.getText();
		String answer = SemRepUtils.queryServer(s, inputText);
		if (answer != null) {
			String[] entities = answer.split(";;");
			String[] fields;
			String cui;
			String name;
			ScoredUMLSConcept concept;
			String conceptString;
			double score;
			int start;
			int length;
			SpanList sl;
			LinkedHashSet<String> semTypes;
			LinkedHashSet<String> semgroups;
			LinkedHashSet<Ontology> onts;
			for(int i = 0; i < entities.length; i++) {
				onts = new LinkedHashSet<Ontology>();
				fields = entities[i].split(",,");
				if (fields.length < 6) {
					log.severe("Error parsing MML server string " + answer);
					return;
				}
				start = Integer.parseInt(fields[0]);
				length = Integer.parseInt(fields[1]);
				sl = new SpanList(start, start+length);
				int cursorIndex = 2;
				do {
					cui = fields[cursorIndex];
					name = fields[cursorIndex + 1];
					conceptString = fields[cursorIndex + 2];
					System.out.println(name + " | " + conceptString);
					score = Double.parseDouble(fields[cursorIndex + 3]);
					semTypes = new LinkedHashSet<String>(Arrays.asList(fields[cursorIndex + 4].split("::")));
					semgroups = new LinkedHashSet<String>(Arrays.asList(fields[cursorIndex + 5].split("::")));
					concept = new ScoredUMLSConcept(cui,name,semTypes,semgroups,"metamaplite",conceptString,score);
					cursorIndex += 6;
					onts.add(concept);
				} while(cursorIndex < fields.length && !fields[cursorIndex].isEmpty());
				temp.put(sl, onts);
			}			
			long mmend = System.currentTimeMillis();
			log.info("Completed processing document with MetaMapLite " + document.getId() + " .. " +(mmend-mmbeg) + " msec.");
			if (wsd != null) {
				annotations.putAll(wsd.disambiguate2(document, props, temp));
			} else {
				annotations.putAll(temp);
			}
			SemRepUtils.closeSocket(s);
		}
		
	}
}

