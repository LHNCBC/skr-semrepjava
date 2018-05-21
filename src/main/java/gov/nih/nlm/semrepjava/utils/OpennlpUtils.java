package gov.nih.nlm.semrepjava.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;
import gov.nih.nlm.semrepjava.core.ChunkedWord;
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
	
	
	public static List<Word> tokenization(String sentence) throws IOException{
    	List<Word> wordList = new ArrayList<Word>();
    	InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	String tokens[] = tokenizer.tokenize(sentence);
    	String tags[] = pos(tokens);
    	//Span tokenSpans[] = tokenizer.tokenizePos(sentence);
    	
    	modelIn = new FileInputStream(System.getProperty("opennlp.en-lemmatizer.bin.path", "data/models/en-lemmatizer.txt"));
    	DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(modelIn);
    	String[] lemmas = lemmatizer.lemmatize(tokens, tags);
    	
    	
    	modelIn = new FileInputStream(System.getProperty("opennlp.en-chunker.bin.path", "data/models/en-chunker.bin"));
    	ChunkerModel chunkerModel = new ChunkerModel(modelIn);
    	ChunkerME chunker = new ChunkerME(chunkerModel);
    	String chunkerTags[] = chunker.chunk(tokens, tags);
    	
    	Word w;
    	WordLexeme wl;
    	for (int i = 0; i < tokens.length; i++) {
    		if(lemmas[i].equals("O")) {
    			wl = new WordLexeme(tokens[i], tags[i]);
    		}else {
    			wl = new WordLexeme(lemmas[i], tags[i]);
    		}
    		w = new ChunkedWord(tokens[i], tags[i], wl, chunkerTags[i]);
    		wordList.add(w);
    	}
    	return wordList;
	}
	
	public static List<Sentence> sentenceSplit(String text) throws IOException{
		List<Sentence> sentList = new ArrayList<Sentence>();
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
    	SentenceModel model = new SentenceModel(modelIn);
    	SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
    	String sentences[] = sentenceDetector.sentDetect(text);
    	opennlp.tools.util.Span sentenceSpans[] = sentenceDetector.sentPosDetect(text);
    	Sentence s;
    	for (int i = 0; i < sentences.length; i++) {
    		s = new Sentence(Integer.toString(i), sentences[i], 
    					new gov.nih.nlm.ling.core.Span(sentenceSpans[i].getStart(), sentenceSpans[i].getEnd()));
    		s.setWords(tokenization(sentences[i].toLowerCase()));
    		sentList.add(s);
    	}
    	return sentList;
	}
}
