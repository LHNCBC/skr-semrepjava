package gov.nih.nlm.semrep.preprocess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import gov.nih.nlm.ling.core.Document;

public class SocketHandler extends Thread {
    final BufferedInputStream bis;
    final BufferedOutputStream bos;
    final Socket socket;
    static CoreNLPServer cnp;

    public SocketHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, CoreNLPServer cnp) {
	this.socket = s;
	this.bis = bis;
	this.bos = bos;
	this.cnp = cnp;
    }

    @Override
    public void run() {
	String received;
	String toreturn;
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(bis));
	    PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
	    String line;
	    StringBuffer sb = new StringBuffer();
	    String PMID = null;
	    while (true) {
		line = br.readLine();
		if (line != null && !line.equals("quit")) {
		    if (line != null && line.equals("EOPF")) {
			// System.out.println("Input: " + sb.toString());
			long startTime = System.currentTimeMillis();
			// String text = FileUtils.stringFromFileWithBytes(inFile, "UTF-8");
			if (PMID == null) // If PMID is not provided
			    PMID = new String("NULL_PMID");
			Document doc = new Document(PMID, sb.toString());
			cnp.coreNLP(doc);
			String result = doc.toXml().toXML();
			long estimatedTime = System.currentTimeMillis() - startTime;
			// System.out.println("Elapsed time: " + estimatedTime + " milisec.");
			// System.out.println("NLP Output: " + result);
			result = result.replaceAll("\\n", " ");
			bw.println(result);
			bw.flush();
			sb = new StringBuffer();
		    } else {
			if (line.startsWith("PMID-")) {
			    PMID = line.substring(6);
			    System.out.println("PMID : " + PMID);
			}
			sb.append(line + "\n");
			continue;
		    }
		} else
		    break;
	    } // end of inner while
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
