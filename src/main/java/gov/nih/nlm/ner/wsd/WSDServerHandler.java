package gov.nih.nlm.ner.wsd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import gov.nih.nlm.nls.wsd.algorithms.AEC.AECMethod;
import gov.nih.nlm.nls.wsd.algorithms.MRD.CandidateCUI;

public class WSDServerHandler extends Thread {
	final BufferedInputStream bis;
	final BufferedOutputStream bos;
	final Socket socket;
	AECMethod methodInst;

	// Constructor
	public WSDServerHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, AECMethod method) throws IOException {
		this.socket = s;
		this.bis = bis;
		this.bos = bos;
		this.methodInst = method;
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
			String jsonString = br.readLine();
	    	JSONObject json = new JSONObject(jsonString);
	    	List<CandidateCUI> cuis = new ArrayList<CandidateCUI>();
	    	String text = (String) json.get("text");
	    	String cuisString = (String) json.get("cuis");
	    	JSONObject cuiJson = new JSONObject(cuisString);
	    	System.out.println(cuiJson.toString());
	    	Iterator<String> keys = cuiJson.keys();
	    	while(keys.hasNext()) {
	    		String key = keys.next();
	    		System.out.println(key);
	    		cuis.add(new CandidateCUI((String) cuiJson.get(key), key));
	    	}
	    	List<String> filteredNames = this.methodInst.disambiguate(cuis, text);
	    	json = new JSONObject();
	    	for(String name: filteredNames) {
	    		json.put(name, name);
	    	}
	    	bw.print(json.toString());
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

