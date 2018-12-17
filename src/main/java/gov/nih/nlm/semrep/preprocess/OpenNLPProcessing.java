package gov.nih.nlm.semrep.preprocess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Chunk;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.SurfaceElement;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;
import gov.nih.nlm.ling.process.SentenceSegmenter;
import gov.nih.nlm.semrep.core.SRSentence;
import gov.nih.nlm.semrep.core.TokenInfo;
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
 * @author Halil Kilicoglu
 *
 */
public class OpenNLPProcessing implements SentenceSegmenter, Tokenization, POSTagging, Lemmatization, Chunking {
	
//	private static Pattern SENTENCE_END = Pattern.compile("^(.+?)([A-Z])\\.");
//	private static Pattern NUM = Pattern.compile("[0-9]+");
	
	private POSTaggerME tagger;
	private Tokenizer tokenizer;
	private DictionaryLemmatizer lemmatizer;
	private ChunkerME chunker;
	private SentenceDetectorME sentenceDetector;

	/**
	 * Initializes openNLP models
	 * @param chunkingOnly	whether or not to load the chunking model only
	 * @throws IOException		if a openNLP model file is not found
	 */
	
	public OpenNLPProcessing(boolean chunkingOnly) throws IOException {
		InputStream modelIn = null;
		if (!chunkingOnly) {
			modelIn = new FileInputStream(
					System.getProperty("opennlp.en-sent.bin.path", "data/models/en-sent.bin"));
			SentenceModel model = new SentenceModel(modelIn);
			sentenceDetector = new SentenceDetectorME(model);
			
			modelIn = new FileInputStream(
					System.getProperty("opennlp.en-token.bin.path", "data/models/en-token.bin"));
			TokenizerModel tokenModel = new TokenizerModel(modelIn);
			tokenizer = new TokenizerME(tokenModel);
			
			modelIn = new FileInputStream(
					System.getProperty("opennlp.en-pos.bin.path", "data/models/en-pos-maxent.bin"));
			POSModel posModel = new POSModel(modelIn);
			tagger = new POSTaggerME(posModel);
			
			modelIn = new FileInputStream(
					System.getProperty("opennlp.en-lemmatizer.bin.path", "data/models/en-lemmatizer.bin"));
			lemmatizer = new DictionaryLemmatizer(modelIn);
			
		} 
		
		modelIn = new FileInputStream(
					System.getProperty("opennlp.en-chunker.bin.path", "data/models/en-chunker.bin"));
		ChunkerModel chunkerModel = new ChunkerModel(modelIn);
		chunker = new ChunkerME(chunkerModel);
	}
	
	@Override
    public void segment(String text, List<Sentence> sentences) {
    	String sents[] = sentenceDetector.sentDetect(text);
    	opennlp.tools.util.Span[] sentenceSpans = sentenceDetector.sentPosDetect(text);
    	SRSentence s;
    	for (int i = 0; i < sents.length; i++) {
    	    s = new SRSentence("S" + Integer.toString(i+1), sents[i],
    		    new gov.nih.nlm.ling.core.Span(sentenceSpans[i].getStart(), sentenceSpans[i].getEnd()));
    	    sentences.add(s);
    	    s.addCompleted(SRSentence.Processing.SSPLIT);
    	}
    }
    
	@Override
    public void tokenize(String sentence, List<TokenInfo> tokens) {
    	String[] toks = tokenizer.tokenize(sentence);
    	opennlp.tools.util.Span[] tokSpans0 = tokenizer.tokenizePos(sentence);
    	int[] begins = new int[tokSpans0.length];
    	int[] ends = new int[tokSpans0.length];
    	for (int i=0; i < tokSpans0.length; i++) {
    		begins[i] = tokSpans0[i].getStart();
    		ends[i] = tokSpans0[i].getEnd();
    	}
    	tokens.addAll(TokenInfo.convert(toks,begins,ends));
 //   	List<TokenInfo> newTsps = fixTokenization(tsps);
 //   	return newTsps;
    }
    
/*    private List<TokenInfo> fixTokenization(List<TokenInfo> inInfo) {
    	TokenInfo last = inInfo.get(inInfo.size()-1);
    	String tok = last.getToken();
    	int b = last.getBegin();
    	int e = last.getEnd();
    	Matcher m = SENTENCE_END.matcher(tok);
    	if (m.find()) {
    		List<TokenInfo> updated = inInfo.subList(0, inInfo.size()-1);
    		updated.add(new TokenInfo(m.group(1) + m.group(2),b,e-1));
    		updated.add(new TokenInfo(tok.substring(tok.length()-1),e-1,e));
    		return updated;
    	} 
    	return inInfo;
    }*/
    
	@Override
    public void tag(List<TokenInfo> tokenSpans) {
    	String[] tokens = TokenInfo.getTokensFromInfo(tokenSpans);
    	String tags[] = tagger.tag(tokens);
    	for (int i=0; i < tokenSpans.size(); i++) {
    		TokenInfo n = tokenSpans.get(i);
    		n.setPos(tags[i]);
    	}
//    	fixPOSTagging(tokenSpans,lexmatches);
    }
    
/*    private void fixPOSTagging(List<TokenInfo> inInfo, List<LexiconMatch> lexmatches) {
    	for (LexiconMatch match: lexmatches) {
    		List<TokenInfo> tokens = match.getMatch();
    		List<LexRecord> lexrecs = match.getLexRecords();
    		if (tokens.size() == 1) {
    			TokenInfo token = tokens.get(0);
        		String pos = token.getPos();
        		String tok = token.getToken();
        		// not great
        		if (pos.equals("CD") && NUM.matcher(tok).find() == false && lexrecs.size() > 0) {
        			token.setPos(LexiconWrapper.REVERSE_POS_TRANSLATION.get(lexrecs.get(0).GetCategory()).get(0));
        		}
    		}
    	}
    }*/
    
