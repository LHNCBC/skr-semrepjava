package gov.nih.nlm.ner.metamap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.ner.MetaMapLite;

/**
 * This class handles client requests for MetaMapLite server
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
 *
 */

public class MetaMapLiteServerHandler extends Thread {
	private static Logger log = Logger.getLogger(MetaMapLiteServerHandler.class.getName());	
	
	final BufferedInputStream bis;
	final BufferedOutputStream bos;
	final Socket socket;
	MetaMapLite metaMapLiteInst;
	Map<String, String> semgroupMap;
	

	// Constructor
	public MetaMapLiteServerHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, MetaMapLite mtmpl, Map<String, String> semgroupMap) throws IOException {
		this.socket = s;
		this.bis = bis;
		this.bos = bos;
		this.metaMapLiteInst = mtmpl;
		this.semgroupMap = semgroupMap;
	}
	
	public String[] findSemanticGroups(String[] semTypes) {
		String[] semgroups = new String[semTypes.length];
		for(int i = 0; i < semTypes.length; i++) {
			semgroups[i] = semgroupMap.containsKey(semTypes[i]) ? semgroupMap.get(semTypes[i]) : "null";
		}
		return semgroups;
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
			String inputText = br.readLine();
			System.out.println("inputtext: " + inputText);
	    	BioCDocument document = FreeText.instantiateBioCDocument(inputText);
	    	List<Entity> entityList = metaMapLiteInst.processDocument(document);
	    	System.out.println("Entity size: " + entityList.size());
	    	
	    	StringBuilder sb = new StringBuilder(); 
	    	String[] semTypes;
	    	String[] semgroupinfos;
	    	for (Entity entity: entityList) {
	    		sb.append(entity.getStart() + ",," + entity.getLength() + ",,");
				for (Ev ev: entity.getEvSet()) {
					ConceptInfo ci = ev.getConceptInfo();
					int setSize = ci.getSemanticTypeSet().size();
					semTypes = ci.getSemanticTypeSet().toArray(new String[setSize]);
					semgroupinfos = findSemanticGroups(semTypes);
					sb.append(ci.getCUI() + ",," + 
							ci.getPreferredName() + ",," + 
							ev.getConceptString() + ",," +
							ev.getScore() + ",," +
							String.join("::", semTypes) + ",," + 
							String.join("::", semgroupinfos) + ",,");
				}
				sb.append(";;"); 
	    	}
	    	bw.print(sb.toString());
	    	bw.flush();
		} catch (Exception e) {
			log.severe("Unable to process text with MML Server.");
			e.printStackTrace();
		}

		try {
			// closing resources
			this.bis.close();
			this.bos.close();

		} catch (IOException e) {
			log.severe("Unable to close MML Server streams.");
			e.printStackTrace();
		}
	}
}
