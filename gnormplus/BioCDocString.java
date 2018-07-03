/**
 * Project: GNormPlus
 * Function: Data storage in BioC format
 */

package gov.nih.nlm.ner.gnormplus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import bioc.BioCAnnotation;
import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.BioCLocation;
import bioc.BioCPassage;
import bioc.io.BioCDocumentWriter;
import bioc.io.BioCFactory;
import bioc.io.woodstox.ConnectorWoodstox;

public class BioCDocString {
    /*
     * Contexts in BioC file
     */
    public ArrayList<String> PMIDs = new ArrayList<>(); // Type: PMIDs
    public ArrayList<ArrayList<String>> PassageNames = new ArrayList(); // PassageName
    public ArrayList<ArrayList<Integer>> PassageOffsets = new ArrayList(); // PassageOffset
    public ArrayList<ArrayList<String>> PassageContexts = new ArrayList(); // PassageContext
    public ArrayList<ArrayList<ArrayList<String>>> Annotations = new ArrayList(); // Annotation - GNormPlus

    public String BioCFormatCheck(String InputFile) throws IOException {

	ConnectorWoodstox connector = new ConnectorWoodstox();
	BioCCollection collection = new BioCCollection();
	try {
	    collection = connector.startRead(new InputStreamReader(new FileInputStream(InputFile), "UTF-8"));
	} catch (UnsupportedEncodingException | FileNotFoundException | XMLStreamException e) {
	    BufferedReader br = new BufferedReader(new FileReader(InputFile));
	    String line = "";
	    String status = "";
	    String Pmid = "";
	    boolean tiabs = false;
	    Pattern patt = Pattern.compile("^([^\\|\\t]+)\\|([^\\|\\t]+)\\|([^\\|\\t]+)$");
	    while ((line = br.readLine()) != null) {
		Matcher mat = patt.matcher(line);
		if (mat.find()) //Title|Abstract
		{
		    if (Pmid.equals("")) {
			Pmid = mat.group(1);
		    } else if (!Pmid.equals(mat.group(1))) {
			return "[Error]: " + InputFile + " - A blank is needed between " + Pmid + " and " + mat.group(1)
				+ ".";
		    }
		    status = "tiabs";
		    tiabs = true;
		} else if (line.contains("\t")) //Annotation
		{
		} else if (line.length() == 0) //Processing
		{
		    if (status.equals("")) {
			if (Pmid.equals("")) {
			    return "[Error]: " + InputFile + " - It's not either BioC or PubTator format.";
			} else {
			    return "[Error]: " + InputFile + " - A redundant blank is after " + Pmid + ".";
			}
		    }
		    Pmid = "";
		    status = "";
		}
	    }
	    br.close();
	    if (tiabs == false) {
		return "[Error]: " + InputFile + " - It's not either BioC or PubTator format.";
	    }
	    if (status.equals("")) {
		return "PubTator";
	    } else {
		return "[Error]: " + InputFile + " - The last column missed a blank.";
	    }
	}
	return "BioC";
    }

