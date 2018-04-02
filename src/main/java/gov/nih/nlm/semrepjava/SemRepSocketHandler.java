package gov.nih.nlm.semrepjava;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import bioc.BioCDocument;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.ner.MetaMapLite;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;

public class SemRepSocketHandler extends Thread {
	final BufferedInputStream bis;
	final BufferedOutputStream bos;
	final Socket socket;
	MetaMapLite metaMapLiteInst;

	// Constructor
	public SemRepSocketHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, MetaMapLite mtmpl) throws IOException {
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
	    	InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
	    	SentenceModel model = new SentenceModel(modelIn);
	    	SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
	    	String sentences[] = sentenceDetector.sentDetect(inputText);
	    	//Span sentencesSpan[] = sentenceDetector.sentPosDetect(inputText);
	    	
	    	modelIn = new FileInputStream(System.getProperty("opennlp.en-token.bin.path", "./data/models/en-token.bin"));
	    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
	    	Tokenizer tokenizer = new TokenizerME(tokenModel);
	    	String tokens[] = tokenizer.tokenize(inputText);
	    	//Span tokenSpans[] = tokenizer.tokenizePos(inputText);
	    	
	    	modelIn = new FileInputStream(System.getProperty("opennlp.en-pos.bin.path", "data/models/en-pos-maxent.bin"));
	    	POSModel posModel = new POSModel(modelIn);
	    	POSTaggerME tagger = new POSTaggerME(posModel);
	    	String tags[] = tagger.tag(tokens);
	    	//double probs[] = tagger.probs();
	    	//Sequence topSequences[] = tagger.topKSequences(tokens);
	    	
	    	modelIn = new FileInputStream(System.getProperty("opennlp.en-chunker.bin.path", "data/models/en-chunker.bin"));
	    	ChunkerModel chunkerModel = new ChunkerModel(modelIn);
	    	ChunkerME chunker = new ChunkerME(chunkerModel);
	    	String chunkerTags[] = chunker.chunk(tokens, tags);
	    	//double chunkerProbs[] = chunker.probs();
	    	
	    	System.out.println(inputText);
	    	BioCDocument document = FreeText.instantiateBioCDocument(inputText);
	    	List<Entity> entityList = metaMapLiteInst.processDocument(document);
	    	StringBuilder sb = new StringBuilder("Concepts: \n"); 
	    	for (Entity entity: entityList) {
	    	  for (Ev ev: entity.getEvSet()) {
	    		  sb.append(ev.getText() + " " + ev.getMatchedText() + " " + ev.getConceptInfo().getCUI() + "\n");
	    	 	//System.out.print(ev.getConceptInfo().getCUI() + "|" + entity.getMatchedText());
	    	    //System.out.println();
	    	  }
	    	}
	    	
	    	sb.append("Sentences: \n" + Arrays.toString(sentences) + "\n" 
	    					+ "Tokens: \n" + Arrays.toString(tokens) + "\n"
	    					+ "POS Tags: \n" + Arrays.toString(tags) + "\n"
	    					+ "Chunk Tags: \n" + Arrays.toString(chunkerTags) + "\n");
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
