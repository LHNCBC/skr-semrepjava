package gov.nih.nlm.semrep.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;
import gov.nih.nlm.semrep.core.Chunk;
import gov.nih.nlm.semrep.core.ChunkedSentence;
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

/**
 * This class contains functions for processing strings using opennlp
 * 
 * @author Zeshan Peng
 *
 */
public class OpennlpUtils {

	/**
	 * Compute part-of-speech tags for the given tokens
	 * 
	 * @param tokens token information 
	 * @return an array of part-of-speech tags
	 * @throws IOException if part-of-speech model file is not found
	 */
	public static String[] pos(String[] tokens) throws IOException{
    	InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-pos.bin.path", "data/models/en-pos-maxent.bin"));
    	POSModel posModel = new POSModel(modelIn);
    	POSTaggerME tagger = new POSTaggerME(posModel);
    	String tags[] = tagger.tag(tokens);
    	return tags;
	}
	
	/**
	 * Compute token information for the given string
	 * 
	 * @param sentence the string to be processed
	 * @return an array of token strings
	 * @throws IOException if token model file is not found
	 */
	
	public static String[] tokenization(String sentence) throws IOException{
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	String tokens[] = tokenizer.tokenize(sentence);
    	return tokens;
	}
	
	/**
	 * Compute token spans for the given string
	 * 
	 * @param sentence the string to be processed
	 * @return an array of spans
	 * @throws IOException if token model file is not found
	 */
	public static opennlp.tools.util.Span[] getTokenSpans(String sentence) throws IOException{
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	return tokenizer.tokenizePos(sentence);
	}
	
	/**
	 * Compute lemmas for the given tokens and part-of-speech tags
	 * 
	 * @param tokens the given tokens
	 * @param tags the given part-of-speech tags
	 * @return an array of lemma strings
	 * @throws IOException if lemmatizer model file is not found
	 */
	public static String[] lemmatization(String[] tokens, String[] tags) throws IOException {
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-lemmatizer.bin.path", "data/models/en-lemmatizer.bin"));
    	DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(modelIn);
    	String[] lemmas = lemmatizer.lemmatize(tokens, tags);
    	return lemmas;
	}
	
	/**
	 * Compute chunk tags for the given tokens and part-of-speech tags
	 * 
	 * @param tokens the given tokens
	 * @param tags the given part-of-speech tags
	 * @return an array of chunk tags
	 * @throws IOException if chunk model file is not found
	 */
	public static String[] chunker(String[] tokens, String[] tags) throws IOException {
		
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-chunker.bin.path", "data/models/en-chunker.bin"));
		ChunkerModel chunkerModel = new ChunkerModel(modelIn);
    	ChunkerME chunker = new ChunkerME(chunkerModel);
    	String chunkTags[] = chunker.chunk(tokens, tags);
    	//opennlp.tools.util.Span[] s = chunker.chunkAsSpans(tokens, tags);
    	return chunkTags;
	}
	
	/**
	 * Create a list of words for the given tokens and part-of-speech tags
	 * 
	 * @param tokens the given tokens
	 * @param tags the given part-of-speech tags
	 * @return a list of words
	 * @throws IOException if any opennlp model file is not found
	 */
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
	
	/**
	 * Compute chunk information for the given sentence object
	 * 
	 * @param s the sentence object to be calculated
	 * @param index the index for the sentence in the document
	 * @throws IOException if any opennlp model file is not found
	 */
	public static void chunking(ChunkedSentence s, int index) throws IOException {
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	String tokens[] = tokenizer.tokenize(s.getText());
    	opennlp.tools.util.Span[] tokenSpans = tokenizer.tokenizePos(s.getText());
    	String[] tags = pos(tokens);
    	
		List<Chunk> chunkList = new ArrayList<Chunk>();
		List<Word> wordList = new ArrayList<Word>();
		String[] lemmas = lemmatization(tokens, tags);
		String[] chunkTags = chunker(tokens, tags);
		
		Word w;
    	WordLexeme wl;
    	gov.nih.nlm.ling.core.Span span;
    	Chunk chunk = null;
    	List<Word> trail = null;
    	for (int i = 0; i < tokens.length; i++) {
    		String[] fields = chunkTags[i].split("-");
    		if(lemmas[i].equals("O")) {
    			wl = new WordLexeme(tokens[i], tags[i]);
    		}else {
    			wl = new WordLexeme(lemmas[i], tags[i]);
    		}
    		span = new gov.nih.nlm.ling.core.Span(tokenSpans[i].getStart() + s.getSpan().getBegin(), tokenSpans[i].getEnd() + s.getSpan().getBegin());
    		w = new Word(tokens[i], tags[i], wl, index, span);
    		w.setSentence(s);
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
	
	/**
	 * Create a list of sentence objects for the given string
	 * 
	 * @param text the given string to be processed
	 * @return a list of sentence objects that contains all related information
	 * @throws IOException if any opennlp model file is not found
	 */
	public static List<Sentence> sentenceSplit(String text) throws IOException{
		List<Sentence> sentList = new ArrayList<Sentence>();
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
    	SentenceModel model = new SentenceModel(modelIn);
    	SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
    	String sentences[] = sentenceDetector.sentDetect(text);
    	opennlp.tools.util.Span[] sentenceSpans = sentenceDetector.sentPosDetect(text);
    	ChunkedSentence s;
    	for (int i = 0; i < sentences.length; i++) {
    		s = new ChunkedSentence(Integer.toString(i), sentences[i], 
    					new gov.nih.nlm.ling.core.Span(sentenceSpans[i].getStart(), sentenceSpans[i].getEnd()));
    		chunking(s, i);
    		sentList.add(s);
    	}
    	return sentList;
	}
}