    public void PubTator2BioC(String input, String output) throws IOException, XMLStreamException // Input
    {
	/*
	 * PubTator2BioC
	 */
	String parser = BioCFactory.WOODSTOX;
	BioCFactory factory = BioCFactory.newFactory(parser);
	BioCDocumentWriter BioCOutputFormat = factory
		.createBioCDocumentWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
	BioCCollection biocCollection = new BioCCollection();

	//time
	ZoneId zonedId = ZoneId.of("America/Montreal");
	LocalDate today = LocalDate.now(zonedId);
	biocCollection.setDate(today.toString());

	biocCollection.setKey("BioC.key");//key
	biocCollection.setSource("tmVar");//source

	BioCOutputFormat.writeCollectionInfo(biocCollection);
	BufferedReader inputfile = new BufferedReader(new FileReader(input));
	ArrayList<String> ParagraphType = new ArrayList<>(); // Type: Title|Abstract
	ArrayList<String> ParagraphContent = new ArrayList<>(); // Text
	ArrayList<String> annotations = new ArrayList<>(); // Annotation
	String line;
	String Pmid = "";
	while ((line = inputfile.readLine()) != null) {
	    if (line.contains("|") && !line.contains("\t")) //Title|Abstract
	    {
		String str[] = line.split("\\|");
		Pmid = str[0];
		if (str[1].equals("t")) {
		    str[1] = "title";
		}
		if (str[1].equals("a")) {
		    str[1] = "abstract";
		}
		ParagraphType.add(str[1]);
		if (str.length == 3) {
		    ParagraphContent.add(str[2]);
		} else {
		    ParagraphContent.add("- No text -");
		}
	    } else if (line.contains("\t")) //Annotation
	    {
		String anno[] = line.split("\t");
		if (anno.length == 6) {
		    annotations.add(anno[1] + "\t" + anno[2] + "\t" + anno[3] + "\t" + anno[4] + "\t" + anno[5]);
		} else if (anno.length == 5) {
		    annotations.add(anno[1] + "\t" + anno[2] + "\t" + anno[3] + "\t" + anno[4]);
		}
	    } else if (line.length() == 0) //Processing
	    {
		BioCDocument biocDocument = new BioCDocument();
		biocDocument.setID(Pmid);
		int startoffset = 0;
		for (int i = 0; i < ParagraphType.size(); i++) {
		    BioCPassage biocPassage = new BioCPassage();
		    Map<String, String> Infons = new HashMap<>();
		    Infons.put("type", ParagraphType.get(i));
		    biocPassage.setInfons(Infons);
		    biocPassage.setText(ParagraphContent.get(i));
		    biocPassage.setOffset(startoffset);
		    startoffset = startoffset + ParagraphContent.get(i).length() + 1;
		    for (int j = 0; j < annotations.size(); j++) {
			String anno[] = annotations.get(j).split("\t");
			if (Integer.parseInt(anno[0]) <= startoffset
				&& Integer.parseInt(anno[0]) >= startoffset - ParagraphContent.get(i).length()) {
			    BioCAnnotation biocAnnotation = new BioCAnnotation();
			    Map<String, String> AnnoInfons = new HashMap<>();
			    if (anno.length == 5) {
				AnnoInfons.put("Identifier", anno[4]);
			    }
			    AnnoInfons.put("type", anno[3]);
			    biocAnnotation.setInfons(AnnoInfons);
			    BioCLocation location = new BioCLocation();
			    location.setOffset(Integer.parseInt(anno[0]));
			    location.setLength(Integer.parseInt(anno[1]) - Integer.parseInt(anno[0]));
			    biocAnnotation.setLocation(location);
			    biocAnnotation.setText(anno[2]);
			    biocPassage.addAnnotation(biocAnnotation);
			}
		    }
		    biocDocument.addPassage(biocPassage);
		}
		biocCollection.addDocument(biocDocument);
		ParagraphType.clear();
		ParagraphContent.clear();
		annotations.clear();
		BioCOutputFormat.writeDocument(biocDocument);
	    }
	}
	BioCOutputFormat.close();
	inputfile.close();
    }

