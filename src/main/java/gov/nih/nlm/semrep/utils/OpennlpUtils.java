package gov.nih.nlm.semrep.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Chunk;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.SurfaceElement;
import gov.nih.nlm.ling.core.SurfaceElementFactory;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;
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
	
	private POSTaggerME tagger;
	private Tokenizer tokenizer;
	private DictionaryLemmatizer lemmatizer;
	private ChunkerME chunker;
	private SentenceDetectorME sentenceDetector;
	
	/**
	 * Initialize opennlp models
	 * @throws IOException 
	 * 				if any opennlp model file is not found
	 */
	
	public OpennlpUtils() throws IOException{
		InputStream modelIn = new FileInputStream(
				System.getProperty("opennlp.en-pos.bin.path", "data/models/en-pos-maxent.bin"));
		POSModel posModel = new POSModel(modelIn);
		tagger = new POSTaggerME(posModel);
		modelIn = new FileInputStream(
				System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
		TokenizerModel tokenModel = new TokenizerModel(modelIn);
		tokenizer = new TokenizerME(tokenModel);
		modelIn = new FileInputStream(
				System.getProperty("opennlp.en-lemmatizer.bin.path", "data/models/en-lemmatizer.bin"));
		lemmatizer = new DictionaryLemmatizer(modelIn);
		modelIn = new FileInputStream(
				System.getProperty("opennlp.en-chunker.bin.path", "data/models/en-chunker.bin"));
		ChunkerModel chunkerModel = new ChunkerModel(modelIn);
		chunker = new ChunkerME(chunkerModel);
		modelIn = new FileInputStream(
				System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
		SentenceModel model = new SentenceModel(modelIn);
		sentenceDetector = new SentenceDetectorME(model);
}
	

    /**
     * Compute part-of-speech tags for the given tokens
     * 
     * @param tokens
     *            token information
     * @return an array of part-of-speech tags
     * @throws IOException
     *             if part-of-speech model file is not found
     */
    public String[] pos(String[] tokens) throws IOException {
	String tags[] = tagger.tag(tokens);
	return tags;
    }

    /**
     * Compute token information for the given string
     * 
     * @param sentence
     *            the string to be processed
     * @return an array of token strings
     * @throws IOException
     *             if token model file is not found
     */

    public String[] tokenization(String sentence) throws IOException {
	String tokens[] = tokenizer.tokenize(sentence);
	return tokens;
    }

    /**
     * Compute token spans for the given string
     * 
     * @param sentence
     *            the string to be processed
     * @return an array of spans
     * @throws IOException
     *             if token model file is not found
     */
    public opennlp.tools.util.Span[] getTokenSpans(String sentence) throws IOException {
	return tokenizer.tokenizePos(sentence);
    }

    /**
     * Compute lemmas for the given tokens and part-of-speech tags
     * 
     * @param tokens
     *            the given tokens
     * @param tags
     *            the given part-of-speech tags
     * @return an array of lemma strings
     * @throws IOException
     *             if lemmatizer model file is not found
     */
    public String[] lemmatization(String[] tokens, String[] tags) throws IOException {
	String[] lemmas = lemmatizer.lemmatize(tokens, tags);
	return lemmas;
    }

    /**
     * Compute chunk tags for the given tokens and part-of-speech tags
     * 
     * @param tokens
     *            the given tokens
     * @param tags
     *            the given part-of-speech tags
     * @return an array of chunk tags
     * @throws IOException
     *             if chunk model file is not found
     */
    public String[] chunker(String[] tokens, String[] tags) throws IOException {

	String chunkTags[] = chunker.chunk(tokens, tags);
	//opennlp.tools.util.Span[] s = chunker.chunkAsSpans(tokens, tags);
	return chunkTags;
    }

    /**
     * Create a list of words for the given tokens and part-of-speech tags
     * 
     * @param tokens
     *            the given tokens
     * @param tags
     *            the given part-of-speech tags
     * @return a list of words
     * @throws IOException
     *             if any opennlp model file is not found
     */
    public List<Word> wording(String[] tokens, String[] tags) throws IOException {

	List<Word> wordList = new ArrayList<Word>();
	String[] lemmas = lemmatization(tokens, tags);

	Word w;
	WordLexeme wl;
	for (int i = 0; i < tokens.length; i++) {
	    if (lemmas[i].equals("O")) {
		wl = new WordLexeme(tokens[i], tags[i]);
	    } else {
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
     * @param s
     *            the sentence object to be calculated
     * @param index
     *            the index for the sentence in the document
     * @throws IOException
     *             if any opennlp model file is not found
     */
    public void chunking(ChunkedSentence s, int index) throws IOException {
	String tokens[] = tokenizer.tokenize(s.getText());
	opennlp.tools.util.Span[] tokenSpans = tokenizer.tokenizePos(s.getText());
	String[] tags = pos(tokens);

	List<Chunk> chunkList = new ArrayList<>();
	List<Word> wordList = new ArrayList<Word>();
	String[] lemmas = lemmatization(tokens, tags);
	String[] chunkTags = chunker(tokens, tags);

	Word w;
	WordLexeme wl;
	gov.nih.nlm.ling.core.Span span;
	Chunk chunk = null;
	List<SurfaceElement> seList;
	SurfaceElementFactory sef = new SurfaceElementFactory();
	
	for (int i = 0; i < tokens.length; i++) {
	    String[] fields = chunkTags[i].split("-");
	    if (lemmas[i].equals("O")) {
		wl = new WordLexeme(tokens[i], tags[i]);
	    } else {
		wl = new WordLexeme(lemmas[i], tags[i]);
	    }
	    span = new gov.nih.nlm.ling.core.Span(tokenSpans[i].getStart() + s.getSpan().getBegin(),
		    tokenSpans[i].getEnd() + s.getSpan().getBegin());
	    w = new Word(tokens[i], tags[i], wl, index, span);
	    w.setSentence(s);
	    wordList.add(w);
	    if (fields[0].equals("B")) {
			if (chunk == null) {
				seList = new ArrayList<SurfaceElement>();
			    chunk = new Chunk(seList, fields[1]);
			} else {
			    chunkList.add(chunk);
			    seList = new ArrayList<SurfaceElement>();
			    chunk = new Chunk(seList, fields[1]);
			}
	    } else if (fields[0].equals("I")) {
			
	    } else if (fields[0].equals("O")) {
			if (chunk != null)
			    chunkList.add(chunk);
			seList = new ArrayList<SurfaceElement>();
			seList.add(w);
			chunk = new Chunk(seList, tags[i]);
			w.setChunk(chunk);
			chunkList.add(chunk);
			chunk = null;
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
     * @param text
     *            the given string to be processed
     * @return a list of sentence objects that contains all related information
     * @throws IOException
     *             if any opennlp model file is not found
     */
    public List<Sentence> sentenceSplit(String text) throws IOException {
	List<Sentence> sentList = new ArrayList<Sentence>();
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
