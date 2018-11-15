package gov.nih.nlm.semrep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.nlm.ling.core.Chunk;
import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.MultiWord;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.sem.AbstractRelation;
import gov.nih.nlm.ling.sem.AbstractTerm;
import gov.nih.nlm.ling.sem.Argument;
import gov.nih.nlm.ling.sem.Concept;
import gov.nih.nlm.ling.sem.Entity;
import gov.nih.nlm.ling.sem.ImplicitRelation;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.ling.sem.SemanticItemFactory;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.ner.AnnotationFilter;
import gov.nih.nlm.ner.LargestSpanFilter;
import gov.nih.nlm.ner.MultiThreadClient;
import gov.nih.nlm.ner.gnormplus.GNormPlusConcept;
import gov.nih.nlm.ner.metamap.ScoredUMLSConcept;
import gov.nih.nlm.semrep.core.ChunkedSentence;
import gov.nih.nlm.semrep.core.MedLineDocument;
import gov.nih.nlm.semrep.utils.MedLineParser;
import gov.nih.nlm.semrep.utils.OpennlpUtils;
import gov.nih.nlm.umls.HypernymProcessing;

/**
 * Main class for SemRep Java implementation
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
 *
 */

public class SemRep 
{
	private static Logger log = Logger.getLogger(SemRep.class.getName());	

//	private static MetaMapLiteClient metamap;
	private static MultiThreadClient nerAnnotator;
	private static OpennlpUtils nlpClient;
	private static HypernymProcessing hpClient;
	private static DocumentBuilder dBuilder;
	private static DocumentBuilderFactory factory;

	/**
	 * Create document object from string and analyze the document with respect to 
	 * lexical and syntactic information
	 * 
	 * @param documentID id for the document object
	 * @param text document text string
	 * @return document object with processed text and given id
	 * @throws IOException if sentence splitting model is not available
	 */
	public static Document lexicoSyntacticAnalysis(String documentID, String text) throws IOException {
		Document doc = new Document(documentID, text);
		if(nlpClient == null) nlpClient = new OpennlpUtils();
		List<Sentence> sentList= nlpClient.sentenceSplit(text);
		for(Sentence sent: sentList) {
			sent.setDocument(doc);
			((ChunkedSentence) sent).setSectionAbbreviation("tx");
			((ChunkedSentence) sent).setSentenceIDInSection(sent.getId());
		}
		doc.setSentences(sentList);
		return doc;
	}

	/**
	 * Set up user specified options for the program
	 * 
	 * @param args user specified properties
	 * @return properties object with given user specified options
	 * @throws FileNotFoundException if default property file is not found
	 * @throws IOException if given property file is not existed
	 */
	public static Properties getOptionProps(String[] args) throws FileNotFoundException, IOException {
		if (args.length < 2) {
			System.out.println("Usage: semrepjava --inputpath={in_path} --outputpath={out_path}.");
			System.exit(2);
		}
		Properties optionProps = new Properties();
		int i = 0;
		while( i < args.length) {
			if (args[i].substring(0, 2).equals("--")) {
				String[] fields = args[i].split("=");
				if(fields[0].equals("--configfile")) {
					String configFilename = fields[1];
					File f = new File(configFilename);
					if( f.exists() && !f.isDirectory())
						optionProps.load(new FileReader(new File(configFilename)));
					else {
						System.out.println("Cannot find specified configuration file. Please check file name.");
						System.exit(1);
					}
				}else if (fields[0].equals("--indexdir")) {
					optionProps.setProperty ("metamaplite.index.dir.name",fields[1]);
				} else if (fields[0].equals("--modelsdir")) {
					optionProps.setProperty ("opennlp.models.dir",fields[1]);
				} else if (fields[0].equals("--inputformat")) {
					optionProps.setProperty("user.inputformat", fields[1]);
				} else if (fields[0].equals("--outputformat")) {
					optionProps.setProperty("user.outputformat", fields[1]);
				} else if (fields[0].equals("--inputpath")) {
					optionProps.setProperty("user.inputpath", fields[1]);
				} else if (fields[0].equals("--outputpath")) {
					optionProps.setProperty("user.outputpath", fields[1]);
				} else if (fields[0].equals("--inputtextformat")) {
					optionProps.setProperty("user.inputtextformat", fields[1]);
				} else if (fields[0].equals("--annsource")) {
					optionProps.setProperty("user.annsource", fields[1]);
				} else if (fields[0].equals("--includes")) {
					optionProps.setProperty("user.output.includes", fields[1]);
				}
			}
			i++;
		}
		return optionProps;
	}