    public void BioC2PubTator(String input, String output) throws IOException, XMLStreamException //Output
    {
	/*
	 * BioC2PubTator
	 */
	HashMap<String, String> pmidlist = new HashMap<>(); // check if appear duplicate pmids
	boolean duplicate = false;
	BufferedWriter PubTatorOutputFormat = new BufferedWriter(new FileWriter(output));
	ConnectorWoodstox connector = new ConnectorWoodstox();
	BioCCollection collection = new BioCCollection();
	collection = connector.startRead(new InputStreamReader(new FileInputStream(input), "UTF-8"));
	while (connector.hasNext()) {
	    BioCDocument document = connector.next();
	    String PMID = document.getID();
	    if (pmidlist.containsKey(PMID)) {
		System.out.println("\nError: duplicate pmid-" + PMID);
		duplicate = true;
	    } else {
		pmidlist.put(PMID, "");
	    }
	    String Anno = "";
	    for (BioCPassage passage : document.getPassages()) {
		if (passage.getInfon("type").equals("title")) {
		    PubTatorOutputFormat.write(PMID + "|t|" + passage.getText() + "\n");
		} else if (passage.getInfon("type").equals("abstract")) {
		    PubTatorOutputFormat.write(PMID + "|a|" + passage.getText() + "\n");
		} else {
		    PubTatorOutputFormat.write(PMID + "|" + passage.getInfon("type") + "|" + passage.getText() + "\n");
		}

		for (BioCAnnotation annotation : passage.getAnnotations()) {
		    String Annotype = annotation.getInfon("type");
		    String Annoid = "";
		    if (Annotype.equals("Gene")) {
			if (!annotation.getInfon("NCBI Gene").isEmpty()) {
			    Annoid = annotation.getInfon("NCBI Gene");
			}
			//else if((!annotation.getInfon("NCBI Homologene").isEmpty()))
			//{
			//	Annoid = annotation.getInfon("NCBI Homologene");
			//}
			//else if(!annotation.getInfon("FocusSpecies").isEmpty())
			//{
			//	Annoid = annotation.getInfon("FocusSpecies");
			//}
			else {
			    Annoid = annotation.getInfon("Identifier");
			}
		    } else if (Annotype.equals("Species")) {
			if (!annotation.getInfon("NCBI Taxonomy").isEmpty()) {
			    Annoid = annotation.getInfon("NCBI Taxonomy");
			} else {
			    Annoid = annotation.getInfon("Identifier");
			}
		    } else {
			Annoid = annotation.getInfon("Identifier");
		    }
		    int start = annotation.getLocations().get(0).getOffset();
		    int last = start + annotation.getLocations().get(0).getLength();
		    String AnnoMention = annotation.getText();
		    Anno = Anno + PMID + "\t" + start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype + "\t"
			    + Annoid + "\n";
		}
	    }
	    PubTatorOutputFormat.write(Anno + "\n");
	}
	PubTatorOutputFormat.close();
	if (duplicate == true) {
	    System.exit(0);
	}
    }

