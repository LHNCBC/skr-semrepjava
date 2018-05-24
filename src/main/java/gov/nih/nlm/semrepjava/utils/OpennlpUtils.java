package gov.nih.nlm.semrepjava.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;
import gov.nih.nlm.semrepjava.core.Chunk;
import gov.nih.nlm.semrepjava.core.ChunkedSentence;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class OpennlpUtils {

	public static String[] pos(String[] tokens) throws IOException{
    	InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-pos.bin.path", "data/models/en-pos-maxent.bin"));
    	POSModel posModel = new POSModel(modelIn);
    	POSTaggerME tagger = new POSTaggerME(posModel);
    	String tags[] = tagger.tag(tokens);
    	return tags;
	}
	
	public static String[] tokenization(String sentence) throws IOException{
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	String tokens[] = tokenizer.tokenize(sentence);
    	return tokens;
	}
	
	public static String[] lemmatization(String[] tokens, String[] tags) throws IOException {
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-lemmatizer.bin.path", "data/models/en-lemmatizer.txt"));
    	DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(modelIn);
    	String[] lemmas = lemmatizer.lemmatize(tokens, tags);
    	return lemmas;
	}
	
	public static String[] chunker(String[] tokens, String[] tags) throws IOException {
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-chunker.bin.path", "data/models/en-chunker.bin"));
    	ChunkerModel chunkerModel = new ChunkerModel(modelIn);
    	ChunkerME chunker = new ChunkerME(chunkerModel);
    	String chunkTags[] = chunker.chunk(tokens, tags);
//    	opennlp.tools.util.Span[] s = chunker.chunkAsSpans(tokens, tags);
    	return chunkTags;
	}
	
	
	public static List<Word> wording(String[] tokens, String[] tags) throws IOException{
    	
		List<Word> wordList = new ArrayList<Word>();
		String[] lemmas = lemmatization(tokens, tags);
		
    	Word w;
    	WordLexeme wl;
    	for (int i = 0; i < tokens.length; i++) {
    		if(lemmas[i].equals("O")) {
    			wl = new WordLexeme(tokens[i], tags[i]);
    		}else {
    			wl = new WordLexeme(lemmas[i], tags[i]);
    		}
    		w = new Word(tokens[i], tags[i], wl);
    		wordList.add(w);
    	}
    	return wordList;
	}
	
	public static void chunking(String[] tokens, String[] tags, ChunkedSentence s) throws IOException {
		List<Chunk> chunkList = new ArrayList<Chunk>();
		List<Word> wordList = new ArrayList<Word>();
		String[] lemmas = lemmatization(tokens, tags);
		String[] chunkTags = chunker(tokens, tags);
		
		Word w;
    	WordLexeme wl;
    	Chunk chunk = null;
    	List<Word> trail = null;
    	for (int i = 0; i < tokens.length; i++) {
    		String[] fields = chunkTags[i].split("-");
    		if(lemmas[i].equals("O")) {
    			wl = new WordLexeme(tokens[i], tags[i]);
    		}else {
    			wl = new WordLexeme(lemmas[i], tags[i]);
    		}
    		w = new Word(tokens[i], tags[i], wl);
    		wordList.add(w);
    		if(fields[0].equals("B")) {
    			if(chunk == null) {
    				trail = new ArrayList<Word>();
    				chunk = new Chunk(w, trail, fields[1]);
    			}else {
    				chunkList.add(chunk);
    				trail = new ArrayList<Word>();
    				chunk = new Chunk(w, trail, fields[1]);
    			}
    		}else if (fields[0].equals("I")) {
    			if (trail == null) {
    				trail = new ArrayList<Word>();
    			}
    			trail.add(w);
    		}else if (fields[0].equals("O")) {
    			if( chunk != null)
    				chunkList.add(chunk);
    			chunk = new Chunk(w, null, tags[i]);
    			chunkList.add(chunk);
    			chunk = null;
    			trail = null;
    		}
    		if (i == tokens.length - 1 && chunk != null) { 
    				chunkList.add(chunk);
    		}
    	}
    	s.setWords(wordList);
    	s.setChunks(chunkList);		
	}
	
	public static List<Sentence> sentenceSplit(String text) throws IOException{
		List<Sentence> sentList = new ArrayList<Sentence>();
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
    	SentenceModel model = new SentenceModel(modelIn);
    	SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
    	String sentences[] = sentenceDetector.sentDetect(text);
    	opennlp.tools.util.Span sentenceSpans[] = sentenceDetector.sentPosDetect(text);
    	ChunkedSentence s;
    	for (int i = 0; i < sentences.length; i++) {
    		System.out.println(sentences[i]);
    		s = new ChunkedSentence(Integer.toString(i), sentences[i], 
    					new gov.nih.nlm.ling.core.Span(sentenceSpans[i].getStart(), sentenceSpans[i].getEnd()));
    		String[] tokens = tokenization(sentences[i]);
    		String[] tags = pos(tokens);
    		chunking(tokens,tags, s);
    		sentList.add(s);
    	}
    	return sentList;
	}
}
