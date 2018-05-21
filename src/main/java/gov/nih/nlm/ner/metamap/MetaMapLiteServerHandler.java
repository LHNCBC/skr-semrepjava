package gov.nih.nlm.ner.metamap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.ner.MetaMapLite;

public class MetaMapLiteServerHandler extends Thread {
	final BufferedInputStream bis;
	final BufferedOutputStream bos;
	final Socket socket;
	MetaMapLite metaMapLiteInst;

	// Constructor
	public MetaMapLiteServerHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, MetaMapLite mtmpl) throws IOException {
		this.socket = s;
		this.bis = bis;
		this.bos = bos;
		this.metaMapLiteInst = mtmpl;
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
			String inputText = br.readLine();
	    	BioCDocument document = FreeText.instantiateBioCDocument(inputText);
	    	List<Entity> entityList = metaMapLiteInst.processDocument(document);
	    	
	    	StringBuilder sb = new StringBuilder(); 
	    	for (Entity entity: entityList) {
	    		sb.append(entity.getStart() + ",," + entity.getLength() + ",,");
				for (Ev ev: entity.getEvSet()) {
					ConceptInfo ci = ev.getConceptInfo();
					int setSize = ci.getSemanticTypeSet().size();
					sb.append(ci.getCUI() + ",," + 
							ci.getPreferredName() + ",," + 
							ev.getConceptString() + ",," +
							ev.getScore() + ",," +
							String.join("::", ci.getSemanticTypeSet().toArray(new String[setSize])) + ",,");
				}
				sb.append(";;"); 
	    	}
	    	System.out.println(sb.toString());
	    	bw.print(sb.toString());
	    	bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// closing resources
			this.bis.close();
			this.bos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