    public void BioCReader(String input) throws IOException, XMLStreamException {
	ConnectorWoodstox connector = new ConnectorWoodstox();
	BioCCollection collection = new BioCCollection();
	collection = connector.startRead(new InputStreamReader(new FileInputStream(input), "UTF-8"));

	/*
	 * Per document
	 */
	while (connector.hasNext()) {
	    BioCDocument document = connector.next();
	    PMIDs.add(document.getID());

	    ArrayList<String> PassageName = new ArrayList<>(); // array of Passage name
	    ArrayList<Integer> PassageOffset = new ArrayList<>(); // array of Passage offset
	    ArrayList<String> PassageContext = new ArrayList<>(); // array of Passage context
	    ArrayList<ArrayList<String>> AnnotationInPMID = new ArrayList(); // array of Annotations in the PassageName

	    /*
	     * Per Passage
	     */
	    for (BioCPassage passage : document.getPassages()) {
		PassageName.add(passage.getInfon("type")); //Paragraph

		String txt = passage.getText();
		txt = txt.replaceAll("Ï‰", "w");
		txt = txt.replaceAll("Î¼", "u");
		txt = txt.replaceAll("Îº", "k");
		txt = txt.replaceAll("Î±", "a");
		txt = txt.replaceAll("Î³", "r");
		txt = txt.replaceAll("Î²", "b");
		txt = txt.replaceAll("Ã—", "x");
		txt = txt.replaceAll("Â¹", "1");
		txt = txt.replaceAll("Â²", "2");
		txt = txt.replaceAll("Â°", "o");
		txt = txt.replaceAll("Ã¶", "o");
		txt = txt.replaceAll("Ã©", "e");
		txt = txt.replaceAll("Ã ", "a");
		txt = txt.replaceAll("Ã�", "A");
		txt = txt.replaceAll("Îµ", "e");
		txt = txt.replaceAll("Î¸", "O");
		txt = txt.replaceAll("â€¢", ".");
		txt = txt.replaceAll("Âµ", "u");
		txt = txt.replaceAll("Î»", "r");
		txt = txt.replaceAll("â�º", "+");
		txt = txt.replaceAll("Î½", "v");
		txt = txt.replaceAll("Ã¯", "i");
		txt = txt.replaceAll("Ã£", "a");
		txt = txt.replaceAll("â‰¡", "=");
		txt = txt.replaceAll("Ã³", "o");
		txt = txt.replaceAll("Â³", "3");
		txt = txt.replaceAll("ã€–", "[");
		txt = txt.replaceAll("ã€—", "]");
		txt = txt.replaceAll("Ã…", "A");
		txt = txt.replaceAll("Ï�", "p");
		txt = txt.replaceAll("Ã¼", "u");
		txt = txt.replaceAll("É›", "e");
		txt = txt.replaceAll("Ä�", "c");
		txt = txt.replaceAll("Å¡", "s");
		txt = txt.replaceAll("ÃŸ", "b");
		txt = txt.replaceAll("â•�", "=");
		txt = txt.replaceAll("Â£", "L");
		txt = txt.replaceAll("Å�", "L");
		txt = txt.replaceAll("Æ’", "f");
		txt = txt.replaceAll("Ã¤", "a");
		txt = txt.replaceAll("â€“", "-");
		txt = txt.replaceAll("â�»", "-");
		txt = txt.replaceAll("ã€ˆ", "<");
		txt = txt.replaceAll("ã€‰", ">");
		txt = txt.replaceAll("Ï‡", "X");
		txt = txt.replaceAll("Ä�", "D");
		txt = txt.replaceAll("â€°", "%");
		txt = txt.replaceAll("Â·", ".");
		txt = txt.replaceAll("â†’", ">");
		txt = txt.replaceAll("â†�", "<");
		txt = txt.replaceAll("Î¶", "z");
		txt = txt.replaceAll("Ï€", "p");
		txt = txt.replaceAll("Ï„", "t");
		txt = txt.replaceAll("Î¾", "X");
		txt = txt.replaceAll("Î·", "h");
		txt = txt.replaceAll("Ã¸", "0");
		txt = txt.replaceAll("Î”", "D");
		txt = txt.replaceAll("âˆ†", "D");
		txt = txt.replaceAll("âˆ‘", "S");
		txt = txt.replaceAll("Î©", "O");
		txt = txt.replaceAll("Î´", "d");
		txt = txt.replaceAll("Ïƒ", "s");
		txt = txt.replaceAll("Î¦", "F");

		if (passage.getText().equals("") || passage.getText().matches("[ ]+")) {
		    PassageContext.add("-notext-"); //Context
		} else {
		    PassageContext.add(txt); //Context
		}
		PassageOffset.add(passage.getOffset()); //Offset
		ArrayList<String> AnnotationInPassage = new ArrayList<>(); // array of Annotations in the PassageName
		AnnotationInPMID.add(AnnotationInPassage);
	    }
	    PassageNames.add(PassageName);
	    PassageContexts.add(PassageContext);
	    PassageOffsets.add(PassageOffset);
	    Annotations.add(AnnotationInPMID);
	}
    }

