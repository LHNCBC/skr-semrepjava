package gov.nih.nlm.semrep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.MultiWord;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.sem.Concept;
import gov.nih.nlm.ling.sem.Entity;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.ling.sem.SemanticItemFactory;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.ner.AnnotationFilter;
import gov.nih.nlm.ner.LargestSpanFilter;
import gov.nih.nlm.ner.MultiThreadClient;
import gov.nih.nlm.semrep.core.Chunk;
import gov.nih.nlm.semrep.core.ChunkedSentence;
import gov.nih.nlm.semrep.core.MedLineDocument;
import gov.nih.nlm.semrep.utils.MedLineParser;
import gov.nih.nlm.semrep.utils.OpennlpUtils;

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
		List<Sentence> sentList= nlpClient.sentenceSplit(text);
		for(Sentence sent: sentList) {
			sent.setDocument(doc);
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
	public static void processFromDirectory(String inPath) throws IOException {
		File[] files = new File(inPath).listFiles();
		String outPath = System.getProperty("user.outputpath");
		String inputTextFormat = System.getProperty("user.inputtextformat");
		File dir = new File(outPath);
		BufferedWriter bw;
		BufferedReader br;
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
		for(File file: files) {
			String filename = file.getName();
			String[] fields = filename.split("\\.");
			if(fields.length == 2 && fields[1].equals("txt")) {
				 br = new BufferedReader(new FileReader(file));
				if (inputTextFormat.equalsIgnoreCase("plaintext")) {
					long fileLen = file.length();
					char[] buf = new char[(int)fileLen];
					br.read(buf,0, (int)fileLen);
					br.close();
					String text = new String(buf);
					Document doc = lexicoSyntacticAnalysis(fields[0], text);
					processForSemantics(doc);
					bw = new BufferedWriter(new FileWriter(outPath + "/" + doc.getId(), true));
					writeResults(doc, bw);
					bw.close();
				}else if (inputTextFormat.equalsIgnoreCase("medline")) {
					MedLineDocument md = MedLineParser.parseSingleMedLine(br, nlpClient);
					processForSemantics(md);
					bw = new BufferedWriter(new FileWriter(outPath + "/" + md.getId(), true));
					writeResults(md, bw);
					bw.close();
				}
				br.close();
			}
		}

	}

	/**
	 * Process from a single file if the user-specified input format is single file.
	 * The file can be in either plaintext or medline format..
	 * 
	 * @param inPath the path of the single file
	 * @throws IOException if it fails to open the input file or to create and write to the output file
	 */
	public static void processFromSingleFile(String inPath) throws IOException {
		String inputTextFormat = System.getProperty("user.inputtextformat");
		String outPath = System.getProperty("user.outputpath");
		BufferedReader br = new BufferedReader(new FileReader(inPath));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outPath, true));
		
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
					writeResults(doc, bw);
				}else {
					sb.append(line + " ");
				}
			} while(line != null);
		} else if (inputTextFormat.equalsIgnoreCase("medline")) {
			log.info("Processing Medline input file : " + inPath);
			List<MedLineDocument> mdList = MedLineParser.parseMultiMedLines(br, nlpClient);
			for (MedLineDocument md : mdList) {
				processForSemantics(md);
				writeResults(md, bw);
			}
		}
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
		
		System.out.println("Enter text below:");
		String input = in.nextLine();
		while(!input.equalsIgnoreCase("exit")) {
			doc = lexicoSyntacticAnalysis("0", input);
			processForSemantics(doc);
			writeResults(doc, bw);
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
	
		// these entities now can now be written to output.
		log.info("Semantic process done.");
	}
	
	/**
	 * This function outputs document infos to a file according to the specified options
	 * 
	 * @param doc the document that contains infos
	 * 
	 * @throws IOException
	 */

	public static void writeResults(Document doc, BufferedWriter writer) throws IOException {
		String outputFormat = System.getProperty("user.outputformat");
		StringBuilder sb = new StringBuilder();
		List<Sentence> sentList = doc.getSentences();
		ChunkedSentence cs;
		LinkedHashSet<SemanticItem> siList;
		Entity ent;
		String includes;
		
		if(outputFormat.equalsIgnoreCase("human-readable")) {
			for(int i = 0; i < sentList.size(); i++) {
				cs = (ChunkedSentence)sentList.get(i);
				sb.append(cs.getText() + "\n");
				siList = Document.getSemanticItemsBySpan(doc, new SpanList(cs.getSpan()), true);
				for(SemanticItem si : siList) {
					ent = (Entity) si;
					sb.append(ent.toShortString() + "\n");
				}
				includes = System.getProperty("user.output.includes");
				if(includes != null) {
					if(includes.equalsIgnoreCase("chunk")) {
						List<Chunk> chunkList = cs.getChunks();
						for(int j = 0; j < chunkList.size(); j++) {
							sb.append(chunkList.get(j).toString() + "\n");
						}
					} else if(includes.equalsIgnoreCase("tag")) {
						List<String> tagList = cs.getTags();
						sb.append(String.join(" ", tagList) + "\n");
					}
				}
				sb.append("\n");
			}
		} else if(outputFormat.equalsIgnoreCase("brat")) {
			// TODO : brat output format
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
	}



	public static void main( String[] args ) throws IOException
	{
		long beg = System.currentTimeMillis();
		System.setProperties(getProps(args));
		init();

		String inputFormat = System.getProperty("user.inputformat");
		String inPath = System.getProperty("user.inputpath");

		log.info("Starting SemRep...");
		if(inputFormat.equalsIgnoreCase("dir")) {
			processFromDirectory(inPath);	
		}else if(inputFormat.equalsIgnoreCase("singlefile")) {
			processFromSingleFile(inPath);
		}else if(inputFormat.equalsIgnoreCase("interactive")) {
			processInteractively();
		}
		long end = System.currentTimeMillis();
		log.info("Completed all " +(end-beg) + " msec.");
	}
}