	@Override
    public void lemmatize(List<TokenInfo> tokenSpans) {
    	String[] tokens = TokenInfo.getTokensFromInfo(tokenSpans);
    	String[] tags = TokenInfo.getPOSTagsFromInfo(tokenSpans);
    	String[] lemmas = lemmatizer.lemmatize(tokens, tags);
    	for (int i=0; i < tokenSpans.size(); i++) {
    		TokenInfo n = tokenSpans.get(i);
    		n.setLemma(lemmas[i]);
    	}
//    	fixLemmatization(tokenSpans,lexmatches);
    }
    
/*    private void fixLemmatization(List<TokenInfo> inInfo, List<LexiconMatch> lexmatches) {
    	for (LexiconMatch match: lexmatches) {
    		List<TokenInfo> tokens = match.getMatch();
 //   		List<LexRecord> lexrecs = match.getLexRecords();
    		for (TokenInfo token: tokens) {
    			if (token.getLemma().equals("O")) { 
        			// too unpredictable and unreliable
    				if (lexrecs.size() > 0) {
    					String base = lexrecs.get(0).GetBase();
    					token.setLemma(base);
    				}  else {
    					token.setLemma(token.getToken().toLowerCase());
  //  				}
    			}
    		}
    	}
    }*/
   
  
	@Override
    public void chunk(SRSentence s, List<TokenInfo> tokens)  {
	List<Chunk> chunkList = new ArrayList<>();
	List<Word> wordList = new ArrayList<Word>();
	String[] toks = TokenInfo.getTokensFromInfo(tokens);
	String[] tags = TokenInfo.getPOSTagsFromInfo(tokens);
	String[] lemmas = TokenInfo.getLemmasFromInfo(tokens);
	String[] chunkTags = chunker.chunk(toks, tags);

	Word w;
	WordLexeme wl;
	gov.nih.nlm.ling.core.Span span;
	Chunk chunk = null;
	List<SurfaceElement> seList = null;
	
	for (int i = 0; i < tokens.size(); i++) {
		String tok = tokens.get(i).getToken();
	    String[] fields = chunkTags[i].split("-");
	    if (lemmas[i].equals("O")) {
		wl = new WordLexeme(tok, tags[i]);
	    } else {
		wl = new WordLexeme(lemmas[i], tags[i]);
	    }
	    span = new gov.nih.nlm.ling.core.Span(tokens.get(i).getBegin() + s.getSpan().getBegin(),
		    tokens.get(i).getEnd() + s.getSpan().getBegin());
	    w = new Word(tok, tags[i], wl, i, span);
	    w.setSentence(s);
	    w.setChunkRole('X');
	    wordList.add(w);
	    if (fields[0].equals("B")) {
			if (chunk == null) {	
			    chunk = new Chunk(null, fields[1]);
			    seList = new ArrayList<SurfaceElement>();
			    w.setChunk(chunk);
				seList.add(w);
			} else {
				if (chunk.getChunkType().equals("NP")) setChunkRolesForSurfaceElementList(seList);
				chunk.setSurfaceElementList(seList);
			    chunkList.add(chunk);
			    chunk = new Chunk(null, fields[1]);
			    seList = new ArrayList<SurfaceElement>();
			    w.setChunk(chunk);
			    seList.add(w);
			}
	    } else if (fields[0].equals("I")) {
	    	if (seList == null) seList = new ArrayList<SurfaceElement>();
	    	w.setChunk(chunk);
	    	seList.add(w);
	    } else if (fields[0].equals("O")) {
			if (chunk != null) {
				if (chunk.getChunkType().equals("NP")) setChunkRolesForSurfaceElementList(seList);
				chunk.setSurfaceElementList(seList);
			    chunkList.add(chunk);
			}
			chunk = new Chunk(null, tags[i]);
			seList = new ArrayList<SurfaceElement>();
			w.setChunk(chunk);
			seList.add(w);
			chunk.setSurfaceElementList(seList);
			chunkList.add(chunk);
			chunk = null;
	    }
	    if (i == tokens.size() - 1 && chunk != null) {
	    	if (chunk.getChunkType().equals("NP")) setChunkRolesForSurfaceElementList(seList);
	    	chunk.setSurfaceElementList(seList);
	    	chunkList.add(chunk);
	    }
	}
	s.setWords(wordList);
	s.setChunks(chunkList);
    }
    
    private void setChunkRolesForSurfaceElementList(List<SurfaceElement> seList) {
    	Word w;
    	if (seList.size() == 1) {
    		w = (Word)seList.get(0);
			w.setChunkRole('H');
    	}
    	int size = seList.size();
    	boolean headDetermined = false;

    	String tag;
    	for (int i = size - 1; i >= 0; i--) {
    		w = (Word) seList.get(i);
			tag = w.getPos();
    		if(!headDetermined) {
    			if(tag.startsWith("NN") || tag.startsWith("JJ") || tag.startsWith("VBG")) {
    				w.setChunkRole('H');
    				headDetermined = true;
    			}
    		}else {
    			if(!tag.startsWith("DT")) w.setChunkRole('M');
    		}
    	}
    }
}