    public void BioCReaderWithAnnotation(String input) throws IOException, XMLStreamException {
	ConnectorWoodstox connector = new ConnectorWoodstox();
	BioCCollection collection = new BioCCollection();
	collection = connector.startRead(new InputStreamReader(new FileInputStream(input), "UTF-8"));

	/*
	 * Per document
	 */
	while (connector.hasNext()) {
	    BioCDocument document = connector.next();
	    PMIDs.add(document.getID());

	    ArrayList<String> PassageName = new ArrayList<>(); // array of Passage name
	    ArrayList<Integer> PassageOffset = new ArrayList<>(); // array of Passage offset
	    ArrayList<String> PassageContext = new ArrayList<>(); // array of Passage context
	    ArrayList<ArrayList<String>> AnnotationInPMID = new ArrayList(); // array of Annotations in the PassageName

	    /*
	     * Per Passage
	     */
	    for (BioCPassage passage : document.getPassages()) {
		PassageName.add(passage.getInfon("type")); //Paragraph

		String txt = passage.getText();
		txt = txt.replaceAll("Ï‰", "w");
		txt = txt.replaceAll("Î¼", "u");
		txt = txt.replaceAll("Îº", "k");
		txt = txt.replaceAll("Î±", "a");
		txt = txt.replaceAll("Î³", "r");
		txt = txt.replaceAll("Î²", "b");
		txt = txt.replaceAll("Ã—", "x");
		txt = txt.replaceAll("Â¹", "1");
		txt = txt.replaceAll("Â²", "2");
		txt = txt.replaceAll("Â°", "o");
		txt = txt.replaceAll("Ã¶", "o");
		txt = txt.replaceAll("Ã©", "e");
		txt = txt.replaceAll("Ã ", "a");
		txt = txt.replaceAll("Ã�", "A");
		txt = txt.replaceAll("Îµ", "e");
		txt = txt.replaceAll("Î¸", "O");
		txt = txt.replaceAll("â€¢", ".");
		txt = txt.replaceAll("Âµ", "u");
		txt = txt.replaceAll("Î»", "r");
		txt = txt.replaceAll("â�º", "+");
		txt = txt.replaceAll("Î½", "v");
		txt = txt.replaceAll("Ã¯", "i");
		txt = txt.replaceAll("Ã£", "a");
		txt = txt.replaceAll("â‰¡", "=");
		txt = txt.replaceAll("Ã³", "o");
		txt = txt.replaceAll("Â³", "3");
		txt = txt.replaceAll("ã€–", "[");
		txt = txt.replaceAll("ã€—", "]");
		txt = txt.replaceAll("Ã…", "A");
		txt = txt.replaceAll("Ï�", "p");
		txt = txt.replaceAll("Ã¼", "u");
		txt = txt.replaceAll("É›", "e");
		txt = txt.replaceAll("Ä�", "c");
		txt = txt.replaceAll("Å¡", "s");
		txt = txt.replaceAll("ÃŸ", "b");
		txt = txt.replaceAll("â•�", "=");
		txt = txt.replaceAll("Â£", "L");
		txt = txt.replaceAll("Å�", "L");
		txt = txt.replaceAll("Æ’", "f");
		txt = txt.replaceAll("Ã¤", "a");
		txt = txt.replaceAll("â€“", "-");
		txt = txt.replaceAll("â�»", "-");
		txt = txt.replaceAll("ã€ˆ", "<");
		txt = txt.replaceAll("ã€‰", ">");
		txt = txt.replaceAll("Ï‡", "X");
		txt = txt.replaceAll("Ä�", "D");
		txt = txt.replaceAll("â€°", "%");
		txt = txt.replaceAll("Â·", ".");
		txt = txt.replaceAll("â†’", ">");
		txt = txt.replaceAll("â†�", "<");
		txt = txt.replaceAll("Î¶", "z");
		txt = txt.replaceAll("Ï€", "p");
		txt = txt.replaceAll("Ï„", "t");
		txt = txt.replaceAll("Î¾", "X");
		txt = txt.replaceAll("Î·", "h");
		txt = txt.replaceAll("Ã¸", "0");
		txt = txt.replaceAll("Î”", "D");
		txt = txt.replaceAll("âˆ†", "D");
		txt = txt.replaceAll("âˆ‘", "S");
		txt = txt.replaceAll("Î©", "O");
		txt = txt.replaceAll("Î´", "d");
		txt = txt.replaceAll("Ïƒ", "s");
		txt = txt.replaceAll("Î¦", "F");

		if (passage.getText().equals("") || passage.getText().matches("[ ]+")) {
		    PassageContext.add("-notext-"); //Context
		} else {
		    PassageContext.add(txt); //Context
		}
		PassageOffset.add(passage.getOffset()); //Offset
		ArrayList<String> AnnotationInPassage = new ArrayList<>(); // array of Annotations in the PassageName

		/*
		 * Per Annotation : start last mention type id
		 */
		for (BioCAnnotation Anno : passage.getAnnotations()) {
		    int start = Anno.getLocations().get(0).getOffset() - passage.getOffset(); // start
		    int last = start + Anno.getLocations().get(0).getLength(); // last
		    String AnnoMention = Anno.getText(); // mention
		    String Annotype = Anno.getInfon("type"); // type
		    String Annoid = Anno.getInfon("Identifier"); // identifier | MESH
		    if (Annoid == null) {
			Annoid = Anno.getInfon("MESH"); // identifier | MESH
		    }
		    if (Annoid == null || Annoid.equals("null")) {
			AnnotationInPassage.add(start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype); //paragraph
		    } else {
			AnnotationInPassage
				.add(start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype + "\t" + Annoid); //paragraph
		    }
		}
		AnnotationInPMID.add(AnnotationInPassage);
	    }
	    PassageNames.add(PassageName);
	    PassageContexts.add(PassageContext);
	    PassageOffsets.add(PassageOffset);
	    Annotations.add(AnnotationInPMID);
	}
    }

