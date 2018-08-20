package gov.nih.nlm.ner.gnormplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Concept;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ling.util.FileUtils;

/**
 * A class that provides access to GNormPlus functionality.
 * <p>
 * 
 * @author Halil Kilicoglu
 *
 */
public class GNormPlusStringWrapper {
    private static Logger log = Logger.getLogger(GNormPlusStringWrapper.class.getName());

    private PrefixTree PT_Species = null;
    private PrefixTree PT_Cell = null;
    private PrefixTree PT_CTDGene = null;
    private PrefixTree PT_Gene = null;
    private PrefixTree PT_GeneChromosome = null;
    private HashMap<String, String> ent_hash = null;
    private HashMap<String, String> GenusID_hash = null;
    private HashMap<String, String> StrainID_hash = null;
    private HashMap<String, String> PrefixID_hash = null;
    private HashMap<String, Double> TaxFreq_hash = null;
    private HashMap<String, String> GeneScoring_hash = null;
    private HashMap<String, Double> GeneScoringDF_hash = null;
    private HashMap<String, String> GeneIDs_hash = null;
    ArrayList<String> SuffixTranslationMap = null;
    private HashMap<String, String> Pmid2Abb_hash = null;
    private HashMap<String, String> PmidAbb2LF_lc_hash = null;
    private HashMap<String, String> PmidLF2Abb_lc_hash = null;
    private HashMap<String, String> PmidAbb2LF_hash = null;
    private HashMap<String, String> PmidLF2Abb_hash = null;
    private HashMap<String, String> Pmid2ChromosomeGene_hash = null;
    private HashMap<String, String> SimConceptMention2Type_hash = null;
    private HashMap<String, String> SP_Virus2Human_hash = null;

    private static Properties properties = null;
    private static GNormPlusStringWrapper gNormPlus = null;
    BioCDocString BioCDocobj = null;

    GNormPlusStringWrapper() {

    }

    GNormPlusStringWrapper(String configFile) throws IOException {
	initConfig(configFile);

	PT_Species = new PrefixTree();
	PT_Cell = new PrefixTree();
	PT_CTDGene = new PrefixTree();
	PT_Gene = new PrefixTree();
	PT_GeneChromosome = new PrefixTree();
	ent_hash = new HashMap<>();
	GenusID_hash = new HashMap<>();
	StrainID_hash = new HashMap<>();
	PrefixID_hash = new HashMap<>();
	TaxFreq_hash = new HashMap<>();
	GeneScoring_hash = new HashMap<>();
	GeneScoringDF_hash = new HashMap<>();
	GeneIDs_hash = new HashMap<>();
	SuffixTranslationMap = new ArrayList<>();
	Pmid2Abb_hash = new HashMap<>();
	PmidAbb2LF_lc_hash = new HashMap<>();
	PmidLF2Abb_lc_hash = new HashMap<>();
	PmidAbb2LF_hash = new HashMap<>();
	PmidLF2Abb_hash = new HashMap<>();
	Pmid2ChromosomeGene_hash = new HashMap<>();
	SimConceptMention2Type_hash = new HashMap<>();
	SP_Virus2Human_hash = new HashMap<>();

	initDictionaries();
    }

    /**
     * Instantiates a GNormPlus instance from a config file
     * 
     * @param configFile
     *            the config file
     * @return a GNormPlus instance
     */
    public static GNormPlusStringWrapper getInstance(String configFile) throws IOException {
	if (gNormPlus == null) {
	    log.info("Initializing a GNormPlus instance...");
	    gNormPlus = new GNormPlusStringWrapper(configFile);

	}
	return gNormPlus;
    }

    public static GNormPlusStringWrapper getInstance() {
	return gNormPlus;
    }

    /**
     * Checks whether gNormPlus is instantiated
     * 
     * @return true if there is a gNormPlus instance
     */
    public static boolean instantiated() {
	return (gNormPlus != null);
    }

