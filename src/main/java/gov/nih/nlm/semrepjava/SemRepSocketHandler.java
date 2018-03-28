package gov.nih.nlm.semrepjava;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

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

	// Constructor
	public SemRepSocketHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos) {
		this.socket = s;
		this.bis = bis;
		this.bos = bos;
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
			String inputText = br.readLine();
	    	InputStream modelIn = new FileInputStream("/export/home/pengz3/eclipse-workspace/semrepjava/opennlp pretrained models/en-sent.bin");
	    	SentenceModel model = new SentenceModel(modelIn);
	    	SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
	    	String sentences[] = sentenceDetector.sentDetect(inputText);
	    	//Span sentencesSpan[] = sentenceDetector.sentPosDetect(inputText);
	    	
	    	modelIn = new FileInputStream("/export/home/pengz3/eclipse-workspace/semrepjava/opennlp pretrained models/en-token.bin");
	    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
	    	Tokenizer tokenizer = new TokenizerME(tokenModel);
	    	String tokens[] = tokenizer.tokenize(inputText);
	    	//Span tokenSpans[] = tokenizer.tokenizePos(inputText);
	    	
	    	modelIn = new FileInputStream("/export/home/pengz3/eclipse-workspace/semrepjava/opennlp pretrained models/en-pos-maxent.bin");
	    	POSModel posModel = new POSModel(modelIn);
	    	POSTaggerME tagger = new POSTaggerME(posModel);
	    	String tags[] = tagger.tag(tokens);
	    	//double probs[] = tagger.probs();
	    	//Sequence topSequences[] = tagger.topKSequences(tokens);
	    	
	    	modelIn = new FileInputStream("/export/home/pengz3/eclipse-workspace/semrepjava/opennlp pretrained models/en-chunker.bin");
	    	ChunkerModel chunkerModel = new ChunkerModel(modelIn);
	    	ChunkerME chunker = new ChunkerME(chunkerModel);
	    	String chunkerTags[] = chunker.chunk(tokens, tags);
	    	//double chunkerProbs[] = chunker.probs();
	    	
	    	String result = "Sentences: \n" + Arrays.toString(sentences) + "\n" 
	    					+ "Tokens: \n" + Arrays.toString(tokens) + "\n"
	    					+ "POS Tags: \n" + Arrays.toString(tags) + "\n"
	    					+ "Chunk Tags: \n" + Arrays.toString(chunkerTags) + "\n";
	    	System.out.println(result);
	    	bw.print(result);
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
