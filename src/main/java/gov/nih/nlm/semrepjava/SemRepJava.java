package gov.nih.nlm.semrepjava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

public class SemRepJava 
{
    public static void main( String[] args ) throws Exception
    {
    	/*Properties myProperties = new Properties();
    	MetaMapLite.expandModelsDir(myProperties, "../public_mm_lite/data/models");
    	MetaMapLite.expandIndexDir(myProperties, "../public_mm_lite/data/ivf/2017AA/Base/strict");
    	myProperties.setProperty("metamaplite.excluded.termsfile", "../public_mm_lite/data/specialterms.txt");
    	MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);
    	List<BioCDocument> documentList = FreeText.loadFreeTextFile("test document.txt");
    	List<Entity> entityList = metaMapLiteInst.processDocumentList(documentList);
    	for (Entity entity: entityList) {
    	  for (Ev ev: entity.getEvSet()) {
    	 	System.out.print(ev.getConceptInfo().getCUI() + "|" + entity.getMatchedText());
    	    System.out.println();
    	  }
    	}*/
    	
    	String inputText = FreeText.loadFile("test document.txt");
    	InputStream modelIn = new FileInputStream("opennlp pretrained models/en-sent.bin");
    	SentenceModel model = new SentenceModel(modelIn);
    	SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
    	String sentences[] = sentenceDetector.sentDetect(inputText);
    	Span sentencesSpan[] = sentenceDetector.sentPosDetect(inputText);
    	System.out.print(sentences.length);
    	System.out.println();
    	System.out.print(sentences[4]);
    	System.out.println();
    	
    	modelIn = new FileInputStream("opennlp pretrained models/en-token.bin");
    	TokenizerModel tokenModel = new TokenizerModel(modelIn);
    	Tokenizer tokenizer = new TokenizerME(tokenModel);
    	String tokens[] = tokenizer.tokenize(inputText);
    	Span tokenSpans[] = tokenizer.tokenizePos(inputText);
    	System.out.print(tokens[0]);
    	System.out.println();
    	System.out.print(tokens.length);
    	System.out.println();
    	
    	modelIn = new FileInputStream("opennlp pretrained models/en-pos-maxent.bin");
    	POSModel posModel = new POSModel(modelIn);
    	POSTaggerME tagger = new POSTaggerME(posModel);
    	String tags[] = tagger.tag(tokens);
    	double probs[] = tagger.probs();
    	Sequence topSequences[] = tagger.topKSequences(tokens);
    	System.out.print(tags[0]);
    	System.out.println();
    	System.out.print(tags.length);
    	System.out.println();
    	
    	modelIn = new FileInputStream("opennlp pretrained models/en-chunker.bin");
    	ChunkerModel chunkerModel = new ChunkerModel(modelIn);
    	ChunkerME chunker = new ChunkerME(chunkerModel);
    	String chunkerTags[] = chunker.chunk(tokens, tags);
    	double chunkerProbs[] = chunker.probs();
    	System.out.print(chunkerTags[4]);
    	System.out.println();
    	System.out.print(chunkerTags.length);
    	System.out.println();
    }
}
