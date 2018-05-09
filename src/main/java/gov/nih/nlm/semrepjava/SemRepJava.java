package gov.nih.nlm.semrepjava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;
import gov.nih.nlm.nls.metamap.document.FreeText;
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

public class SemRepJava 
{
	
	public static String[] pos(String[] tokens) throws IOException{
    	InputStream modelIn = new FileInputStream(System.getProperty("opennlp.models.dir", "data/models") + "/" + 
				System.getProperty("opennlp.en-pos.bin.path", "en-pos-maxent.bin"));
    	POSModel posModel = new POSModel(modelIn);
    	POSTaggerME tagger = new POSTaggerME(posModel);
    	String tags[] = tagger.tag(tokens);
    	return tags;
	}
	
	
	public static List<Word> tokenization(String sentence) throws IOException{
    	List<Word> wordList = new ArrayList<Word>();
    	InputStream modelIn = new FileInputStream(System.getProperty("opennlp.models.dir", "data/models") + "/" + 
				System.getProperty("opennlp.en-token.bin.path", "en-token.bin"));
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	String tokens[] = tokenizer.tokenize(sentence);
    	String tags[] = pos(tokens);
    	//Span tokenSpans[] = tokenizer.tokenizePos(sentence);
    	
    	modelIn = new FileInputStream(System.getProperty("opennlp.models.dir", "data/models") + "/" + 
				System.getProperty("opennlp.en-lemmatizer.bin.path", "en-lemmatizer.txt"));
    	DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(modelIn);
    	String[] lemmas = lemmatizer.lemmatize(tokens, tags);
    	
    	
    	modelIn = new FileInputStream(System.getProperty("opennlp.models.dir", "data/models") + "/" + 
				System.getProperty("opennlp.en-chunker.bin.path", "en-chunker.bin"));
    	ChunkerModel chunkerModel = new ChunkerModel(modelIn);
    	ChunkerME chunker = new ChunkerME(chunkerModel);
    	String chunkerTags[] = chunker.chunk(tokens, tags);
    	
    	Word w;
    	WordLexeme wl;
    	for (int i = 0; i < tokens.length; i++) {
    		System.out.println(tokens[i] + "\t" + tags[i]);
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
		InputStream modelIn = new FileInputStream(System.getProperty("opennlp.models.dir", "data/models") + "/" + 
				System.getProperty("opennlp.en-sent.bin.path", "en-sent.bin"));
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
	
	
	public static Document processingFromText(String documentID, String text) throws IOException {
		Document doc = new Document(documentID, text);
		List<Sentence> sentList= sentenceSplit(text);
		doc.setSentences(sentList);
		return doc;
	}
	
	public static Properties setOptionProps(String[] args) throws FileNotFoundException, IOException {
		Properties optionProps = new Properties();
		String configFilename;
		int i = 0;
		while( i < args.length) {
			if (args[i].substring(0, 2).equals("--")) {
				String[] fields = args[i].split("=");
				if(fields[0].equals("--configfile")) {
					configFilename = fields[1];
					File f = new File(configFilename);
					if( f.exists() && !f.isDirectory())
						optionProps.load(new FileReader(new File(configFilename)));
					else {
						System.out.println("Cannot find specified configuration file. Please check file name.");
						System.exit(1);
					}
				}else if (fields[0].equals("--indexdir")) {
				      optionProps.setProperty ("index.dir.name",fields[1]);
			    } else if (fields[0].equals("--modelsdir")) {
			      optionProps.setProperty ("opennlp.models.dir",fields[1]);
			    }
			}
			i++;
		}
		return optionProps;
	}
	
	public static Properties setDefaultProps() throws FileNotFoundException, IOException {
		Properties  props = new Properties(System.getProperties());
		String configFilename = "semrepjava.properties";
		File configFile = new File(configFilename);
		if( configFile.exists() && !configFile.isDirectory()) {
			 props.load(new FileReader(configFile));
		}
		return props;
	}
	
	public static void printChunkedDocument(Document doc) {
		List<Sentence> sentList = doc.getSentences();
		Sentence s;
		List<Word> wordList;
		String chunkerTag;
		ChunkedWord cw;
		String[] fields;
		StringBuilder sb;
		boolean newChunk = true;
		for(int i = 0; i < sentList.size(); i++) {
			s = sentList.get(i);
			wordList = s.getWords();
			System.out.println(s.getText());
			sb = new StringBuilder();
			for(int j = 0; j < wordList.size(); j++) {
				cw = (ChunkedWord) wordList.get(j);
				chunkerTag = cw.getChunkerTag();
				fields = chunkerTag.split("-");
				if(fields[0].equals("B")) {
					if (newChunk) {
						sb.append("[ " + cw.getText() + " (" + cw.getPos() + ", " + cw.getLemma() + ") ");
						newChunk = false;
					}else {
						sb.append("]");
						sb.append("\n");
						sb.append("[ " + cw.getText() + " (" + cw.getPos() + ", " + cw.getLemma() + ") ");
					}
				}else if(fields[0].equals("I")) {
					sb.append(cw.getText() + " (" + cw.getPos() + ", " + cw.getLemma() + ") ");
				}
			}
			sb.append("]");
			newChunk = true;
			System.out.println(sb);
			System.out.println();
		}
	}
	
    public static void main( String[] args ) throws Exception
    {
    	Properties  defaultProps = setDefaultProps();
    	if(args.length > 1) {
    		Properties optionProps = setOptionProps(args);
    		defaultProps.putAll(optionProps);
    	}
    	System.setProperties(defaultProps);
    	
    	//String filename = args[1];
    	//for test
    	String filename = "document.txt";
    	
    	String inputText = FreeText.loadFile(filename);
    	
    	Document doc = processingFromText(filename, inputText);
    	
    	printChunkedDocument(doc);

    }
}
