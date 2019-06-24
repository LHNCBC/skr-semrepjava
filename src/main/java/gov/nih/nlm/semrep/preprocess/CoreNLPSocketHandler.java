package gov.nih.nlm.semrep.preprocess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLPSocketHandler extends Thread {
    final BufferedInputStream bis;
    final BufferedOutputStream bos;
    final Socket socket;
    static CoreNLPServer cnp;
    private static StanfordCoreNLP pipeline;

    public CoreNLPSocketHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, StanfordCoreNLP pipeline) {
	this.socket = s;
	this.bis = bis;
	this.bos = bos;
	this.cnp = cnp;
	this.pipeline = pipeline;
    }

    @Override
    public void run() {
	String received;
	String toreturn;
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(bis));
	    // PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
	    ObjectOutputStream ostream = new ObjectOutputStream(socket.getOutputStream());
	    String line;
	    while (true) {
		line = br.readLine();
		if (line != null && !line.equals("quit")) {
		    System.out.println("Input: " + line);
		    long startTime = System.currentTimeMillis();
		    // String text = FileUtils.stringFromFileWithBytes(inFile, "UTF-8");
		    Annotation annotation = new Annotation(line.trim());
		    pipeline.annotate(annotation);
		    List<CoreMap> sentenceAnns = annotation.get(SentencesAnnotation.class);
		    SentenceAnnsSO serializable = new SentenceAnnsSO(sentenceAnns);
		    long estimatedTime = System.currentTimeMillis();
		    System.out.println("Elapsed time: " + estimatedTime + " milisec.");
		    // System.out.println("NLP Output: " + result);
		    // result = result.replaceAll("\\n", " ");
		    ostream.writeObject(serializable);
		    ostream.flush();
		    // bw.flush();
		} else {
		    continue;
		}
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