    protected void initConfig(String filename) throws FileNotFoundException, IOException {
	properties = new Properties();
	BufferedReader br = new BufferedReader(new FileReader(filename));
	String line = "";
	Pattern ptmp = Pattern.compile("^	([A-Za-z]+) = ([^ \\t\\n\\r]+)$");
	while ((line = br.readLine()) != null) {
	    Matcher mtmp = ptmp.matcher(line);
	    if (mtmp.find()) {
		properties.put(mtmp.group(1), mtmp.group(2));
	    }
	}
	br.close();
	if (!properties.containsKey("GeneIDMatch")) {
	    properties.put("GeneIDMatch", "True");
	}
    }

    private void initDictionaries() throws FileNotFoundException, IOException {
	String species = properties.getProperty("FocusSpecies");
	String folder = properties.getProperty("DictionaryFolder");
	BufferedReader br;
	String line = "";

	double startTime, endTime, totTime;
	startTime = System.currentTimeMillis();// start time

	/** CTDGene */
	PT_CTDGene.TreeFile2Tree(folder + "/PT_CTDGene.txt");

	/** ent */
	br = new BufferedReader(new FileReader(folder + "/ent.rev.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t"); // &#x00391; Alpha
	    ent_hash.put(l[0], l[1]);
	}
	br.close();

	/** Species */
	PT_Species.TreeFile2Tree(folder + "/PT_Species.txt");

	/** Cell */
	PT_Cell.TreeFile2Tree(folder + "/PT_Cell.txt");

	/** Genus */
	br = new BufferedReader(new FileReader(folder + "/SPGenus.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    GenusID_hash.put(l[0], l[1]); // tax id -> Genus
	}
	br.close();

	/** Strain */
	br = new BufferedReader(new FileReader(folder + "/SPStrain.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    StrainID_hash.put(l[0], l[1]); // tax id -> strain
	}
	br.close();
	/** Prefix */
	br = new BufferedReader(new FileReader(folder + "/SPPrefix.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    PrefixID_hash.put(l[0], l[1]); // tax id -> prefix
	}
	br.close();
	/** Frequency */
	br = new BufferedReader(new FileReader(folder + "/taxonomy_freq.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    TaxFreq_hash.put(l[0], Double.parseDouble(l[1]) / 200000000); // tax
									  // id
									  // ->
									  // prefix
	}
	br.close();

	/** SP_Virus2Human_hash */
	br = new BufferedReader(new FileReader(folder + "/SP_Virus2HumanList.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    SP_Virus2Human_hash.put(line, "9606");
	}
	br.close();

	/** SimConcept.MentionType */
	br = new BufferedReader(new FileReader(folder + "/SimConcept.MentionType.txt"));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    SimConceptMention2Type_hash.put(l[0], l[1]);
	}
	br.close();

	/** Gene */
	PT_Gene.TreeFile2Tree(folder + "/PT_Gene.txt");

	/** GeneScoring */
	String FileName = folder + "/GeneScoring.txt";

	if ((!species.equals("")) && (!species.equals("All"))) {
	    FileName = folder + "/GeneScoring." + species + ".txt";
	}
	br = new BufferedReader(new FileReader(FileName));
	line = "";
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    GeneScoring_hash.put(l[0], l[1] + "\t" + l[2] + "\t" + l[3] + "\t" + l[4] + "\t" + l[5] + "\t" + l[6]);
	}
	br.close();

	/** GeneScoring.DF */
	FileName = folder + "/GeneScoring.DF.txt";
	if ((!species.equals("")) && (!species.equals("All"))) {
	    FileName = folder + "/GeneScoring.DF." + species + ".txt";
	}
	br = new BufferedReader(new FileReader(FileName));
	double Sum = Double.parseDouble(br.readLine());
	while ((line = br.readLine()) != null) {
	    String l[] = line.split("\t");
	    // token -> idf
	    GeneScoringDF_hash.put(l[0], Math.log10(Sum / Double.parseDouble(l[1])));
	}
	br.close();

	/** Suffix Translation */
	HashMap<String, String> tmp = new HashMap<>();
	SuffixTranslationMap.add("alpha-a");
	SuffixTranslationMap.add("alpha-1");
	SuffixTranslationMap.add("a-alpha");
	SuffixTranslationMap.add("a-1");
	SuffixTranslationMap.add("1-alpha");
	SuffixTranslationMap.add("1-a");
	SuffixTranslationMap.add("beta-b");
	SuffixTranslationMap.add("beta-2");
	SuffixTranslationMap.add("b-beta");
	SuffixTranslationMap.add("b-2");
	SuffixTranslationMap.add("2-beta");
	SuffixTranslationMap.add("2-b");
	SuffixTranslationMap.add("gamma-g");
	SuffixTranslationMap.add("gamma-y");
	SuffixTranslationMap.add("g-gamma");
	SuffixTranslationMap.add("y-gamma");

	/** GeneID */
	if (properties.containsKey("GeneIDMatch")
		&& properties.getProperty("GeneIDMatch").toLowerCase().equals("true")) {
	    br = new BufferedReader(new FileReader(folder + "/GeneIDs.txt"));
	    line = "";
	    while ((line = br.readLine()) != null) {
		String l[] = line.split("\t");
		GeneIDs_hash.put(l[0], l[1]);
	    }
	    br.close();
	}

	/** GeneChromosome */
	PT_GeneChromosome.TreeFile2Tree(folder + "/PT_GeneChromosome.txt");
	endTime = System.currentTimeMillis();
	totTime = endTime - startTime;
	log.info("Loading Gene Dictionary : Processing Time:" + totTime / 1000 + "sec");
    }

    public PrefixTree getPT_Species() {
	return PT_Species;
    }

    public PrefixTree getPT_Cell() {
	return PT_Cell;
    }

    public PrefixTree getPT_CTDGene() {
	return PT_CTDGene;
    }

    public PrefixTree getPT_Gene() {
	return PT_Gene;
    }

    public PrefixTree getPT_GeneChromosome() {
	return PT_GeneChromosome;
    }

    public HashMap<String, String> getEnt_hash() {
	return ent_hash;
    }

    public HashMap<String, String> getGenusID_hash() {
	return GenusID_hash;
    }

    public HashMap<String, String> getStrainID_hash() {
	return StrainID_hash;
    }

    public HashMap<String, String> getPrefixID_hash() {
	return PrefixID_hash;
    }

    public HashMap<String, Double> getTaxFreq_hash() {
	return TaxFreq_hash;
    }

    public HashMap<String, String> getGeneScoring_hash() {
	return GeneScoring_hash;
    }

    public HashMap<String, Double> getGeneScoringDF_hash() {
	return GeneScoringDF_hash;
    }

    public HashMap<String, String> getGeneIDs_hash() {
	return GeneIDs_hash;
    }

    public ArrayList<String> getSuffixTranslationMap() {
	return SuffixTranslationMap;
    }

    public HashMap<String, String> getPmid2Abb_hash() {
	return Pmid2Abb_hash;
    }

    public HashMap<String, String> getPmidAbb2LF_lc_hash() {
	return PmidAbb2LF_lc_hash;
    }

    public HashMap<String, String> getPmidLF2Abb_lc_hash() {
	return PmidLF2Abb_lc_hash;
    }

    public HashMap<String, String> getPmidAbb2LF_hash() {
	return PmidAbb2LF_hash;
    }

    public HashMap<String, String> getPmidLF2Abb_hash() {
	return PmidLF2Abb_hash;
    }

    public HashMap<String, String> getPmid2ChromosomeGene_hash() {
	return Pmid2ChromosomeGene_hash;
    }

    public HashMap<String, String> getSimConceptMention2Type_hash() {
	return SimConceptMention2Type_hash;
    }

    public HashMap<String, String> getSP_Virus2Human_hash() {
	return SP_Virus2Human_hash;
    }

    // public BioCDocString annotateFile(String inFile, String outFile) throws IOException, XMLStreamException {
    public List<String> annotateFile(String inFile) throws IOException, XMLStreamException {
	String inFileName = inFile.substring(inFile.lastIndexOf(File.separator) + 1);
	String TrainTest = "Test";
	String species = properties.getProperty("FocusSpecies");
	List<String> annotation = null;

	double startTime, endTime, totTime;
	startTime = System.currentTimeMillis();// start time

	// File f = new File(outFile);
	// if (f.exists() && !f.isDirectory()) {
	//    System.out.println(inFile + " - Done. (The output file exists in output folder)");
	// } else {
	BioCDocobj = new BioCDocString();

	/*
	 * Format Check
	 */
	String Format = "";
	String checkR = BioCDocobj.BioCFormatCheck(inFile);
	if (checkR.equals("BioC")) {
	    Format = "BioC";
	    BioCDocobj.BioCReader(inFile);
	} else if (checkR.equals("PubTator")) {
	    Format = "PubTator";
	    BioCDocobj.PubTator2BioC(inFile, "tmp/" + inFileName);
	    BioCDocobj.BioCReader("tmp/" + inFileName);
	} else {
	    System.out.println(checkR);
	    System.exit(0);
	}

	// System.out.println(inFile + " - (" + Format + " format) : Processing ... \r");

	/*
	 * GNR
	 */
	{
	    GNRString GNRobj = new GNRString();

	    if (Format.equals("PubTator")) {
		GNRobj.LoadInputFile(BioCDocobj, "tmp/" + inFileName, gNormPlus, "tmp/" + inFileName + ".Abb",
			TrainTest);
	    } else if (Format.equals("BioC")) {
		GNRobj.LoadInputFile(BioCDocobj, inFile, gNormPlus, "tmp/" + inFileName + ".Abb", TrainTest);
	    }

	    //
	    /*
	     * GNRobj.FeatureExtraction(BioCDocobj, gNormPlus, "tmp/" + inFileName +
	     * ".data", "tmp/" + inFileName + ".loca", TrainTest);
	     */
	    DataFormat df = GNRobj.FeatureExtraction(BioCDocobj, gNormPlus, TrainTest);

	    // Test
	    String outputData = GNRobj.CRF_test(properties.getProperty("GNRModel"), df.Data, "top3"); // top3
	    df.Output = outputData;
	    /*
	     * BufferedReader br1 = new BufferedReader( new FileReader(
	     * "C:\\Projects\\TranslationalPortal2\\tmp\\Test-loc.txt")); BufferedReader br2
	     * = new BufferedReader( new FileReader(
	     * "C:\\Projects\\TranslationalPortal2\\tmp\\Test-correct_output.txt" ));
	     * StringBuffer sb = new StringBuffer(); String line = null; while ((line =
	     * br1.readLine()) != null) { sb.append(line +
	     * System.getProperty("line.separator")); } sb.append(new String("") +
	     * System.getProperty("line.separator")); df.Location = sb.toString(); sb = new
	     * StringBuffer(); while ((line = br2.readLine()) != null) { sb.append(line +
	     * System.getProperty("line.separator")); } df.Output = sb.toString();
	     */

	    if (Format.equals("PubTator")) {
		GNRobj.ReadCRFresult(BioCDocobj, df.Location, df.Output, 0.005, 0.05); // 0.005,0.05
		GNRobj.PostProcessing(BioCDocobj, gNormPlus, "tmp/" + inFileName);
	    } else if (Format.equals("BioC")) {
		/*
		 * GNRobj.ReadCRFresult(BioCDocobj, "tmp/" + inFileName + ".loca", "tmp/" +
		 * inFileName + ".output", "tmp/" + inFileName + ".GNR.xml", 0.005, 0.05); //
		 * 0.005,0.05
		 */
		GNRobj.ReadCRFresult(BioCDocobj, df.Location, df.Output, 0.005, 0.05); // 0.005,0.05
		GNRobj.PostProcessing(BioCDocobj, gNormPlus, inFile);
	    }
	}

	/*
	 * SR & SA
	 */

	SRString SRobj = new SRString();
	SRobj.SpeciesRecognition(BioCDocobj, gNormPlus);
	if ((!species.equals("")) && (!species.equals("All"))) {
	    SRobj.SpeciesAssignment(BioCDocobj, gNormPlus);
	    /*
	     * if(Format.equals("PubTator")) {
	     * SRobj.SpeciesAssignment("tmp/"+inFileName,"tmp/"+inFileName+
	     * ".SA.xml",species); } else if(Format.equals("BioC")) {
	     * SRobj.SpeciesAssignment(inFile,"tmp/"+inFileName+".SA.xml", species); }
	     */
	} else {
	    SRobj.SpeciesAssignment(BioCDocobj, gNormPlus);
	    /*
	     * if(Format.equals("PubTator")) {
	     * SRobj.SpeciesAssignment("tmp/"+inFileName,"tmp/"+inFileName+ ".SA.xml"); }
	     * else if(Format.equals("BioC")) {
	     * SRobj.SpeciesAssignment(inFile,"tmp/"+inFileName+".SA.xml"); }
	     */
	}

	/*
	 * SimConcept
	 */
	{
	    SimConceptString SCobj = new SimConceptString();
	    DataFormat df = SCobj.FeatureExtraction_Test(BioCDocobj, gNormPlus);
	    String outputData = SCobj.CRF_test(properties.getProperty("SCModel"), df.Data);
	    df.Output = outputData;

	    /*
	     * if(Format.equals("PubTator")) {
	     * SCobj.ReadCRFresult("tmp/"+inFileName,"tmp/"+inFileName+
	     * ".SC.output","tmp/"+inFileName+".SC.xml"); } else {
	     * SCobj.ReadCRFresult(inFile,"tmp/"+inFileName+".SC.output",
	     * "tmp/"+inFileName+".SC.xml"); }
	     */
	    SCobj.ReadCRFresult(BioCDocobj, gNormPlus, df.Output);

	}

	/*
	 * GN
	 */
	GNString GNobj = new GNString();
	GNobj.PreProcessing4GN(BioCDocobj, gNormPlus);
	GNobj.ChromosomeRecognition(BioCDocobj, gNormPlus);
	if (properties.containsKey("GeneIDMatch") && properties.get("GeneIDMatch").equals("True")) {

	    if (Format.equals("PubTator")) {
		/*
		 * GNobj.GeneNormalization("tmp/" + inFileName, BioCDocobj, gNormPlus, "tmp/" +
		 * inFileName + ".GN.xml", true); GNobj.GeneIDRecognition("tmp/" + inFileName,
		 * BioCDocobj, gNormPlus, "tmp/" + inFileName + ".GN.xml");
		 * BioCDocobj.BioC2PubTator("tmp/" + inFileName + ".GN.xml", outFile);
		 */
	    } else if (Format.equals("BioC")) {
		GNobj.GeneNormalization(inFile, BioCDocobj, gNormPlus, true);
		// GNobj.GeneIDRecognition(inFile, BioCDocobj, gNormPlus, outFile);
		annotation = GNobj.GeneIDRecognition(BioCDocobj, gNormPlus);
	    }
	} else {
	    if (Format.equals("PubTator")) {
		GNobj.GeneNormalization("tmp/" + inFileName, BioCDocobj, gNormPlus, false);
		BioCDocobj.BioC2PubTator("tmp/" + inFileName + ".GN.xml", null);
	    } else if (Format.equals("BioC")) {
		GNobj.GeneNormalization(inFile, BioCDocobj, gNormPlus, false);
	    }
	}
	// System.out.println("In java: SimConceptString");
	/*
	 * { SimConceptString SCobj = new SimConceptString();
	 * SCobj.FeatureExtraction_Test(BioCDocobj, gNormPlus, "tmp/" + inFileName +
	 * ".SC.data"); SCobj.CRF_test(properties.getProperty("SCModel"), "tmp/" +
	 * inFileName + ".SC.data", "tmp/" + inFileName + ".SC.output");
	 * SCobj.ReadCRFresult(BioCDocobj, gNormPlus, "tmp/" + inFileName +
	 * ".SC.output", "tmp/" + inFileName + ".SC.xml");
	 * 
	 * }
	 */

	//System.out.println("In java: PreProcessing4GN");
	/*
	 * GN
	 */
	/*
	 * GNString GNobj = new GNString(); GNobj.PreProcessing4GN(BioCDocobj,
	 * gNormPlus, "tmp/" + inFileName + ".PreProcessing4GN.xml");
	 * GNobj.ChromosomeRecognition(BioCDocobj, gNormPlus, "tmp/" + inFileName +
	 * ".GN.xml"); if (properties.containsKey("GeneIDMatch") &&
	 * properties.get("GeneIDMatch").equals("True")) {
	 * 
	 * if (Format.equals("PubTator")) { GNobj.GeneNormalization("tmp/" + inFileName,
	 * BioCDocobj, gNormPlus, "tmp/" + inFileName + ".GN.xml", true);
	 * GNobj.GeneIDRecognition("tmp/" + inFileName, BioCDocobj, gNormPlus, "tmp/" +
	 * inFileName + ".GN.xml"); BioCDocobj.BioC2PubTator("tmp/" + inFileName +
	 * ".GN.xml", outFile); } else if (Format.equals("BioC")) {
	 * GNobj.GeneNormalization(inFile, BioCDocobj, gNormPlus, "tmp/" + inFileName +
	 * ".GN.xml", true); GNobj.GeneIDRecognition(inFile, BioCDocobj, gNormPlus,
	 * outFile); } } else { if (Format.equals("PubTator")) {
	 * GNobj.GeneNormalization("tmp/" + inFileName, BioCDocobj, gNormPlus, "tmp/" +
	 * inFileName + ".GN.xml", false); BioCDocobj.BioC2PubTator("tmp/" + inFileName
	 * + ".GN.xml", outFile); } else if (Format.equals("BioC")) {
	 * GNobj.GeneNormalization(inFile, BioCDocobj, gNormPlus, outFile, false); } }
	 */
	/*
	 * remove tmp files
	 */
	if ((!properties.containsKey("DeleteTmp"))
		|| properties.getProperty("DeleteTmp").toLowerCase().equals("true")) {
	    String path = "tmp";
	    File file = new File(path);
	    // System.out.println("DeleteTmp is true");
	    File[] files = file.listFiles();
	    for (File ftmp : files) {
		if (ftmp.isFile() && ftmp.exists()) {
		    if (ftmp.toString().matches(inFile + ".*")) {
			// System.out.println("Delete file : " + ftmp.toString());
			ftmp.delete();
		    }
		}
	    }
	}

	endTime = System.currentTimeMillis();
	totTime = endTime - startTime;
	// System.out.println(inFile + " - (" + Format + " format) : Processing Time:" + totTime / 1000 + "sec");
	System.out.println("Processing Time:" + totTime / 1000 + "sec");
	// }
	// return BioCDocobj;
	return annotation;

    }

    public void annotate(String inDir, String outDir) {
	double startTime, endTime, totTime;
	startTime = System.currentTimeMillis();// start time

	File folder = new File(inDir);
	File[] listOfFiles = folder.listFiles();
	for (int i = 0; i < listOfFiles.length; i++) {
	    if (listOfFiles[i].isFile()) {
		String InputFile = listOfFiles[i].getAbsolutePath();
		String OutputFile = outDir + File.separator + listOfFiles[i].getName();
		annotate(InputFile, OutputFile);
	    }
	}

	/*
	 * Time stamp - last
	 */
	endTime = System.currentTimeMillis();
	totTime = endTime - startTime;
	System.out.println(inDir + " Processing Time:" + totTime / 1000 + "sec");
    }

    // public void annotateText(String inText,
    // Map<SpanList,LinkedHashSet<Ontology>> annotations) throws IOException,
    // XMLStreamException {
    public Map<SpanList, LinkedHashSet<Ontology>> annotateText(String inText) throws IOException, XMLStreamException {
	Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<>();
	String tempFileName = "tmp" + File.separator + "TMP" + inText.hashCode() + ".txt";
	BioCConverter.convertAndWrite(inText, tempFileName);
	// String tempOutFileName = "TMP" + inText.hashCode() + ".out";
	// BioCDocString BioCDocobj = annotateFile(tempFileName, tempOutFileName);
	List<String> Annotation = annotateFile(tempFileName);
	int id = 0;
	// for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) {
	/** Paragraphs : j */
	// for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) {
	// if (j < BioCDocobj.Annotations.get(i).size()) {
	//    ArrayList<String> Annotation = BioCDocobj.Annotations.get(i).get(j); // Annotation
	for (int k = 0; k < Annotation.size(); k++) // k : Annotations
	{
	    String anno[] = Annotation.get(k).split("\t");
	    int start = Integer.parseInt(anno[0]);
	    int last = Integer.parseInt(anno[1]);
	    String mention = anno[2];
	    String type = anno[3];
	    String geneid = anno[4];

	    SpanList sp = new SpanList(start, last);
	    LinkedHashSet<Ontology> concs = new LinkedHashSet<>();
	    if (annotations.containsKey(sp)) {
		concs = annotations.get(sp);
	    }
	    LinkedHashSet<String> semtypes = new LinkedHashSet<>();
	    semtypes.add(type);
	    Concept conc = new Concept("GNP_" + geneid, mention, semtypes, type);
	    concs.add(conc);
	    annotations.put(sp, concs);
	    // }
	}
	// }
	// }
	/*
	 * for (SpanList sp: annotations.keySet()) { LinkedHashSet<Ontology> concs =
	 * annotations.get(sp); for (Ontology c: concs) { //
	 * System.out.println(sp.toString() + "\t" + c.toString()); Concept con =
	 * (Concept) c; System.out.println(sp.toString() + "\t" + con.getName() + "\t" +
	 * con.getSemtypes().toString()); } }
	 */
	return annotations;

	/*
	 * remove out file
	 */
	/*
	 * if((!properties.containsKey("DeleteTmp")) ||
	 * properties.getProperty("DeleteTmp").toLowerCase().equals("true")) { String
	 * path="tmp"; File file = new File(path); File[] files = file.listFiles(); for
	 * (File ftmp:files) { if (ftmp.isFile() && ftmp.exists()) {
	 * if(ftmp.toString().matches(tempOutFileName+".*")) { ftmp.delete(); } } } }
	 */

    }

    public static void main(String[] args) throws IOException, XMLStreamException {
	System.setProperty("java.util.logging.config.file", "logging.properties");
	String in = args[0];
	String out = args[1];
	File inFile = new File(in);
	File outFile = new File(out);
	if (inFile.isDirectory()) {
	    if (outFile.exists() == false) {
		outFile.mkdir();
	    }
	    List<String> files = FileUtils.listFiles(in, false, "txt");
	    int fileNum = 0;
	    for (String f : files) {
		String id = f.substring(f.lastIndexOf(File.separator) + 1).replace(".txt", "");
		log.log(Level.INFO, "Processing {0}: {1}.", new Object[] { id, ++fileNum });
		String outfile = outFile.getAbsolutePath() + File.separator + id + ".out";
		String text = FileUtils.stringFromFileWithBytes(f, "UTF8");
		Map<SpanList, LinkedHashSet<Ontology>> annotations = GNormPlusStringWrapper.getInstance("setup.txt")
			.annotateText(text);

		for (SpanList sp : annotations.keySet()) {
		    LinkedHashSet<Ontology> concs = annotations.get(sp);
		    for (Ontology c : concs) {
			Concept con = (Concept) c;
			System.out.println(sp.toString() + "\t" + con.getName() + "\t" + con.getSemtypes().toString());
		    }
		}
	    }
	} else {
	    if (outFile.exists() == false) {
		String text = FileUtils.stringFromFileWithBytes(in, "UTF8");
		// Map<SpanList,LinkedHashSet<Ontology>> anns = new HashMap<>();
		Map<SpanList, LinkedHashSet<Ontology>> annotations = GNormPlusStringWrapper.getInstance("setup.txt")
			.annotateText(text);
		// GNormPlusWrapper.getInstance("setup.txt").annotateText(text,anns);
		for (SpanList sp : annotations.keySet()) {
		    LinkedHashSet<Ontology> concs = annotations.get(sp);
		    for (Ontology c : concs) {
			Concept con = (Concept) c;
			System.out.println(sp.toString() + "\t" + con.getName() + "\t" + con.getSemtypes().toString());
		    }
		}
	    }
	}
    }
}