	/**
	 * Set up overall properties for the program (combine both default properties and user specified properties)
	 * 
	 * @param args user specified properties
	 * @return properties object with combined overall properties for the program
	 * @throws FileNotFoundException if default property file is not found
	 * @throws IOException if given property file does not exist
	 */
	public static Properties getProps(String[] args) throws FileNotFoundException, IOException {
		Properties  defaultProps = new Properties(System.getProperties());
		Properties configFileProps = FileUtils.loadPropertiesFromFile("semrepjava.properties");
		defaultProps.putAll(configFileProps);
		Properties optionProps = getOptionProps(args);
		defaultProps.putAll(optionProps);
		return defaultProps;
	}

	/**
	 * Print chunk contents for the given document object onto the output file
	 * 
	 * @param doc the document object to print
	 * @throws IOException if it fails to open and write to the output file
	 */
	public static void generateChunkOutput(Document doc) throws IOException {
		String outPath = System.getProperty("user.outputpath");
		String inputFormat = System.getProperty("user.inputformat");
		List<Sentence> sentList = doc.getSentences();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sentList.size(); i++) {
			ChunkedSentence cs = (ChunkedSentence)sentList.get(i);
			sb.append(cs.getText() + "\n");
			List<Chunk> chunkList = cs.getChunks();
			for(int j = 0; j < chunkList.size(); j++) {
				sb.append(chunkList.get(j).toString() + "\n");
			}
			sb.append("\n");
		}
		if(inputFormat.equalsIgnoreCase("dir")) {
			File dir = new File(outPath);
			if(!dir.isDirectory()) {
				dir.mkdirs();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath + "/" + doc.getId() + ".ann"));
			writer.write(sb.toString());
			writer.close();
		}else if (inputFormat.equalsIgnoreCase("singlefile")) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath + ".ann", true));
			writer.write(sb.toString());
			writer.close();
		}
	}

	/**
	 * Process from a directory of files if user specified input format is directory
	 * 
	 * @param inPath the path of the directory
	 * @throws IOException if it fails to open input files or to create and write to the output file
	 */
	public static void processFromDirectory(String inPath, String outPath) throws IOException {
		File[] files = new File(inPath).listFiles();	
		File dir = new File(outPath);
		
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
		for(File file: files) {
			String filename = file.getName();
			String[] fields = filename.split("\\.");
			processFromSingleFile(inPath + "/" + filename, outPath + "/" + fields[0]);
		}

	}

	/**
	 * Process from a single file if the user-specified input format is single file.
	 * The file can be in either plaintext or medline format..
	 * 
	 * @param inPath the path of the single file
	 * @throws IOException if it fails to open the input file or to create and write to the output file
	 */
	public static void processFromSingleFile(String inPath, String outPath) throws IOException {
		String inputTextFormat = System.getProperty("user.inputtextformat");
		BufferedReader br = new BufferedReader(new FileReader(inPath));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outPath, true));
		List<Document> processedDocuments = new ArrayList<Document>();
		
		if (inputTextFormat.equalsIgnoreCase("plaintext")) {		
			log.info("Processing plain text file : " + inPath);
			int count = 0;
			String line;
			StringBuilder sb = new StringBuilder();
			do {
				line = br.readLine();
				if( (line == null || line.trim().isEmpty()) && !sb.toString().trim().isEmpty()) {
					count++;
					Document doc = lexicoSyntacticAnalysis(Integer.toString(count),sb.toString());
					sb = new StringBuilder();
					processForSemantics(doc);
					processedDocuments.add(doc);
					//writeResults(doc, bw);
				}else {
					sb.append(line + " ");
				}
			} while(line != null);
		} else if (inputTextFormat.equalsIgnoreCase("medline")) {
			log.info("Processing Medline input file : " + inPath);
			List<MedLineDocument> mdList = MedLineParser.parseMultiMedLines(br, nlpClient);
			for (MedLineDocument md : mdList) {
				processForSemantics(md);
				processedDocuments.add(md);
				//writeResults(md, bw);
			}
		} else if (inputTextFormat.equalsIgnoreCase("medlinexml")) {
			log.info("Processing Medline input file : " + inPath);
			List<MedLineDocument> mdList = MedLineParser.parseMultiMedLinesXML(inPath, nlpClient, dBuilder);
			for (MedLineDocument md : mdList) {
				processForSemantics(md);
				processedDocuments.add(md);
				//writeResults(md, bw);
			}
		}
		writeResults(processedDocuments, bw);
		bw.close();
		br.close();
	}
	
	/**
	 * Process interactively on the command line. Expect a line of text to be entered.
	 * @throws IOException if it fails to open the input file or to create and write to the output file
	 */
	
	public static void processInteractively() throws IOException {
		Scanner in = new Scanner(System.in);
		BufferedWriter bw = new BufferedWriter(new PrintWriter(System.out));
		Document doc;
		List<Document> processedDocuments;
		
		System.out.println("Enter text below:");
		String input = in.nextLine();
		while(!input.equalsIgnoreCase("exit")) {
			doc = lexicoSyntacticAnalysis("0", input);
			processForSemantics(doc);
			processedDocuments = new ArrayList<Document>();
			processedDocuments.add(doc);
			writeResults(processedDocuments, bw);
			bw.flush();
			System.out.println("\nEnter text below:");
			input = in.nextLine();
		}
		bw.close();
		in.close();
	}
	

	/**
	 * Processes a <code>Document</code> object semantically, by identifying named entities 
	 * and extracting relations.  The document is expected to have gone through sentence splitting,
	 * lexical-syntactic analysis already.
	 * 
	 * @param doc the document to process
	 * @throws IOException if it fails to open the input file or to create and write to the output file
	 */
	public static void processForSemantics(Document doc) throws IOException {
		if (doc == null) return;
		if (doc.getSentences() == null || doc.getSentences().size() == 0)  {
			log.info("Document needs to be sentence-split for semantic processing.." + doc.getId());
			return;
		}
		
		// to generate chunk output to a file
		//generateChunkOutput(doc);
		
		// named entity recognition
		Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
		if(nerAnnotator == null) nerAnnotator = new MultiThreadClient(System.getProperties());
		nerAnnotator.annotate(doc,System.getProperties(),annotations);
		for (SpanList sp: annotations.keySet()) {
			LinkedHashSet<Ontology> terms = annotations.get(sp);
		    for (Ontology ont : terms) {
		    	Concept conc = (Concept) ont;
		    	log.info("Concept : " + sp.toString() + "\t" +  conc.toString());
			    }
		}
		
		// combine/filter named entity recognition results
		AnnotationFilter merger = new LargestSpanFilter();
		Map<SpanList, LinkedHashSet<Ontology>> mergedAnnotations = merger.filter(annotations);
		
		// create entities for named entity results
		List<SpanList> sps = new ArrayList<>(mergedAnnotations.keySet());
		Collections.sort(sps, new Comparator<SpanList>(){
			public int compare(SpanList a, SpanList b) {
				if (SpanList.atLeft(a, b)) return -1;
				else if (SpanList.atLeft(b, a)) return 1;
				else if (a.equals(b)) return -1;
					int as = a.getBegin();
					int bs = b.getBegin();
					return (as-bs);

			}
		});
		SemanticItemFactory sif = doc.getSemanticItemFactory();
		for (SpanList sp: sps) {
			LinkedHashSet<Ontology> terms = mergedAnnotations.get(sp);
			List<Word> wordList = doc.getWordsInSpan(sp);
			SpanList headSpan = MultiWord.findHeadFromCategory(wordList).getSpan();
			Concept sense = null;
			LinkedHashSet<Concept> concepts = new LinkedHashSet<>();
			Iterator<Ontology> iter = terms.iterator();
			while (iter.hasNext()) {		
				Concept conc = (Concept)iter.next();
				if (sense == null) sense = conc;
				concepts.add(conc);
			}
			sif.newEntity(doc, sp, headSpan, sense.getSemtypes().toString(),concepts,sense);
		}
		LinkedHashSet<SemanticItem> entities = Document.getSemanticItemsByClass(doc, Entity.class);
		for (SemanticItem sem : entities) {
			Entity ent = (Entity)sem;
			log.info("Entity:" + ent.toShortString());
		}
		
		List<Argument> args;
		for (Sentence cs: doc.getSentences()) {
			for(Chunk chunk: ((ChunkedSentence)cs).getChunks()) {
				if(hpClient == null) hpClient = new HypernymProcessing();
				args = hpClient.intraNP(chunk);
				if(args != null) sif.newImplicitRelation(doc, "ISA", args);
			}
		}
	
		// these entities now can now be written to output.
		log.info("Semantic process done.");
	}
	
	/**
	 * This function outputs documents' infos to a output stream according to the specified options
	 * 
	 * @param docs the list of documents to be output
	 * 
	 * @throws IOException
	 */

	public static void writeResults(List<Document> docs, BufferedWriter writer) throws IOException {
		String outputFormat = System.getProperty("user.outputformat");
		StringBuilder sb = new StringBuilder();
		List<Sentence> sentList;
		ChunkedSentence cs;
		LinkedHashSet<SemanticItem> siList;
		Entity ent;
		String includes = System.getProperty("user.output.includes");
		Document doc;
		
		if(outputFormat.equalsIgnoreCase("simplified")) {
			for(int i = 0; i < docs.size(); i++) {
				doc = docs.get(i);
				sentList = doc.getSentences();
				for(int j = 0; j < sentList.size(); j++) {
					cs = (ChunkedSentence)sentList.get(j);
					sb.append(cs.getText() + "\n");
					siList = Document.getSemanticItemsByClassSpan(doc, Entity.class, new SpanList(cs.getSpan()), true);
					for(SemanticItem si : siList) {
						ent = (Entity) si;
						sb.append(ent.toShortString() + "\n");
					}
					siList = Document.getSemanticItemsByClassSpan(doc, AbstractRelation.class, new SpanList(cs.getSpan()), true);
					for(SemanticItem si : siList) {
						if(si instanceof ImplicitRelation) sb.append(((ImplicitRelation)si).toShortString() + "\n");
					}
					if( includes != null)
						sb.append(cs.getIncludeInfo(includes));
					sb.append("\n");
				}
			}
		} else if(outputFormat.equalsIgnoreCase("brat")) {
			for(int i = 0; i < docs.size(); i++) {
				doc = docs.get(i);
				siList = Document.getSemanticItemsByClass(doc, Entity.class);
				for (SemanticItem si : siList) {
					ent = (Entity)si;
					sb.append(ent.toStandoffAnnotation(true, 0) + "\n");
				}
				sb.append("\n");
			}		
		} else if(outputFormat.equalsIgnoreCase("human-readable")) {
			for(int i = 0; i < docs.size(); i++) {
				doc = docs.get(i);
				sentList = doc.getSentences();
				List<String> commonOutputFields;
				List<String> textOutputFields;
				List<String> entityOutputFields;
				LinkedHashSet<SemanticItem> entities;
				
				for(int j = 0; j < sentList.size(); j++) {
					cs = (ChunkedSentence)sentList.get(j);
					commonOutputFields = new ArrayList<String>();
					textOutputFields = new ArrayList<String>();
						
					commonOutputFields.add(doc.getId());
					commonOutputFields.add(cs.getSubsection());
					commonOutputFields.add(cs.getSectionAbbreviation());
					commonOutputFields.add(cs.getSentenceIDInSection());
					
					textOutputFields.addAll(commonOutputFields);
					textOutputFields.add("text");
					textOutputFields.addAll(cs.getRemainFieldsForTextOutput());
					sb.append(String.join("|", textOutputFields) + "\n");
					
					entities = Document.getSemanticItemsByClassSpan(doc, Entity.class, new SpanList(cs.getSpan()), true);
					for(SemanticItem entity : entities) {
						for(Concept concept: ((Entity)entity).getConcepts()) {
							entityOutputFields = new ArrayList<String>();
							entityOutputFields.addAll(commonOutputFields);
							entityOutputFields.add("entity");
							entityOutputFields.add(concept.getId());
							entityOutputFields.add(concept.getName());
							entityOutputFields.add(String.join(",", concept.getSemtypes()));
							entityOutputFields.add(((AbstractTerm)entity).getText());
							if(concept instanceof GNormPlusConcept)
								entityOutputFields.add("1000");
							else if(concept instanceof ScoredUMLSConcept)
								entityOutputFields.add(Double.toString(((ScoredUMLSConcept)concept).getScore()));
							entityOutputFields.add(Integer.toString(entity.getSpan().getBegin()));
							entityOutputFields.add(Integer.toString(entity.getSpan().getEnd()));
							sb.append(String.join("|", entityOutputFields) + "\n");
						}			
					}
					siList = Document.getSemanticItemsByClassSpan(doc, AbstractRelation.class, new SpanList(cs.getSpan()), true);
					for(SemanticItem si : siList) {
						if(si instanceof ImplicitRelation) sb.append(((ImplicitRelation)si).toShortString() + "\n");
					}
					if( includes != null)
						sb.append(cs.getIncludeInfo(includes));
					sb.append("\n");
				}
			}
		} else if(outputFormat.equalsIgnoreCase("json")) {
			JSONArray documentsJsonArray = new JSONArray();
			JSONArray sentencesJsonArray, entitiesJsonArray;
			JSONObject docJson,sentJson,entJson;
			LinkedHashSet<SemanticItem> entities;
			
			for(int i = 0; i < docs.size(); i++) {
				doc = docs.get(i);
				docJson = new JSONObject();
				docJson.put("id", doc.getId());
				docJson.put("text", doc.getText());
				sentList = doc.getSentences();
				sentencesJsonArray = new JSONArray();
				for(int j = 0; j < sentList.size(); j++) {
					cs = (ChunkedSentence)sentList.get(j);
					sentJson = new JSONObject();
					sentJson.put("id", cs.getId());
					sentJson.put("subsection", cs.getSubsection());
					sentJson.put("section_abbreviation", cs.getSectionAbbreviation());
					sentJson.put("sentence_id", cs.getSentenceIDInSection());
					sentJson.put("text", cs.getText());
					sentJson.put("begin", cs.getSpan().getBegin());
					sentJson.put("end", cs.getSpan().getEnd());
					entitiesJsonArray = new JSONArray();
					entities = Document.getSemanticItemsBySpan(doc, new SpanList(cs.getSpan()), true);
					for(SemanticItem entity : entities) {
						for(Concept concept: ((Entity)entity).getConcepts()) {
							entJson = new JSONObject();
							entJson.put("cui", concept.getId());
							entJson.put("name", concept.getName());
							entJson.put("semtypes", String.join(",", concept.getSemtypes()));
							entJson.put("text", ((AbstractTerm)entity).getText());
							if(concept instanceof GNormPlusConcept)
								entJson.put("score", "1000");
							else if(concept instanceof ScoredUMLSConcept)
								entJson.put("score", ((ScoredUMLSConcept)concept).getScore());
							entJson.put("begin", entity.getSpan().getBegin());
							entJson.put("end", entity.getSpan().getEnd());
							entitiesJsonArray.put(entJson);
						}			
					}
					sentJson.put("entities", entitiesJsonArray);
					sentencesJsonArray.put(sentJson);
				}
				docJson.put("sentences", sentencesJsonArray);
				documentsJsonArray.put(docJson);
			}
			sb.append(documentsJsonArray.toString());
		}
		writer.write(sb.toString());	
	}
	

   /**
    * Initializes logging from a file configuration (logging.properties).
    * 
    */
	public static void initLogging() {
		try {
			InputStream config = SemRep.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(config);
			config.close();
		} catch (IOException ex) {
			log.warning("Could not open logging configuration file. Logging not configured (console output only).");
		}
	}

	/**
	 * Initializes logging and named entity recognizers.
	 * @throws IOException 
	 * 				if any opennlp model file is not found
	 * 
	 */
	public static void init() throws IOException {
		initLogging();
		nerAnnotator = new MultiThreadClient(System.getProperties());
		nlpClient = new OpennlpUtils();
//		metamap = new MetaMapLiteClient(System.getProperties());
		hpClient = new HypernymProcessing();
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		try {
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.out.println("Unable to initialize document builder in init()...");
		}
	}


	public static void main( String[] args ) throws IOException
	{
		long beg = System.currentTimeMillis();
		System.setProperties(getProps(args));
		init();

		String inputFormat = System.getProperty("user.inputformat");
		String inPath = System.getProperty("user.inputpath");
		String outPath = System.getProperty("user.outputpath");

		log.info("Starting SemRep...");
		if(inputFormat.equalsIgnoreCase("dir")) {
			processFromDirectory(inPath, outPath);	
		}else if(inputFormat.equalsIgnoreCase("singlefile")) {
			processFromSingleFile(inPath, outPath);
		}else if(inputFormat.equalsIgnoreCase("interactive")) {
			processInteractively();
		}
		long end = System.currentTimeMillis();
		log.info("Completed all " +(end-beg) + " msec.");
	}
}
