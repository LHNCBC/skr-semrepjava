/**
 * Project: GNormPlus
 * Function: SimConcept : Simplify Composite mentions
 */

package gov.nih.nlm.ner.gnormplus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

// import GNormPluslib.BioCDoc;

public class SimConceptString {
    /*
     * Feature Extraction
     */
    public void FeatureExtraction_Train(BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus, String FilenameData)
	    throws IOException, XMLStreamException {

	try {
	    HashMap<String, String> AdditionCorpus = new HashMap<String, String>();
	    /** output files */
	    BufferedWriter FileData = new BufferedWriter(new FileWriter(FilenameData)); // .data
	    // NLP modules
	    SnowballStemmer stemmer = new englishStemmer();
	    /** PMIDs : i */
	    for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) {
		String Pmid = BioCDocobj.PMIDs.get(i);

		/** Paragraphs : j */
		for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) {

		    ArrayList<String> Annotation = BioCDocobj.Annotations.get(i).get(j);
		    /**
		     * Annotations : k 0 start 1 last 2 mention 3 type 4 id
		     */
		    int Inital_Annotation_size = Annotation.size();
		    for (int k = 0; k < Annotation.size(); k++) // k :
								// Annotations
		    {
			String anno[] = Annotation.get(k).split("\\t");
			int MentionStart = Integer.parseInt(anno[0]);
			int MentionLast = Integer.parseInt(anno[1]);
			String Mention = anno[2];
			String Type = anno[3];
			String ID = anno[4];

			String TokenSTR = Mention;
			TokenSTR = TokenSTR.replaceAll("([0-9])([A-Za-z])", "$1 $2");
			TokenSTR = TokenSTR.replaceAll("([A-Za-z])([0-9])", "$1 $2");
			TokenSTR = TokenSTR.replaceAll("([A-Z])([a-z])", "$1 $2");
			TokenSTR = TokenSTR.replaceAll("([a-z])([A-Z])", "$1 $2");
			TokenSTR = TokenSTR.replaceAll("([\\W])", " $1 ");
			TokenSTR = TokenSTR.replaceAll("[ ]+", " ");
			TokenSTR = TokenSTR.replaceAll("^[ ]+", "");

			/*
			 * Only for Gene
			 */
			if (ID.equals("ASJAS") && k < Inital_Annotation_size) {
			    Pattern ptmp = Pattern.compile("^([^ ][^ ][^ ]+) ([^ ]+) ([^\\W\\-\\_]+) ([^ ]+) ([^ ]+)$");
			    Matcher mtmp = ptmp.matcher(TokenSTR.toLowerCase());
			    if (mtmp.find()) {
				String t1 = mtmp.group(1);
				String t2 = mtmp.group(2);
				String t3 = mtmp.group(3);
				String t4 = mtmp.group(4);
				String t5 = mtmp.group(5);
				String tmp_ment = t1 + " " + t2 + " " + t3 + " - " + t5;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tASCOS");
				tmp_ment = t1 + " " + t2 + " " + t3 + " " + t4.substring(0, 1) + " " + t5;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tASCSS");
				tmp_ment = t1 + " " + t2 + " / " + t5;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tASCS");
				tmp_ment = t1 + " " + t2 + " or - " + t5;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tASCOS");
				tmp_ment = t1 + " I, II, and III";
				Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					+ "\tASCSCCS");
				tmp_ment = t1 + " B, D, and F";
				Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					+ "\tASCSCCS");
				tmp_ment = t1 + " A, B, C, D, E, and F";
				Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					+ "\tASCSCSCSCSCCS");
				tmp_ment = "B and G " + t1;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tSCSA");
				tmp_ment = "A/E " + t1;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tSCSA");
				tmp_ment = "alpha/beta " + t1;
				Annotation.add(
					MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type + "\tSCSA");
				if (t5.matches("[0-9]") && t2.matches("[0-9]")
					&& Integer.parseInt(t5) > Integer.parseInt(t2)) {
				    tmp_ment = t1 + " " + t2 + " to " + t5;
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tASNS");
				    tmp_ment = t1 + " " + t2 + " to -" + t5;
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tASNOS");
				    tmp_ment = t1 + " -" + t2 + " to -" + t5;
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tAASNOS");
				    tmp_ment = t1 + " " + t2 + " to " + t1 + " " + t5;
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tASNAS");
				    tmp_ment = t1 + " " + t2 + "-" + t5;
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tASNS");
				    tmp_ment = t1 + " " + t2 + ", " + t5 + ", and " + (Integer.parseInt(t5) + 2);
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tASCSCCS");
				    tmp_ment = t1 + " -" + t2 + ", -" + t5 + ", and -" + (Integer.parseInt(t5) + 2);
				    Annotation.add(MentionStart + "\t" + MentionLast + "\t" + tmp_ment + "\t" + Type
					    + "\tAASC0SCC0S");
				}
			    }
			}

			String Mention_tmp = Mention;
			String tokens[] = TokenSTR.split(" ");

			// For Repeat
			HashMap<String, Integer> Token2Num = new HashMap<String, Integer>();
			for (int p = 0; p < tokens.length; p++) {
			    if (!Token2Num.containsKey(tokens[p])) {
				Token2Num.put(tokens[p], 0);
			    }
			    Token2Num.put(tokens[p], Token2Num.get(tokens[p]) + 1);
			}

			// For Abbreviation
			HashMap<Integer, String> AbbLFStatus_hash = new HashMap<Integer, String>();
			for (String Pmid_LF : gNormPlus.getPmidLF2Abb_hash().keySet()) {
			    String pf[] = Pmid_LF.split("\\t");
			    if (pf[0].equals(Pmid)) {
				String Abb = gNormPlus.getPmidLF2Abb_hash().get(Pmid_LF);
				String LF = pf[1];

				Abb = Abb.replaceAll("([0-9])([A-Za-z])", "$1 $2");
				Abb = Abb.replaceAll("([A-Za-z])([0-9])", "$1 $2");
				Abb = Abb.replaceAll("([A-Z])([a-z])", "$1 $2");
				Abb = Abb.replaceAll("([a-z])([A-Z])", "$1 $2");
				Abb = Abb.replaceAll("([\\W])", " $1 ");
				Abb = Abb.replaceAll("[ ]+", " ");
				Abb = Abb.replaceAll("^[ ]+", "");

				LF = LF.replaceAll("([0-9])([A-Za-z])", "$1 $2");
				LF = LF.replaceAll("([A-Za-z])([0-9])", "$1 $2");
				LF = LF.replaceAll("([A-Z])([a-z])", "$1 $2");
				LF = LF.replaceAll("([a-z])([A-Z])", "$1 $2");
				LF = LF.replaceAll("([\\W])", " $1 ");
				LF = LF.replaceAll("[ ]+", " ");
				LF = LF.replaceAll("^[ ]+", "");

				Abb = Abb.replaceAll("([^A-Za-z0-9@ ])", "\\\\$1");
				LF = LF.replaceAll("([^A-Za-z0-9@ ])", "\\\\$1");
				Abb = Abb.toLowerCase();
				LF = LF.toLowerCase();
				Pattern ptmp1 = Pattern.compile("(.*)(" + LF + ")([ ]*\\([ ]*)(" + Abb + ")[ ]*\\).*");
				Matcher mtmp1 = ptmp1.matcher(TokenSTR.toLowerCase());
				Pattern ptmp2 = Pattern.compile("(.*)(" + Abb + ")([ ]*\\([ ]*)(" + LF + ")[ ]*\\).*");
				Matcher mtmp2 = ptmp2.matcher(TokenSTR.toLowerCase());
				int start_LF = 0;
				int last_LF = 0;
				int start_Abb = 0;
				int last_Abb = 0;
				if (mtmp1.find()) {
				    start_LF = mtmp1.group(1).length();
				    last_LF = start_LF + mtmp1.group(2).length();
				    start_Abb = last_LF + mtmp1.group(3).length();
				    last_Abb = start_Abb + mtmp1.group(4).length();
				} else if (mtmp2.find()) {
				    start_Abb = mtmp2.group(1).length();
				    last_Abb = start_LF + mtmp2.group(2).length();
				    start_LF = last_LF + mtmp2.group(3).length();
				    last_LF = start_Abb + mtmp2.group(4).length();
				}
				for (int l = start_LF; l < last_LF; l++) {
				    AbbLFStatus_hash.put(l, "FullName");
				}
				for (int l = start_Abb; l < last_Abb; l++) {
				    AbbLFStatus_hash.put(l, "Abbreviation");
				}
			    }
			}