    // public void BioCOutput(String input, String output, ArrayList<ArrayList<ArrayList<String>>> Annotations,
    public List<String> BioCOutput(String passage_Text, String output,
	    ArrayList<ArrayList<ArrayList<String>>> Annotations, boolean Final) throws IOException, XMLStreamException {
	// BioCDocumentWriter BioCOutputFormat = BioCFactory.newFactory(BioCFactory.WOODSTOX)
	//	.createBioCDocumentWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
	BioCCollection biocCollection_input = new BioCCollection();
	BioCCollection biocCollection_output = new BioCCollection();

	//input: BioC
	ConnectorWoodstox connector = new ConnectorWoodstox();
	// biocCollection_input = connector.startRead(new InputStreamReader(new FileInputStream(input), "UTF-8"));
	// BioCOutputFormat.writeCollectionInfo(biocCollection_input);
	int i = 0; //count for pmid
	List<String> annotation = new ArrayList<>();
	// while (connector.hasNext()) {
	//    BioCDocument document_output = new BioCDocument();
	//    BioCDocument document_input = connector.next();
	//    String PMID = document_input.getID();
	//    document_output.setID(PMID);

	int j = 0; //count for paragraph
	//    for (BioCPassage passage_input : document_input.getPassages()) {
	//	BioCPassage passage_output = passage_input;
	//	passage_output.clearAnnotations();
	//	int passage_Offset = passage_input.getOffset();
	// String passage_Text = passage_input.getText();

	ArrayList<String> AnnotationInPassage = new ArrayList<>();
	//ArrayList<String> AnnotationInPassage = Annotations.get(i).get(j);
	/*
	 * Boundary check for String version 05/01/2018 Dongwook Shin Annotations.get(i)
	 */
	if (j < Annotations.get(i).size())
	    for (int a = 0; a < Annotations.get(i).get(j).size(); a++) {
		String Anno[] = Annotations.get(i).get(j).get(a).split("\\t");
		int start = Integer.parseInt(Anno[0]);
		int last = Integer.parseInt(Anno[1]);
		String mention = Anno[2];
		if (Final == true) {
		    mention = passage_Text.substring(start, last);
		}
		String type = Anno[3];
		String id = ""; // optional
		if (Anno.length >= 5) {
		    id = Anno[4];
		}
		boolean found = false;
		if (Final == true) {
		    for (int b = 0; b < AnnotationInPassage.size(); b++) {
			String Annob[] = AnnotationInPassage.get(b).split("\\t");
			int startb = Integer.parseInt(Annob[0]);
			int lastb = Integer.parseInt(Annob[1]);
			String typeb = Annob[3];
			String idb = ""; // optional
			if (Annob.length >= 5) {
			    idb = Annob[4];
			}

			if (start == startb && last == lastb && type.equals(typeb)) {
			    found = true;
			    if (id.matches("(Focus|Right|Left|Prefix|GeneID):[0-9]+") && (!idb.equals(""))) {
			    } else if (idb.matches("(Focus|Right|Left|Prefix|GeneID):[0-9]+")
				    && (!id.matches("(Focus|Right|Left|Prefix|GeneID):[0-9]+")) && (!id.equals(""))) {
				AnnotationInPassage.set(b,
					start + "\t" + last + "\t" + mention + "\t" + type + "\t" + id);
			    } else {
				if (id.equals("")) {
				} else {
				    AnnotationInPassage.set(b,
					    start + "\t" + last + "\t" + mention + "\t" + type + "\t" + idb + ";" + id);
				}

			    }
			    break;
			}
		    }
		}
		if (found == false) {
		    AnnotationInPassage.add(Annotations.get(i).get(j).get(a));
		}
	    }

	for (int a = 0; a < AnnotationInPassage.size(); a++) {
	    String Anno[] = AnnotationInPassage.get(a).split("\\t");
	    HashMap<String, String> id_hash = new HashMap<>();
	    if (Anno.length >= 5) {
		String ids = Anno[4];
		String idlist[] = ids.split(",");
		for (int b = 0; b < idlist.length; b++) {
		    id_hash.put(idlist[b], "");
		}
		ids = "";
		for (String id : id_hash.keySet()) {
		    if (ids.equals("")) {
			ids = id;
		    } else {
			ids = ids + ";" + id;
		    }
		}
		AnnotationInPassage.set(a, Anno[0] + "\t" + Anno[1] + "\t" + Anno[2] + "\t" + Anno[3] + "\t" + ids);
	    }
	}

	for (int a = 0; a < AnnotationInPassage.size(); a++) {
	    String Anno[] = AnnotationInPassage.get(a).split("\\t");
	    int start = Integer.parseInt(Anno[0]);
	    int last = Integer.parseInt(Anno[1]);
	    String mention = Anno[2];
	    if (Final == true) {
		mention = passage_Text.substring(start, last);
	    }
	    String type = Anno[3];
	    if (type.equals("GeneID")) {
		type = "Gene";
	    }
	    BioCAnnotation biocAnnotation = new BioCAnnotation();
	    Map<String, String> AnnoInfons = new HashMap<>();
	    AnnoInfons.put("type", type);
	    if (Anno.length == 5) {
		String identifier = Anno[4];
		if (Final == true) {
		    if (type.matches("(Gene|FamilyName|DomainMotif)")) {
			Pattern ptmp0 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID)\\:([0-9]+)\\|([0-9]+)$");
			Matcher mtmp0 = ptmp0.matcher(identifier);
			Pattern ptmp1 = Pattern
				.compile("^(Focus|Right|Left|Prefix|GeneID)\\:([0-9]+)\\|([0-9]+)\\-([0-9]+)$");
			Matcher mtmp1 = ptmp1.matcher(identifier);
			Pattern ptmp2 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID)\\:([0-9]+)$");
			Matcher mtmp2 = ptmp2.matcher(identifier);
			Pattern ptmp3 = Pattern.compile("^Homo\\:([0-9]+)$");
			Matcher mtmp3 = ptmp3.matcher(identifier);
			if (mtmp0.find()) {
			    String Method_SA = mtmp0.group(1);
			    String TaxonomyID = mtmp0.group(2);
			    String NCBIGeneID = mtmp0.group(3);
			    AnnoInfons.put("NCBI Gene", NCBIGeneID);
			    // System.out.println("NCBI Gene 1: " + NCBIGeneID);
			} else if (mtmp1.find()) {
			    String Method_SA = mtmp1.group(1);
			    String TaxonomyID = mtmp1.group(2);
			    String NCBIGeneID = mtmp1.group(3);
			    String HomoID = mtmp1.group(4);
			    AnnoInfons.put("NCBI Gene", NCBIGeneID);
			    // System.out.println("NCBI Gene 2: " + NCBIGeneID);
			} else if (mtmp2.find()) {
			    String Method_SA = mtmp2.group(1);
			    String TaxonomyID = mtmp2.group(2);
			    AnnoInfons.put("FocusSpecies", "NCBITaxonomyID:" + TaxonomyID);
			} else if (mtmp3.find()) {
			    String Method_SA = mtmp3.group(1);
			    String HomoID = mtmp3.group(2);
			    AnnoInfons.put("NCBI Homologene", HomoID);
			} else {
			    String identifiers[] = identifier.split(";");
			    if (identifiers.length > 1) {
				ArrayList<String> identifierSTR = new ArrayList<>();
				for (int idi = 0; idi < identifiers.length; idi++) {
				    Pattern ptmp4 = Pattern.compile(
					    "^(Focus|Right|Left|Prefix|GeneID)\\:([0-9]+)\\|([0-9]+)\\-([0-9]+)$");
				    Matcher mtmp4 = ptmp4.matcher(identifiers[idi]);
				    Pattern ptmp5 = Pattern
					    .compile("^(Focus|Right|Left|Prefix|GeneID)\\:([0-9]+)\\|([0-9]+)$");
				    Matcher mtmp5 = ptmp5.matcher(identifiers[idi]);
				    if (mtmp4.find()) {
					String Method_SA = mtmp4.group(1);
					String TaxonomyID = mtmp4.group(2);
					String NCBIGeneID = mtmp4.group(3);
					String HomoID = mtmp4.group(4);
					if (!identifierSTR.contains(NCBIGeneID)) {
					    identifierSTR.add(NCBIGeneID);
					}
				    } else if (mtmp5.find()) {
					String Method_SA = mtmp5.group(1);
					String TaxonomyID = mtmp5.group(2);
					String NCBIGeneID = mtmp5.group(3);
					if (!identifierSTR.contains(NCBIGeneID)) {
					    identifierSTR.add(NCBIGeneID);
					}
				    }
				}
				String idSTR = "";
				for (int x = 0; x < identifierSTR.size(); x++) {
				    if (idSTR.equals("")) {
					idSTR = identifierSTR.get(x);
				    } else {
					idSTR = idSTR + ";" + identifierSTR.get(x);
				    }
				}
				AnnoInfons.put("NCBI Gene", idSTR);
				// System.out.println("NCBI Gene 3: " + idSTR);
			    }
			    //else
			    //{
			    //	AnnoInfons.put("Identifier", identifier);
			    //}
			}
		    } else if (type.matches("(Cell|Species|Genus|Strain)")) {
			AnnoInfons.put("type", "Species");
			AnnoInfons.put("NCBI Taxonomy", identifier);
		    } else {
			AnnoInfons.put("Identifier", identifier);
		    }
		} else {
		    AnnoInfons.put("Identifier", identifier);
		}
	    }
	    biocAnnotation.setInfons(AnnoInfons);
	    // BioCLocation location = new BioCLocation();
	    // location.setOffset(start + passage_Offset);
	    // location.setLength(last - start);
	    // biocAnnotation.setLocation(location);
	    //  biocAnnotation.setText(mention);
	    if (Final == true) {
		if (AnnoInfons.containsKey("Identifier") || AnnoInfons.containsKey("NCBI Homologene")
			|| AnnoInfons.containsKey("NCBI Gene") || AnnoInfons.containsKey("NCBI Taxonomy")) {
		    // passage_output.addAnnotation(biocAnnotation);
		    // Add gene Id, June 4 2018 Dongwook Shin
		    String annstr = null;
		    if (AnnoInfons.get("NCBI Gene") != null)
			annstr = new String(start + "\t" + last + "\t" + mention + "\t" + AnnoInfons.get("type") + "\t"
				+ AnnoInfons.get("NCBI Gene"));
		    else
			annstr = new String(
				start + "\t" + last + "\t" + mention + "\t" + AnnoInfons.get("type") + "\t0");

		    annotation.add(annstr);
		}
	    } else {
		// passage_output.addAnnotation(biocAnnotation);
		String annstr = new String(start + "\t" + last + "\t" + mention + "\t" + AnnoInfons.get("type"));
		annotation.add(annstr);
	    }
	}
	// document_output.addPassage(passage_output);
	j++;
	//  }
	// biocCollection_output.addDocument(document_output);
	// BioCOutputFormat.writeDocument(document_output);
	i++;
	// }
	// BioCOutputFormat.close();
	/*
	 * System.out.println("\t====== Inside BioCOutput ======>>>"); for (String ann :
	 * annotation) { System.out.println("\t" + ann.toString()); }
	 * System.out.println( "\t<<<====== In BioCOutput ======");
	 */
	return annotation;
    }
}