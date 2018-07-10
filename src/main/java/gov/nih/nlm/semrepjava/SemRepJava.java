package gov.nih.nlm.semrepjava;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ner.metamap.MetaMapLiteClient;
import gov.nih.nlm.ner.wsd.WSDClient;
import gov.nih.nlm.semrepjava.core.Chunk;
import gov.nih.nlm.semrepjava.core.ChunkedSentence;
import gov.nih.nlm.semrepjava.core.MedLineDocument;
import gov.nih.nlm.semrepjava.utils.MedLineParser;
import gov.nih.nlm.semrepjava.utils.OpennlpUtils;

public class SemRepJava 
{
	public static Document processingFromText(String documentID, String text) throws IOException {
		Document doc = new Document(documentID, text);
		List<Sentence> sentList= OpennlpUtils.sentenceSplit(text);
		for(Sentence sent: sentList) {
			sent.setDocument(doc);
		}
		doc.setSentences(sentList);
		return doc;
	}
	
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
				      optionProps.setProperty ("index.dir.name",fields[1]);
			    } else if (fields[0].equals("--modelsdir")) {
			      optionProps.setProperty ("opennlp.models.dir",fields[1]);
			    } else if (fields[0].equals("--inputformat")) {
			    	optionProps.setProperty("inputformat", fields[1]);
			    } else if (fields[0].equals("--outputformat")) {
			    	optionProps.setProperty("outputformat", fields[1]);
			    } else if (fields[0].equals("--inputpath")) {
			    	optionProps.setProperty("inputpath", fields[1]);
			    } else if (fields[0].equals("--outputpath")) {
			    	optionProps.setProperty("outputpath", fields[1]);
			    } else if (fields[0].equals("--inputtextformat")) {
			    	optionProps.setProperty("inputtextformat", fields[1]);
			    } else if (fields[0].equals("--annsource")) {
			    	optionProps.setProperty("annsource", fields[1]);
			    }
			}
			i++;
		}
		return optionProps;
	}
	
	public static Properties getProps(String[] args) throws FileNotFoundException, IOException {
		Properties  defaultProps = new Properties(System.getProperties());
		String configFilename = "semrepjava.properties";
		File configFile = new File(configFilename);
		if( configFile.exists() && !configFile.isDirectory()) {
			 defaultProps.load(new FileReader(configFile));
		}
		Properties optionProps = getOptionProps(args);
		defaultProps.putAll(optionProps);
		return defaultProps;
	}
	
	public static void generateChunkOutput(Document doc) throws IOException {
		String outPath = System.getProperty("outputpath");
		String inputFormat = System.getProperty("inputformat");
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath + "/" + doc.getId() + ".ann2"));
			writer.write(sb.toString());
			writer.close();
		}else if (inputFormat.equalsIgnoreCase("singlefile")) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath + ".ann2", true));
			writer.write(sb.toString());
			writer.close();
		}
	}
	
	public static void processFromDirectory(String inPath) throws IOException {
		File[] files = new File(inPath).listFiles();
		String inputTextFormat = System.getProperty("inputtextformat");
		for(File file: files) {
			String filename = file.getName();
			String[] fields = filename.split("\\.");
			if(fields.length == 2 && fields[1].equals("txt")) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				if (inputTextFormat.equalsIgnoreCase("plaintext")) {
					long fileLen = file.length();
				    char[] buf = new char[(int)fileLen];
				    br.read(buf,0, (int)fileLen);
				    br.close();
				    String text = new String(buf);
				    Document doc = processingFromText(fields[0], text);
				    generateChunkOutput(doc);
				}else if (inputTextFormat.equalsIgnoreCase("medline")) {
				    MedLineDocument md = MedLineParser.parseSingleMedLine(br);
				    generateChunkOutput(md);
				}
				br.close();
			}
		}
		
	}
	
	public static void processFromSingleFile(String inPath) throws IOException {
		String inputTextFormat = System.getProperty("inputtextformat");
		BufferedReader br = new BufferedReader(new FileReader(inPath));
		if (inputTextFormat.equalsIgnoreCase("plaintext")) {		
			int count = 0;
			String line;
			StringBuilder sb = new StringBuilder();
			do {
				line = br.readLine();
				if( line == null || line.trim().isEmpty()) {
					count++;
					Document doc = processingFromText(Integer.toString(count),sb.toString());
					sb = new StringBuilder();
					generateChunkOutput(doc);
					
					//test
					MetaMapLiteClient client = new MetaMapLiteClient();
					Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
					client.annotate(doc, System.getProperties(), annotations);
					WSDClient wsdClient = new WSDClient();
					wsdClient.disambiguate(doc, System.getProperties(), annotations);
				}else {
					sb.append(line + " ");
				}
			} while(line != null);
		} else if (inputTextFormat.equalsIgnoreCase("medline")) {
			List<MedLineDocument> mdList = MedLineParser.parseMultiMedLines(br);
			for (MedLineDocument md : mdList) {
				generateChunkOutput(md);
			}
		}
		br.close();
	}
	
    public static void main( String[] args ) throws Exception
    {
    	System.setProperties(getProps(args));
    	
    	String inputFormat = System.getProperty("inputformat");
    	String inPath = System.getProperty("inputpath");
    	
    	if(inputFormat.equalsIgnoreCase("dir")) {
    		processFromDirectory(inPath);	
    	}else if(inputFormat.equalsIgnoreCase("singlefile")) {
    		processFromSingleFile(inPath);
    	}
    	
    }
}