			int Offset = 0;
			for (int p = 0; p < tokens.length; p++) // p : toknes
			{
			    // white space
			    String WSB = "WSB:NoGap"; // no gap to backward
			    String WSF = "WSF:NoGap"; // no gap to forward
			    // while(Mention_tmp.substring(0,1).equals(" "))
			    // {
			    // Mention_tmp=Mention_tmp.substring(1);
			    // WSB="WSB:Gap";
			    // }
			    if (Mention_tmp.length() > tokens[p].length()
				    && Mention_tmp.substring(tokens[p].length(), tokens[p].length() + 1).equals(" ")) {
				WSF = "WSF:Gap";
			    }
			    if (p == 0) {
				WSB = "WSB:1st";
			    } else if (p == tokens.length - 1) {
				WSF = "WSF:last";
			    }

			    // stemming
			    stemmer.setCurrent(tokens[p].toLowerCase());
			    stemmer.stem();
			    String stem = stemmer.getCurrent();

			    // Number of Numbers [0-9]
			    String Num_num = "";
			    String tmp = tokens[p];
			    tmp = tmp.replaceAll("[^0-9]", "");
			    if (tmp.length() > 3) {
				Num_num = "N:4+";
			    } else {
				Num_num = "N:" + tmp.length();
			    }

			    // Number of Uppercase [A-Z]
			    String Num_Uc = "";
			    tmp = tokens[p];
			    tmp = tmp.replaceAll("[^A-Z]", "");
			    if (tmp.length() > 3) {
				Num_Uc = "U:4+";
			    } else {
				Num_Uc = "U:" + tmp.length();
			    }

			    // Number of Lowercase [a-z]
			    String Num_lc = "";
			    tmp = tokens[p];
			    tmp = tmp.replaceAll("[^a-z]", "");
			    if (tmp.length() > 3) {
				Num_lc = "L:4+";
			    } else {
				Num_lc = "L:" + tmp.length();
			    }

			    // Number of ALL char
			    String Num_All = "";
			    if (tokens[p].length() > 3) {
				Num_All = "A:4+";
			    } else {
				Num_All = "A:" + tokens[p].length();
			    }

			    // specific character (;:,.->+_)
			    String SpecificC = "__nil__";
			    if (tokens[p].equals(";") || tokens[p].equals(":") || tokens[p].equals(",")
				    || tokens[p].equals(".") || tokens[p].equals("-") || tokens[p].equals(">")
				    || tokens[p].equals("+") || tokens[p].equals("_")) {
				SpecificC = "-SpecificC1-";
			    } else if (tokens[p].equals("(") || tokens[p].equals(")")) {
				SpecificC = "-SpecificC2-";
			    } else if (tokens[p].equals("{") || tokens[p].equals("}")) {
				SpecificC = "-SpecificC3-";
			    } else if (tokens[p].equals("[") || tokens[p].equals("]")) {
				SpecificC = "-SpecificC4-";
			    } else if (tokens[p].equals("\\") || tokens[p].equals("/")) {
				SpecificC = "-SpecificC5-";
			    }

			    // Chemical Prefix/Suffix
			    String ChemPreSuf = "__nil__";
			    if (tokens[p].matches(".*(yl|ylidyne|oyl|sulfonyl)")) {
				ChemPreSuf = "-CHEMinlineSuffix-";
			    } else if (tokens[p].matches("(meth|eth|prop|tetracos).*")) {
				ChemPreSuf = "-CHEMalkaneStem-";
			    } else if (tokens[p].matches("(di|tri|tetra).*")) {
				ChemPreSuf = "-CHEMsimpleMultiplier-";
			    } else if (tokens[p].matches("(benzen|pyridin|toluen).*")) {
				ChemPreSuf = "-CHEMtrivialRing-";
			    } else if (tokens[p].matches(
				    ".*(one|ol|carboxylic|amide|ate|acid|ium|ylium|ide|uide|iran|olan|inan|pyrid|acrid|amid|keten|formazan|fydrazin)(s|)")) {
				ChemPreSuf = "-CHEMsuffix-";
			    }

			    // MentionType
			    String MentionType = "__nil__";
			    if (gNormPlus.getSimConceptMention2Type_hash().containsKey(tokens[p])) {
				MentionType = "-" + gNormPlus.getSimConceptMention2Type_hash().get(tokens[p]) + "-";
			    }

			    // Protein symbols
			    String ProteinSym = "__nil__";
			    if (tokens[p].matches(
				    ".*(glutamine|glutamic|leucine|valine|isoleucine|lysine|alanine|glycine|aspartate|methionine|threonine|histidine|aspartic|asparticacid|arginine|asparagine|tryptophan|proline|phenylalanine|cysteine|serine|glutamate|tyrosine|stop|frameshift).*")) {
				ChemPreSuf = "-ProteinSymFull-";
			    } else if (tokens[p].matches(
				    "(cys|ile|ser|gln|met|asn|pro|lys|asp|thr|phe|ala|gly|his|leu|arg|trp|val|glu|tyr|fs|fsx)")) {
				ChemPreSuf = "-ProteinSymTri-";
			    } else if (tokens[p].matches("[CISQMNPKDTFAGHLRWVEYX]")) {
				ChemPreSuf = "-ProteinSymChar-";
			    }

			    // Repeat
			    String Repeat = "__nil__";
			    if (Token2Num.get(tokens[p]) > 1 && tokens[p].length() > 1 && (!tokens[p].matches(
				    "([\\W\\-\\_0-9]+|and|or|alpha|beta|gamma|theta|zeta|delta|kappa|II|VI|IV|III)"))) {
				Repeat = "-Repeat-";
			    }

			    // Patterns
			    String Pattern1 = tokens[p];
			    if (Pattern1.matches(".*[\\W\\-\\_].*")) {
				Pattern1 = "__nil__";
			    } else {
				Pattern1 = Pattern1.replaceAll("[A-Z]", "A");
				Pattern1 = Pattern1.replaceAll("[a-z]", "a");
				Pattern1 = Pattern1.replaceAll("[0-9]", "0");
				Pattern1 = "P1:" + Pattern1;
			    }
			    String Pattern2 = tokens[p];
			    if (Pattern2.matches(".*[\\W\\-\\_].*")) {
				Pattern2 = "__nil__";
			    } else {
				Pattern2 = Pattern2.replaceAll("[A-Za-z]", "a");
				Pattern2 = Pattern2.replaceAll("[0-9]", "0");
				Pattern2 = "P2:" + Pattern2;
			    }
			    String Pattern3 = tokens[p];
			    if (Pattern3.matches(".*[\\W\\-\\_].*")) {
				Pattern3 = "__nil__";
			    } else {
				Pattern3 = Pattern3.replaceAll("[A-Z]+", "A");
				Pattern3 = Pattern3.replaceAll("[a-z]+", "a");
				Pattern3 = Pattern3.replaceAll("[0-9]+", "0");
				Pattern3 = "P3:" + Pattern3;
			    }
			    String Pattern4 = tokens[p];
			    if (Pattern4.matches(".*[\\W\\-\\_].*")) {
				Pattern4 = "__nil__";
			    } else {
				Pattern4 = Pattern4.replaceAll("[A-Za-z]+", "a");
				Pattern4 = Pattern4.replaceAll("[0-9]+", "0");
				Pattern4 = "P4:" + Pattern4;
			    }

			    // prefix
			    String prefix = "";
			    tmp = tokens[p];
			    if (tmp.length() >= 1) {
				prefix = tmp.substring(0, 1);
			    } else {
				prefix = "__nil__";
			    }
			    if (tmp.length() >= 2) {
				prefix = prefix + " " + tmp.substring(0, 2);
			    } else {
				prefix = prefix + " __nil__";
			    }
			    if (tmp.length() >= 3) {
				prefix = prefix + " " + tmp.substring(0, 3);
			    } else {
				prefix = prefix + " __nil__";
			    }
			    if (tmp.length() >= 4) {
				prefix = prefix + " " + tmp.substring(0, 4);
			    } else {
				prefix = prefix + " __nil__";
			    }
			    if (tmp.length() >= 5) {
				prefix = prefix + " " + tmp.substring(0, 5);
			    } else {
				prefix = prefix + " __nil__";
			    }

			    // suffix
			    String suffix = "";
			    tmp = tokens[p];
			    if (tmp.length() >= 1) {
				suffix = tmp.substring(tmp.length() - 1, tmp.length());
			    } else {
				suffix = "__nil__";
			    }
			    if (tmp.length() >= 2) {
				suffix = suffix + " " + tmp.substring(tmp.length() - 2, tmp.length());
			    } else {
				suffix = suffix + " __nil__";
			    }
			    if (tmp.length() >= 3) {
				suffix = suffix + " " + tmp.substring(tmp.length() - 3, tmp.length());
			    } else {
				suffix = suffix + " __nil__";
			    }
			    if (tmp.length() >= 4) {
				suffix = suffix + " " + tmp.substring(tmp.length() - 4, tmp.length());
			    } else {
				suffix = suffix + " __nil__";
			    }
			    if (tmp.length() >= 5) {
				suffix = suffix + " " + tmp.substring(tmp.length() - 5, tmp.length());
			    } else {
				suffix = suffix + " __nil__";
			    }

			    // Abbreviation & Long Form
			    String AbbLF = "__nil__";
			    if (AbbLFStatus_hash.containsKey(Offset)) {
				AbbLF = AbbLFStatus_hash.get(Offset);
			    }

			    String Status = ID.substring(p, p + 1);
			    FileData.write(tokens[p] + " " + WSB + " " + WSF + " " + stem + " " + Num_num + " "
				    + Num_num + " " + Num_Uc + " " + Num_lc + " " + Num_All + " " + SpecificC + " "
				    + ChemPreSuf + " " + MentionType + " " + ProteinSym + " " + Repeat + " " + Pattern1
				    + " " + Pattern2 + " " + Pattern3 + " " + Pattern4 + " " + prefix + " " + suffix
				    + " " + AbbLF + " " + Status + "\n");
			    Offset = Offset + tokens[p].length() + 1;
			}
			FileData.write("\n");
		    }

		}
	    }
	    FileData.close();
	} catch (IOException e1) {
	    System.out.println("[MR]: Input file is not exist.");
	}
    }

    // public void FeatureExtraction_Test(BioCDoc BioCDocobj, GNormPlusStringWrapper gNormPlus, String FilenameData)
    public DataFormat FeatureExtraction_Test(BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus) {

	// try {
	/** output files */
	// BufferedWriter FileData = new BufferedWriter(new FileWriter(FilenameData)); // .data
	StringBuilder FileDataSB = new StringBuilder();
	DataFormat df = null;
	// NLP modules
	SnowballStemmer stemmer = new englishStemmer();
	/** PMIDs : i */
	for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) {
	    String Pmid = BioCDocobj.PMIDs.get(i);

	    /** Paragraphs : j */
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) {
		// Added the boundary check for new string version 
		// May 1 2018 Dongwook Shin
		if (j < BioCDocobj.Annotations.get(i).size()) {
		    ArrayList<String> Annotation = BioCDocobj.Annotations.get(i).get(j);
		    /**
		     * Annotations : k 0 start 1 last 2 mention 3 type 4 id
		     */
		    for (int k = 0; k < Annotation.size(); k++) // k :
								// Annotations
		    {
			String anno[] = Annotation.get(k).split("\\t");
			String Mentions = anno[2];
			String Type = anno[3];
			String MentionArr[] = Mentions.split("\\|");
			if (Type.equals("Gene")) {
			    for (int m = 0; m < MentionArr.length; m++) {
				String Mention = MentionArr[m];
				String TokenSTR = Mention;
				TokenSTR = TokenSTR.replaceAll("([0-9])([A-Za-z])", "$1 $2");
				TokenSTR = TokenSTR.replaceAll("([A-Za-z])([0-9])", "$1 $2");
				TokenSTR = TokenSTR.replaceAll("([A-Z])([a-z])", "$1 $2");
				TokenSTR = TokenSTR.replaceAll("([a-z])([A-Z])", "$1 $2");
				TokenSTR = TokenSTR.replaceAll("([\\W])", " $1 ");
				TokenSTR = TokenSTR.replaceAll("[ ]+", " ");
				TokenSTR = TokenSTR.replaceAll("^[ ]+", "");
				String Mention_tmp = Mention;
				String tokens[] = TokenSTR.split(" ");

				// For Repeat
				HashMap<String, Integer> Token2Num = new HashMap<String, Integer>();
				for (int p = 0; p < tokens.length; p++) {
				    if (!Token2Num.containsKey(tokens[p])) {
					Token2Num.put(tokens[p], 0);
				    }
				    Token2Num.put(tokens[p], Token2Num.get(tokens[p]) + 1);
				}

				// For Abbreviation
				HashMap<Integer, String> AbbLFStatus_hash = new HashMap<Integer, String>();
				for (String Pmid_LF : gNormPlus.getPmidLF2Abb_hash().keySet()) {
				    String pf[] = Pmid_LF.split("\\t");
				    if (pf[0].equals(Pmid)) {
					String Abb = gNormPlus.getPmidLF2Abb_hash().get(Pmid_LF);
					String LF = pf[1];

					Abb = Abb.replaceAll("([0-9])([A-Za-z])", "$1 $2");
					Abb = Abb.replaceAll("([A-Za-z])([0-9])", "$1 $2");
					Abb = Abb.replaceAll("([A-Z])([a-z])", "$1 $2");
					Abb = Abb.replaceAll("([a-z])([A-Z])", "$1 $2");
					Abb = Abb.replaceAll("([\\W])", " $1 ");
					Abb = Abb.replaceAll("[ ]+", " ");
					Abb = Abb.replaceAll("^[ ]+", "");

					LF = LF.replaceAll("([0-9])([A-Za-z])", "$1 $2");
					LF = LF.replaceAll("([A-Za-z])([0-9])", "$1 $2");
					LF = LF.replaceAll("([A-Z])([a-z])", "$1 $2");
					LF = LF.replaceAll("([a-z])([A-Z])", "$1 $2");
					LF = LF.replaceAll("([\\W])", " $1 ");
					LF = LF.replaceAll("[ ]+", " ");
					LF = LF.replaceAll("^[ ]+", "");

					Abb = Abb.replaceAll("([^A-Za-z0-9@ ])", "\\\\$1");
					LF = LF.replaceAll("([^A-Za-z0-9@ ])", "\\\\$1");
					Abb = Abb.toLowerCase();
					LF = LF.toLowerCase();
					Pattern ptmp1 = Pattern.compile("(.*)" + "(" + LF + ")" + "([ ]*\\([ ]*)" + "("
						+ Abb + ")" + "[ ]*\\).*");
					Matcher mtmp1 = ptmp1.matcher(TokenSTR.toLowerCase());
					Pattern ptmp2 = Pattern.compile("(.*)" + "(" + Abb + ")" + "([ ]*\\([ ]*)" + "("
						+ LF + ")" + "[ ]*\\).*");
					Matcher mtmp2 = ptmp2.matcher(TokenSTR.toLowerCase());
					int start_LF = 0;
					int last_LF = 0;
					int start_Abb = 0;
					int last_Abb = 0;
					if (mtmp1.find()) {
					    start_LF = mtmp1.group(1).length();
					    last_LF = start_LF + mtmp1.group(2).length();
					    start_Abb = last_LF + mtmp1.group(3).length();
					    last_Abb = start_Abb + mtmp1.group(4).length();
					} else if (mtmp2.find()) {
					    start_Abb = mtmp2.group(1).length();
					    last_Abb = start_LF + mtmp2.group(2).length();
					    start_LF = last_LF + mtmp2.group(3).length();
					    last_LF = start_Abb + mtmp2.group(4).length();
					}
					for (int l = start_LF; l < last_LF; l++) {
					    AbbLFStatus_hash.put(l, "FullName");
					}
					for (int l = start_Abb; l < last_Abb; l++) {
					    AbbLFStatus_hash.put(l, "Abbreviation");
					}
				    }
				}

				int Offset = 0;
				for (int p = 0; p < tokens.length; p++) // p :
									// toknes
				{
				    // white space
				    String WSB = "WSB:NoGap"; // no gap to
							      // backward
				    String WSF = "WSF:NoGap"; // no gap to
							      // forward
				    if (Mention_tmp.length() > 0) {
					while (Mention_tmp.substring(0, 1).equals(" ")) {
					    Mention_tmp = Mention_tmp.substring(1);
					    WSB = "WSB:Gap";
					}
				    }
				    if (Mention_tmp.length() > tokens[p].length() && Mention_tmp
					    .substring(tokens[p].length(), tokens[p].length() + 1).equals(" ")) {
					WSF = "WSF:Gap";
				    }
				    if (p == 0) {
					WSB = "WSB:1st";
				    } else if (p == tokens.length - 1) {
					WSF = "WSF:last";
				    }

				    // stemming
				    stemmer.setCurrent(tokens[p].toLowerCase());
				    stemmer.stem();
				    String stem = stemmer.getCurrent();

				    // Number of Numbers [0-9]
				    String Num_num = "";
				    String tmp = tokens[p];
				    tmp = tmp.replaceAll("[^0-9]", "");
				    if (tmp.length() > 3) {
					Num_num = "N:4+";
				    } else {
					Num_num = "N:" + tmp.length();
				    }

				    // Number of Uppercase [A-Z]
				    String Num_Uc = "";
				    tmp = tokens[p];
				    tmp = tmp.replaceAll("[^A-Z]", "");
				    if (tmp.length() > 3) {
					Num_Uc = "U:4+";
				    } else {
					Num_Uc = "U:" + tmp.length();
				    }

				    // Number of Lowercase [a-z]
				    String Num_lc = "";
				    tmp = tokens[p];
				    tmp = tmp.replaceAll("[^a-z]", "");
				    if (tmp.length() > 3) {
					Num_lc = "L:4+";
				    } else {
					Num_lc = "L:" + tmp.length();
				    }

				    // Number of ALL char
				    String Num_All = "";
				    if (tokens[p].length() > 3) {
					Num_All = "A:4+";
				    } else {
					Num_All = "A:" + tokens[p].length();
				    }

				    // specific character (;:,.->+_)
				    String SpecificC = "__nil__";
				    if (tokens[p].equals(";") || tokens[p].equals(":") || tokens[p].equals(",")
					    || tokens[p].equals(".") || tokens[p].equals("-") || tokens[p].equals(">")
					    || tokens[p].equals("+") || tokens[p].equals("_")) {
					SpecificC = "-SpecificC1-";
				    } else if (tokens[p].equals("(") || tokens[p].equals(")")) {
					SpecificC = "-SpecificC2-";
				    } else if (tokens[p].equals("{") || tokens[p].equals("}")) {
					SpecificC = "-SpecificC3-";
				    } else if (tokens[p].equals("[") || tokens[p].equals("]")) {
					SpecificC = "-SpecificC4-";
				    } else if (tokens[p].equals("\\") || tokens[p].equals("/")) {
					SpecificC = "-SpecificC5-";
				    }

				    // Chemical Prefix/Suffix
				    String ChemPreSuf = "__nil__";
				    if (tokens[p].matches(".*(yl|ylidyne|oyl|sulfonyl)")) {
					ChemPreSuf = "-CHEMinlineSuffix-";
				    } else if (tokens[p].matches("(meth|eth|prop|tetracos).*")) {
					ChemPreSuf = "-CHEMalkaneStem-";
				    } else if (tokens[p].matches("(di|tri|tetra).*")) {
					ChemPreSuf = "-CHEMsimpleMultiplier-";
				    } else if (tokens[p].matches("(benzen|pyridin|toluen).*")) {
					ChemPreSuf = "-CHEMtrivialRing-";
				    } else if (tokens[p].matches(
					    ".*(one|ol|carboxylic|amide|ate|acid|ium|ylium|ide|uide|iran|olan|inan|pyrid|acrid|amid|keten|formazan|fydrazin)(s|)")) {
					ChemPreSuf = "-CHEMsuffix-";
				    }

				    // MentionType
				    String MentionType = "__nil__";
				    if (gNormPlus.getSimConceptMention2Type_hash().containsKey(tokens[p])) {
					MentionType = "-" + gNormPlus.getSimConceptMention2Type_hash().get(tokens[p])
						+ "-";
				    }

				    // Protein symbols
				    String ProteinSym = "__nil__";
				    if (tokens[p].matches(
					    ".*(glutamine|glutamic|leucine|valine|isoleucine|lysine|alanine|glycine|aspartate|methionine|threonine|histidine|aspartic|asparticacid|arginine|asparagine|tryptophan|proline|phenylalanine|cysteine|serine|glutamate|tyrosine|stop|frameshift).*")) {
					ChemPreSuf = "-ProteinSymFull-";
				    } else if (tokens[p].matches(
					    "(cys|ile|ser|gln|met|asn|pro|lys|asp|thr|phe|ala|gly|his|leu|arg|trp|val|glu|tyr|fs|fsx)")) {
					ChemPreSuf = "-ProteinSymTri-";
				    } else if (tokens[p].matches("[CISQMNPKDTFAGHLRWVEYX]")) {
					ChemPreSuf = "-ProteinSymChar-";
				    }

				    // Repeat
				    String Repeat = "__nil__";
				    if (Token2Num.get(tokens[p]) > 1 && tokens[p].length() > 1 && (!tokens[p].matches(
					    "([\\W\\-\\_0-9]+|and|or|alpha|beta|gamma|theta|zeta|delta|kappa|II|VI|IV|III)"))) {
					Repeat = "-Repeat-";
				    }

				    // Patterns
				    String Pattern1 = tokens[p];
				    if (Pattern1.matches(".*[\\W\\-\\_].*")) {
					Pattern1 = "__nil__";
				    } else {
					Pattern1 = Pattern1.replaceAll("[A-Z]", "A");
					Pattern1 = Pattern1.replaceAll("[a-z]", "a");
					Pattern1 = Pattern1.replaceAll("[0-9]", "0");
					Pattern1 = "P1:" + Pattern1;
				    }
				    String Pattern2 = tokens[p];
				    if (Pattern2.matches(".*[\\W\\-\\_].*")) {
					Pattern2 = "__nil__";
				    } else {
					Pattern2 = Pattern2.replaceAll("[A-Za-z]", "a");
					Pattern2 = Pattern2.replaceAll("[0-9]", "0");
					Pattern2 = "P2:" + Pattern2;
				    }
				    String Pattern3 = tokens[p];
				    if (Pattern3.matches(".*[\\W\\-\\_].*")) {
					Pattern3 = "__nil__";
				    } else {
					Pattern3 = Pattern3.replaceAll("[A-Z]+", "A");
					Pattern3 = Pattern3.replaceAll("[a-z]+", "a");
					Pattern3 = Pattern3.replaceAll("[0-9]+", "0");
					Pattern3 = "P3:" + Pattern3;
				    }
				    String Pattern4 = tokens[p];
				    if (Pattern4.matches(".*[\\W\\-\\_].*")) {
					Pattern4 = "__nil__";
				    } else {
					Pattern4 = Pattern4.replaceAll("[A-Za-z]+", "a");
					Pattern4 = Pattern4.replaceAll("[0-9]+", "0");
					Pattern4 = "P4:" + Pattern4;
				    }

				    // prefix
				    String prefix = "";
				    tmp = tokens[p];
				    if (tmp.length() >= 1) {
					prefix = tmp.substring(0, 1);
				    } else {
					prefix = "__nil__";
				    }
				    if (tmp.length() >= 2) {
					prefix = prefix + " " + tmp.substring(0, 2);
				    } else {
					prefix = prefix + " __nil__";
				    }
				    if (tmp.length() >= 3) {
					prefix = prefix + " " + tmp.substring(0, 3);
				    } else {
					prefix = prefix + " __nil__";
				    }
				    if (tmp.length() >= 4) {
					prefix = prefix + " " + tmp.substring(0, 4);
				    } else {
					prefix = prefix + " __nil__";
				    }
				    if (tmp.length() >= 5) {
					prefix = prefix + " " + tmp.substring(0, 5);
				    } else {
					prefix = prefix + " __nil__";
				    }

				    // suffix
				    String suffix = "";
				    tmp = tokens[p];
				    if (tmp.length() >= 1) {
					suffix = tmp.substring(tmp.length() - 1, tmp.length());
				    } else {
					suffix = "__nil__";
				    }
				    if (tmp.length() >= 2) {
					suffix = suffix + " " + tmp.substring(tmp.length() - 2, tmp.length());
				    } else {
					suffix = suffix + " __nil__";
				    }
				    if (tmp.length() >= 3) {
					suffix = suffix + " " + tmp.substring(tmp.length() - 3, tmp.length());
				    } else {
					suffix = suffix + " __nil__";
				    }
				    if (tmp.length() >= 4) {
					suffix = suffix + " " + tmp.substring(tmp.length() - 4, tmp.length());
				    } else {
					suffix = suffix + " __nil__";
				    }
				    if (tmp.length() >= 5) {
					suffix = suffix + " " + tmp.substring(tmp.length() - 5, tmp.length());
				    } else {
					suffix = suffix + " __nil__";
				    }

				    // Abbreviation & Long Form
				    String AbbLF = "__nil__";
				    if (AbbLFStatus_hash.containsKey(Offset)) {
					AbbLF = AbbLFStatus_hash.get(Offset);
				    }

				    FileDataSB.append(tokens[p] + " " + WSB + " " + WSF + " " + stem + " " + Num_num
					    + " " + Num_num + " " + Num_Uc + " " + Num_lc + " " + Num_All + " "
					    + SpecificC + " " + ChemPreSuf + " " + MentionType + " " + ProteinSym + " "
					    + Repeat + " " + Pattern1 + " " + Pattern2 + " " + Pattern3 + " " + Pattern4
					    + " " + prefix + " " + suffix + " " + AbbLF + "\n");
				    Offset = Offset + tokens[p].length() + 1;
				}
				FileDataSB.append("\n");
			    }
			}
		    }
		}

	    }
	}

	df = new DataFormat(FileDataSB.toString(), null);
	return df;
	// FileData.close();
	// } catch (IOException e1) {
	//	System.out.println("[MR]: Input file is not exist.");
	// }
    }

    public String CRF_test(String model, String dataString) throws IOException {
	// File f = new File(FilenameOutput);
	// BufferedWriter fr = new BufferedWriter(new FileWriter(f));

	CRFTestJNI crfjni = new CRFTestJNI();
	String outputdata = crfjni.crftest(0, model, dataString);
	return outputdata;

	// String cmd = "CRF/crf_test -m " + model + " -o " + FilenameOutput + " " + FilenameData;

    }

    public void CRF_learn(String model, String FilenameData) throws IOException {
	Runtime runtime = Runtime.getRuntime();

	Process process = null;
	String line = null;
	InputStream is = null;
	InputStreamReader isr = null;
	BufferedReader br = null;
	String cmd = "CRF/crf_learn -f 3 -c 4.0 CRF/template_SimConcept " + FilenameData + " " + model;

	try {
	    process = runtime.exec(cmd);
	    is = process.getInputStream();
	    isr = new InputStreamReader(is);
	    br = new BufferedReader(isr);
	    while ((line = br.readLine()) != null) {
		System.out.println(line);
		System.out.flush();
	    }
	    is.close();
	    isr.close();
	    br.close();
	} catch (IOException e) {
	    System.out.println(e);
	    runtime.exit(0);
	}
    }

    public void ReadCRFresult(BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus, String outputString)
	    throws XMLStreamException, IOException {

	/** load CRF output */
	// ArrayList<String> outputArr1 = new ArrayList<String>();
	String[] outputArr1 = outputString.split(System.getProperty("line.separator"));
	/*
	 * BufferedReader inputfile = new BufferedReader(new
	 * FileReader(FilenameOutput)); String line; while ((line =
	 * inputfile.readLine()) != null) { outputArr1.add(line); }
	 * inputfile.close();
	 */

	/**
	 * Recognize the mentions which can be simplified
	 */
	int Count_mention = 0;
	boolean Simplified = false;
	String Mention = "";
	String Mention_NoSpace = "";
	String States = "";
	HashMap<String, String> Mentions_hash = new HashMap<String, String>();
	HashMap<String, String> States_hash = new HashMap<String, String>();
	HashMap<String, String> Output_Split_mention_Ind = new HashMap<String, String>();
	HashMap<String, String> Output_Split_mention = new HashMap<String, String>();
	// for (int i = 0; i < outputArr1.size(); i++) {
	for (int i = 0; i < outputArr1.length; i++) {
	    // String outputsRow[] = outputArr1.get(i).split("\\t");
	    String outputsRow[] = outputArr1[i].split("\\t");
	    String Token = outputsRow[0]; // 1st column: Token
	    String TknState = outputsRow[outputsRow.length - 1]; // last column:
								 // state
	    if (TknState.matches("[CJBEFN]")) // Simplify-able
	    {
		Simplified = true;
	    }

	    if (outputsRow.length <= 1) // the end of a mention
	    {
		if (Simplified == true) {
		    /*
		     * update BF states :
		     * 
		     * AABAFA AWXWOA
		     * 
		     */
		    for (String PmidAbb : gNormPlus.getPmidAbb2LF_hash().keySet()) {
			String PmidAbbs[] = PmidAbb.split("\\t");
			String Abb = PmidAbbs[1];
			String LF = gNormPlus.getPmidAbb2LF_hash().get(PmidAbb);

			Abb = Abb.replaceAll("([0-9])([A-Za-z])", "$1 $2");
			Abb = Abb.replaceAll("([A-Za-z])([0-9])", "$1 $2");
			Abb = Abb.replaceAll("([A-Z])([a-z])", "$1 $2");
			Abb = Abb.replaceAll("([a-z])([A-Z])", "$1 $2");
			Abb = Abb.replaceAll("([\\W])", " $1 ");
			Abb = Abb.replaceAll("[ ]+", " ");
			Abb = Abb.replaceAll("^[ ]+", "");
			LF = LF.replaceAll("([0-9])([A-Za-z])", "$1 $2");
			LF = LF.replaceAll("([A-Za-z])([0-9])", "$1 $2");
			LF = LF.replaceAll("([A-Z])([a-z])", "$1 $2");
			LF = LF.replaceAll("([a-z])([A-Z])", "$1 $2");
			LF = LF.replaceAll("([\\W])", " $1 ");
			LF = LF.replaceAll("[ ]+", " ");
			LF = LF.replaceAll("^[ ]+", "");
			Abb = Abb.replaceAll("([^A-Za-z0-9@ ])", "\\\\$1");
			LF = LF.replaceAll("([^A-Za-z0-9@ ])", "\\\\$1");

			Pattern ptmp1 = Pattern.compile("^(.*)(" + LF + ") \\( (" + Abb + ") \\) (.*)$");
			Matcher mtmp1 = ptmp1.matcher(Mention);
			Pattern ptmp2 = Pattern.compile("^(.*)(" + Abb + ") \\( (" + LF + ") \\) (.*)$");
			Matcher mtmp2 = ptmp2.matcher(Mention);
			if (mtmp1.find()) {
			    int t1_len = 0;
			    if (!mtmp1.group(1).equals("")) {
				t1_len = mtmp1.group(1).split(" ").length;
			    }
			    String t2[] = mtmp1.group(2).split(" ");
			    String t3[] = mtmp1.group(3).split(" ");

			    String N1 = "";
			    String N2 = "";
			    String N3 = "";
			    String N4 = "";
			    N1 = States.substring(0, t1_len);
			    for (int t = 0; t < t2.length; t++) {
				N2 = N2 + "W";
			    }
			    for (int t = 0; t < t3.length; t++) {
				N3 = N3 + "W";
			    }
			    N4 = States.substring(N1.length() + N2.length() + N3.length() + 2);
			    States = N1 + N2 + "X" + N3 + "O" + N4;
			} else if (mtmp2.find()) {
			    int t1_len = 0;
			    if (!mtmp2.group(1).equals("")) {
				t1_len = mtmp2.group(1).split(" ").length;
			    }
			    String t2[] = mtmp2.group(2).split(" ");
			    String t3[] = mtmp2.group(3).split(" ");

			    String N1 = "";
			    String N2 = "";
			    String N3 = "";
			    String N4 = "";
			    N1 = States.substring(0, t1_len);
			    for (int t = 0; t < t2.length; t++) {
				N2 = N2 + "W";
			    }
			    for (int t = 0; t < t3.length; t++) {
				N3 = N3 + "W";
			    }
			    N4 = States.substring(N1.length() + N2.length() + N3.length() + 2);
			    States = N1 + N2 + "X" + N3 + "O" + N4;
			} else {
			    // System.out.println(Mention+"\t"+LF+"\t"+Abb);
			}
		    }
		    States_hash.put(Mention_NoSpace, States);
		    Mentions_hash.put(Mention_NoSpace, Mention);
		}
		// Initial
		Simplified = false;
		Mention = "";
		Mention_NoSpace = "";
		Count_mention++;
		States = "";
	    } else {
		if (Mention.equals("")) {
		    Mention = Token;
		} else {
		    Mention = Mention + " " + Token;
		}
		States = States + TknState;
		Mention_NoSpace = Mention_NoSpace + Token;
	    }
	}
	for (String MNoSpace : Mentions_hash.keySet()) // mention : i
	{
	    ArrayList<String> Split_mention = new ArrayList<String>();
	    ArrayList<String> Split_state = new ArrayList<String>();
	    String tmp_mention = "";
	    String tmp_state = "";
	    /**
	     * count = Mentions_count.get(i) : # of the mention in the corpus
	     * (543) Mentions_hash.get(count) : Original Mention (ORP - 1 to ORP
	     * - 6) States_hash.get(count) : States (AASNOOS)
	     */
	    String TokenArr[] = Mentions_hash.get(MNoSpace).split(" ");
	    String StateArr[] = States_hash.get(MNoSpace).split("");

	    // refinement : isn't used
	    Pattern ptmp1 = Pattern.compile("^([S]+)([CN])([S]+)$");
	    Matcher mtmp1 = ptmp1.matcher(States_hash.get(MNoSpace));
	    if (mtmp1.find()) {
		States_hash.put(MNoSpace, mtmp1.group(1) + "J" + mtmp1.group(3));
	    }

	    // Split BE
	    for (int s = 0; s < StateArr.length; s++) // token/state : s
	    {
		if (StateArr[s].matches("[BE]")) {
		    if (tmp_mention.length() > 0) {
			Split_mention.add(tmp_mention);
			Split_state.add(tmp_state);
		    }
		    tmp_mention = "";
		    tmp_state = "";
		} else // CNBF
		{
		    tmp_mention = tmp_mention + TokenArr[s] + " ";
		    tmp_state = tmp_state + StateArr[s];
		}
	    }
	    if (!tmp_mention.equals("")) {
		Split_mention.add(tmp_mention);
		Split_state.add(tmp_state);
	    }

	    // Split B/F
	    for (int m = 0; m < Split_mention.size(); m++)// m mention
	    {
		String A = "";
		String X = "";
		String STAA = "";
		String strainX = "";
		String STAstrainX = "";
		int X_continous = 0;
		ArrayList<String> strainsX = new ArrayList<String>();
		ArrayList<String> STAstrainsX = new ArrayList<String>();
		String each_token[] = Split_mention.get(m).split(" ");
		String each_state[] = Split_state.get(m).split("");
		for (int s = 0; s < each_state.length; s++) // s token
		{
		    if (each_state[s].matches("[ACNS]")) {
			A = A + each_token[s] + " ";
			STAA = STAA + each_state[s];
		    } else if (each_state[s].equals("W")) {
			A = A + "STRAIN";
			STAA = STAA + "@";
			strainX = strainX + each_token[s] + " ";
			STAstrainX = STAstrainX + each_state[s];
			X_continous = 0;
		    } else if (each_state[s].equals("X") && X_continous == 0) {
			X = each_state[s];
			strainsX.add(strainX);
			STAstrainsX.add(STAstrainX);
			strainX = "";
			STAstrainX = "";
			X_continous++;
		    }
		}
		if (!strainX.equals("")) {
		    strainsX.add(strainX);
		}
		if (!STAstrainX.equals("")) {
		    STAstrainsX.add(STAstrainX);
		}
		A = A.replaceAll("(STRAIN){1,}", "STRAIN");
		STAA = STAA.replaceAll("(@){1,}", "@");

		if (X.equals("X")) {
		    for (int x = 0; x < strainsX.size(); x++) // x token
		    {
			String strain = strainsX.get(x);
			String state = STAstrainsX.get(x);
			String tkns = A;
			strain = strain.replaceAll("\\\\", "\\\\\\\\");
			tkns = tkns.replaceAll("STRAIN", strain);
			tkns = tkns.replaceAll("  ", " ");
			if (tkns.substring(tkns.length() - 1, tkns.length() - 1).equals(" ")) {
			    tkns = tkns.substring(0, tkns.length() - 2);
			}
			String STAAs = STAA;
			STAAs = STAAs.replaceAll("@", state);
			Split_mention.add(tkns);
			Split_state.add(STAAs);
		    }
		    Split_mention.remove(m);
		    Split_state.remove(m);
		}
	    }
	    for (int s = 0; s < Split_state.size(); s++) {
		Split_state.set(s, Split_state.get(s).replaceAll("W", "A"));
	    }
	    // Split J
	    for (int m = 0; m < Split_mention.size(); m++)// m mention
	    {
		String each_token[] = Split_mention.get(m).split(" ");
		String each_state[] = Split_state.get(m).split("");
		String sub_mention = "";
		boolean found_J = false;
		for (int k = 0; k < each_state.length; k++) {
		    if (each_state[k].equals("J")) {
			found_J = true;
			if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
			    Output_Split_mention_Ind.put(MNoSpace,
				    Output_Split_mention_Ind.get(MNoSpace) + "|" + sub_mention);
			} else {
			    Output_Split_mention_Ind.put(MNoSpace, sub_mention);
			}
			sub_mention = "";
		    } else {
			sub_mention = sub_mention + each_token[k] + " ";
		    }
		}
		if (found_J == true) {
		    if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
			Output_Split_mention_Ind.put(MNoSpace,
				Output_Split_mention_Ind.get(MNoSpace) + "|" + sub_mention);
		    } else {
			Output_Split_mention_Ind.put(MNoSpace, sub_mention);
		    }
		}
	    }
	    // Split CN
	    for (int m = 0; m < Split_mention.size(); m++) // s split mention
	    {
		String A = "";
		String strainCN = "";
		int CNO_continous = 0;
		ArrayList<String> strainsCN = new ArrayList<String>();
		String CorN = "";

		String each_token[] = Split_mention.get(m).split(" ");
		String each_state[] = Split_state.get(m).split("");

		boolean A_get = false;
		for (int k = 0; k < each_state.length; k++) {
		    if (each_state[k].equals("A")) {
			A = A + each_token[k] + " ";
		    } else if (each_state[k].equals("S")) {
			if (A.length() >= 4) {
			    A = A.replace("s $", "");
			}
			A = A + "STRAIN";
			strainCN = strainCN + each_token[k] + " ";
			CNO_continous = 0;
		    } else if (each_state[k].matches("[CN]") && CNO_continous == 0) {
			CorN = each_state[k];
			strainsCN.add(strainCN);
			strainCN = "";
			CNO_continous++;
		    } else if (each_state[k].equals("J")) {
			if (!strainCN.equals("")) {
			    strainsCN.add(strainCN);
			}

			A = A.replaceAll("STRAINSTRAIN", "STRAIN");
			A = A.replaceAll("STRAINSTRAIN", "STRAIN");

			ptmp1 = Pattern.compile("^(.+)s (.*)$");
			mtmp1 = ptmp1.matcher(A);
			if (mtmp1.find() && mtmp1.group(1).length() >= 3) {
			    A = mtmp1.group(1) + " " + mtmp1.group(2);
			}

			if (CorN.equals("C")) {
			    for (int x = 0; x < strainsCN.size(); x++) {
				String tmp = A;
				tmp = tmp.replaceAll("STRAIN", strainsCN.get(x));
				tmp = tmp.replaceAll("[ ]+", " ");
				if (tmp.length() > 2
					&& (tmp.substring(tmp.length() - 2, tmp.length() - 2).equals(" "))) {
				    tmp = tmp.substring(0, tmp.length() - 2);
				}
				if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
				    Output_Split_mention_Ind.put(MNoSpace,
					    Output_Split_mention_Ind.get(MNoSpace) + "|" + tmp);
				} else {
				    Output_Split_mention_Ind.put(MNoSpace, tmp);
				}
			    }
			} else if (CorN.equals("N")) {
			    if (strainsCN.contains(0) && strainsCN.contains(1)) {
				String strain1 = strainsCN.get(0).replaceAll(" ", "");
				String strain2 = strainsCN.get(1).replaceAll(" ", "");
				if (strain1.matches("[0-9]+") && strain2.matches("[0-9]+")) {
				    if (Integer.parseInt(strain2) - Integer.parseInt(strain1) <= 20) {
					for (int strCount = Integer.parseInt(strain1); strCount <= Integer
						.parseInt(strain2); strCount++) {
					    String tmp = A;
					    tmp = tmp.replace("STRAIN", Integer.toString(strCount));
					    tmp = tmp.replaceAll("[ ]+", " ");
					    if (tmp.length() > 2
						    && tmp.substring(tmp.length() - 2, tmp.length() - 2).equals(" ")) {
						tmp = tmp.substring(0, tmp.length() - 2);
					    }
					    if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
						Output_Split_mention_Ind.put(MNoSpace,
							Output_Split_mention_Ind.get(MNoSpace) + "|" + tmp);
					    } else {
						Output_Split_mention_Ind.put(MNoSpace, tmp);
					    }
					}
				    }
				} else if (strain1.matches("[A-Z]+ ") && strain2.matches("[A-Z]+ ")) {
				    int strInt1 = strain1.replaceAll(" ", "").charAt(0);
				    int strInt2 = strain2.replaceAll(" ", "").charAt(0);
				    if (strInt2 - strInt1 <= 20) {
					for (int strCount = strInt1; strCount <= strInt2; strCount++) {
					    String tmp = A;
					    tmp = tmp.replace("STRAIN", Integer.toString(strCount));
					    tmp = tmp.replaceAll("[ ]+", " ");
					    if (tmp.length() > 2
						    && tmp.substring(tmp.length() - 2, tmp.length() - 2).equals(" ")) {
						tmp = tmp.substring(0, tmp.length() - 2);
					    }
					    if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
						Output_Split_mention_Ind.put(MNoSpace,
							Output_Split_mention_Ind.get(MNoSpace) + "|" + tmp);
					    } else {
						Output_Split_mention_Ind.put(MNoSpace, tmp);
					    }
					}
				    }
				} else {
				    if (Output_Split_mention.containsKey(MNoSpace)) {
					Output_Split_mention.put(MNoSpace,
						Output_Split_mention.get(MNoSpace) + "|" + Split_mention.get(m));
				    } else {
					Output_Split_mention.put(MNoSpace, Split_mention.get(m));
				    }
				}
			    }
			} else {
			    if (Output_Split_mention.containsKey(MNoSpace)) {
				Output_Split_mention.put(MNoSpace,
					Output_Split_mention.get(MNoSpace) + "|" + Split_mention.get(m));
			    } else {
				Output_Split_mention.put(MNoSpace, Split_mention.get(m));
			    }
			}

			A = "";
			strainCN = "";
			CNO_continous = 0;
			strainsCN = new ArrayList<String>();
			CorN = "";
		    }
		}
		if (!strainCN.equals("")) {
		    strainsCN.add(strainCN);
		}

		A = A.replaceAll("STRAINSTRAIN", "STRAIN");
		A = A.replaceAll("STRAINSTRAIN", "STRAIN");

		ptmp1 = Pattern.compile("^(.+)s (.*)$");
		mtmp1 = ptmp1.matcher(A);
		if (mtmp1.find() && mtmp1.group(1).length() >= 3) {
		    A = mtmp1.group(1) + " " + mtmp1.group(2);
		}

		if (CorN.equals("C")) {
		    for (int x = 0; x < strainsCN.size(); x++) {
			String tmp = A;
			tmp = tmp.replaceAll("STRAIN", strainsCN.get(x));
			tmp = tmp.replaceAll("[ ]+", " ");
			if (tmp.length() > 2 && (tmp.substring(tmp.length() - 2, tmp.length() - 2).equals(" "))) {
			    tmp = tmp.substring(0, tmp.length() - 2);
			}
			if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
			    Output_Split_mention_Ind.put(MNoSpace, Output_Split_mention_Ind.get(MNoSpace) + "|" + tmp);
			} else {
			    Output_Split_mention_Ind.put(MNoSpace, tmp);
			}
		    }
		} else if (CorN.equals("N")) {
		    if (strainsCN.contains(0) && strainsCN.contains(1)) {
			String strain1 = strainsCN.get(0).replaceAll(" ", "");
			String strain2 = strainsCN.get(1).replaceAll(" ", "");
			if (strain1.matches("[0-9]+") && strain2.matches("[0-9]+")) {
			    if (Integer.parseInt(strain2) - Integer.parseInt(strain1) <= 20) {
				for (int strCount = Integer.parseInt(strain1); strCount <= Integer
					.parseInt(strain2); strCount++) {
				    String tmp = A;
				    tmp = tmp.replace("STRAIN", Integer.toString(strCount));
				    tmp = tmp.replaceAll("[ ]+", " ");
				    if (tmp.length() > 2
					    && tmp.substring(tmp.length() - 2, tmp.length() - 2).equals(" ")) {
					tmp = tmp.substring(0, tmp.length() - 2);
				    }
				    if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
					Output_Split_mention_Ind.put(MNoSpace,
						Output_Split_mention_Ind.get(MNoSpace) + "|" + tmp);
				    } else {
					Output_Split_mention_Ind.put(MNoSpace, tmp);
				    }
				}
			    }
			} else if (strain1.matches("[A-Z]+ ") && strain2.matches("[A-Z]+ ")) {
			    int strInt1 = strain1.replaceAll(" ", "").charAt(0);
			    int strInt2 = strain2.replaceAll(" ", "").charAt(0);
			    if (strInt2 - strInt1 <= 20) {
				for (int strCount = strInt1; strCount <= strInt2; strCount++) {
				    String tmp = A;
				    tmp = tmp.replace("STRAIN", Integer.toString(strCount));
				    tmp = tmp.replaceAll("[ ]+", " ");
				    if (tmp.length() > 2
					    && tmp.substring(tmp.length() - 2, tmp.length() - 2).equals(" ")) {
					tmp = tmp.substring(0, tmp.length() - 2);
				    }
				    if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
					Output_Split_mention_Ind.put(MNoSpace,
						Output_Split_mention_Ind.get(MNoSpace) + "|" + tmp);
				    } else {
					Output_Split_mention_Ind.put(MNoSpace, tmp);
				    }
				}
			    }
			} else {
			    if (Output_Split_mention.containsKey(MNoSpace)) {
				Output_Split_mention.put(MNoSpace,
					Output_Split_mention.get(MNoSpace) + "|" + Split_mention.get(m));
			    } else {
				Output_Split_mention.put(MNoSpace, Split_mention.get(m));
			    }
			}
		    }
		} else {
		    if (Output_Split_mention.containsKey(MNoSpace)) {
			Output_Split_mention.put(MNoSpace,
				Output_Split_mention.get(MNoSpace) + "|" + Split_mention.get(m));
		    } else {
			Output_Split_mention.put(MNoSpace, Split_mention.get(m));
		    }
		}
	    }
	}
	for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) {
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) {
		/*
		 * Check the boundary of BioCDocobj.Annotations.get(i) 5/1/2018
		 * Dongwook Shin
		 */
		if (j < BioCDocobj.Annotations.get(i).size()) {
		    // System.out.println(BioCDocobj.Annotations.get(i).get(j));
		    int Annotation_Num = BioCDocobj.Annotations.get(i).get(j).size();
		    for (int k = 0; k < Annotation_Num; k++) // k : Annotations
		    {
			String anno[] = BioCDocobj.Annotations.get(i).get(j).get(k).split("\\t"); // Mention
			String MenArr[] = anno[2].split("\\|");
			for (int m = 0; m < MenArr.length; m++) {
			    String MNoSpace = MenArr[m];
			    MNoSpace = MNoSpace.replaceAll("[ ]+", "");
			    if (Output_Split_mention_Ind.containsKey(MNoSpace)) {
				if (anno.length == 5) {
				    String split_men[] = Output_Split_mention_Ind.get(MNoSpace).split("\\|");
				    for (int s = 0; s < split_men.length; s++) {
					String anno2 = anno[2] + "|" + split_men[s];
					BioCDocobj.Annotations.get(i).get(j).add(anno[0] + "\t" + anno[1] + "\t" + anno2
						+ "\t" + anno[3] + "\t" + anno[4]);
				    }
				} else if (anno.length < 5) {
				    String split_men[] = Output_Split_mention_Ind.get(MNoSpace).split("\\|");
				    for (int s = 0; s < split_men.length; s++) {
					String anno2 = anno[2] + "|" + split_men[s];
					BioCDocobj.Annotations.get(i).get(j)
						.add(anno[0] + "\t" + anno[1] + "\t" + anno2 + "\t" + anno[3]);
				    }
				}
			    } else if (Output_Split_mention.containsKey(MNoSpace)) {
				if (anno.length == 5) {
				    String anno2 = anno[2] + "|" + Output_Split_mention.get(MNoSpace);
				    BioCDocobj.Annotations.get(i).get(j).set(k,
					    anno[0] + "\t" + anno[1] + "\t" + anno2 + "\t" + anno[3] + "\t" + anno[4]);
				} else if (anno.length < 5) {
				    BioCDocobj.Annotations.get(i).get(j).set(k,
					    anno[0] + "\t" + anno[1] + "\t" + anno[2] + "\t" + anno[3]);
				}
			    }
			}
		    }
		}
	    }
	}
	// BioCDocobj.BioCOutput(Filename,FilenameBioC,BioCDocobj.Annotations,false);
    }
}