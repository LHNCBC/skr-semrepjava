/**
 * Project: GNormPlus
 * Function: Gene Normalization
 */

package gov.nih.nlm.ner.gnormplus;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

// import GNormPluslib.GNormPlus;

import GNormPluslib.GNormPlus;

public class GNString {
    public static HashMap<String, String> MatchedTokens_hash = new HashMap<>();

    private double ScoringFunction(String geneid, HashMap<String, String> Mention_hash, String LF) {
	/*
	 * define gene/homo id
	 */

	// LF
	LF = LF.toLowerCase();
	LF = LF.replaceAll("([0-9])([a-z])", "$1 $2");
	LF = LF.replaceAll("([a-z])([0-9])", "$1 $2");
	LF = LF.replaceAll("([\\W\\-\\_])", " ");
	LF = LF.replaceAll("[ ]+", " ");
	String LF_tkn[] = LF.split(" ");
	int LF_ParticalMatch = 0;

	Pattern ptmp = Pattern.compile("[0-9]+\\-([0-9]+)");
	Matcher mtmp = ptmp.matcher(geneid);
	Pattern ptmp2 = Pattern.compile("([0-9]+)");
	Matcher mtmp2 = ptmp.matcher(geneid);
	if (mtmp.find()) {
	    geneid = "Homo:" + mtmp.group(1);
	} else {
	    geneid = "Gene:" + geneid;
	}

	if (GNormPlus.GeneScoring_hash.containsKey(geneid)) {
	    HashMap<String, Double> TF = new HashMap<>(); // token
							  // i in
							  // gene
							  // j
	    HashMap<String, Double> TermFrequency = new HashMap<>();

	    /*
	     * Tokens in Query (Gene id lexicon)
	     */
	    String l[] = GNormPlus.GeneScoring_hash.get(geneid).split("\t"); // Gene:2664293
									     // cmk-1,cytidylate-1,kinase-1,mssa-1
									     // 0.4096
									     // 4
									     // 0.0625
									     // 1
									     // 2.0
	    String tkns_Gene[] = l[0].split(",");
	    for (int i = 0; i < tkns_Gene.length; i++) {
		String Tkn_Freq[] = tkns_Gene[i].split("-");
		TermFrequency.put(Tkn_Freq[0], Double.parseDouble(Tkn_Freq[1]));
	    }
	    Double Cj = Double.parseDouble(l[1]);
	    Double AllTknNum = Double.parseDouble(l[2]);
	    // Double Cj_max = Double.parseDouble(l[3]);
	    // Double MaxTknNum = Double.parseDouble(l[4]);
	    Double Norm = Double.parseDouble(l[5]);
	    if (Norm == 0.0) {
		Norm = 1.0;
	    }

	    /*
	     * Tokens in Document (recognized mentions)
	     */
	    for (String Mention : Mention_hash.keySet()) {
		Mention = Mention.toLowerCase();
		Mention = Mention.replaceAll("([0-9])([a-z])", "$1 $2");
		Mention = Mention.replaceAll("([a-z])([0-9])", "$1 $2");
		Mention = Mention.replaceAll("([\\W\\-\\_])", " ");
		Mention = Mention.replaceAll("[ ]+", " ");
		String tkns_Mention[] = Mention.split(" ");
		for (int i = 0; i < tkns_Mention.length; i++) {
		    if (TermFrequency.containsKey(tkns_Mention[i])) {
			TF.put(tkns_Mention[i], TermFrequency.get(tkns_Mention[i]));
		    }
		}
	    }

	    Double score = 0.0;
	    for (String Tkn : TF.keySet()) {
		// LF
		for (int t = 0; t < LF_tkn.length; t++) {
		    if (LF_tkn[t].equals(Tkn)) {
			LF_ParticalMatch++;
		    }
		}

		double TFij = TF.get(Tkn) / AllTknNum;
		double IDFi = GNormPlus.GeneScoringDF_hash.get(Tkn);
		score = score + TFij * IDFi * (1 / (1 - TFij));
	    }
	    // score = Cj * (1/Norm) *score;
	    if (LF_ParticalMatch > 0) {
		score = score + LF_ParticalMatch;
		/* System.out.println(geneid+"\t"+LF+"\t"+score); */}
	    return score;
	} else {
	    // System.out.println("Error: cannot find geneid: "+geneid+" in
	    // GeneScoring_hash");
	    return 0.0;
	}
    }

    // public void PreProcessing4GN(BioCDoc BioCDocobj, GNormPlusStringWrapper gNormPlus, String FilenameBioC)
    public void PreProcessing4GN(BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus)
	    throws IOException, XMLStreamException {
	for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) {
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) {
		/*
		 * Check the boundary 5/1/2018 Dongwook Shin
		 */
		if (j < BioCDocobj.Annotations.get(i).size())
		    for (int k = 0; k < BioCDocobj.Annotations.get(i).get(j).size(); k++) {
			// if (k < BioCDocobj.Annotations.get(i).get(j).size()) {
			String anno[] = BioCDocobj.Annotations.get(i).get(j).get(k).split("\t");
			String start = anno[0];
			String last = anno[1];
			String mentions = anno[2];
			String type = anno[3];
			String id = anno[4];

			if (type.equals("Gene")) {
			    String mentionArr[] = mentions.split("\\|");
			    boolean update = false;
			    for (int m = 0; m < mentionArr.length; m++) {
				Pattern ptmp = Pattern.compile("^(.*[0-9A-Z])[ ]*p$");
				Matcher mtmp = ptmp.matcher(mentionArr[m]);
				Pattern ptmp2 = Pattern.compile("^(.+)nu$");
				Matcher mtmp2 = ptmp2.matcher(mentionArr[m]);
				Pattern ptmp3 = Pattern.compile("^(.+)alpha$");
				Matcher mtmp3 = ptmp3.matcher(mentionArr[m]);
				Pattern ptmp4 = Pattern.compile("^(.+)beta$");
				Matcher mtmp4 = ptmp4.matcher(mentionArr[m]);
				Pattern ptmp5 = Pattern.compile("^(.+[0-9])a$");
				Matcher mtmp5 = ptmp5.matcher(mentionArr[m]);
				Pattern ptmp6 = Pattern.compile("^(.+[0-9])b$");
				Matcher mtmp6 = ptmp6.matcher(mentionArr[m]);
				if (mtmp.find()) {
				    mentions = mentions + "|" + mtmp.group(1);
				    update = true;
				} else if (mtmp2.find()) {
				    mentions = mentions + "|" + mtmp2.group(1);
				    update = true;
				} else if (mtmp3.find()) {
				    mentions = mentions + "|" + mtmp3.group(1) + "a";
				    update = true;
				} else if (mtmp4.find()) {
				    mentions = mentions + "|" + mtmp4.group(1) + "b";
				    update = true;
				} else if (mtmp5.find()) {
				    mentions = mentions + "|" + mtmp5.group(1) + "alpha";
				    update = true;
				} else if (mtmp6.find()) {
				    mentions = mentions + "|" + mtmp6.group(1) + "beta";
				    update = true;
				}
			    }
			    if (update == true) {
				BioCDocobj.Annotations.get(i).get(j).set(k,
					start + "\t" + last + "\t" + mentions + "\t" + type + "\t" + id);
			    }
			}
			// }
		    }
	    }
	}
	// BioCDocobj.BioCOutput(Filename,FilenameBioC,BioCDocobj.Annotations,false);
    }

    public void ChromosomeRecognition(BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus)
	    throws IOException, XMLStreamException {
	for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) /** PMIDs : i */
	{
	    String Pmid = BioCDocobj.PMIDs.get(i);
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) /** Paragraphs : j */
	    {
		String PassageContext = BioCDocobj.PassageContexts.get(i).get(j); // Passage
										  // context

		/** Chromosome recognition */
		ArrayList<String> locations = gNormPlus.getPT_GeneChromosome().SearchMentionLocation(PassageContext,
			"ChromosomeLocation");
		for (int k = 0; k < locations.size(); k++) {
		    String anno[] = locations.get(k).split("\t");
		    // int start= Integer.parseInt(anno[0]);
		    // int last= Integer.parseInt(anno[1]);
		    // String mention = anno[2];
		    String ids = anno[3];
		    // BioCDocobj.Annotations.get(i).get(j).add(start+"\t"+last+"\t"+mention+"\tChromosomeLocation\t"+ids);
		    // //paragraph
		    String IDs[] = ids.split("[\\|,]");
		    for (int idcount = 0; idcount < IDs.length; idcount++) {
			// IDs[idcount] = IDs[idcount].replaceAll("\\-[0-9]+",
			// "");
			gNormPlus.getPmid2ChromosomeGene_hash().put(Pmid + "\t" + IDs[idcount], "");
		    }
		}
	    }
	}
	// BioCDocobj.BioCOutput(Filename,FilenameBioC,BioCDocobj.Annotations,false);
    }

    public void GeneNormalization(String Filename, BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus,
	    // public void GeneNormalization(BioCDoc BioCDocobj, GNormPlusStringWrapper gNormPlus,
	    boolean GeneIDMatch) throws IOException, XMLStreamException {
	final DecimalFormat df = new DecimalFormat("0.####");
	df.setRoundingMode(RoundingMode.HALF_UP);

	// Tokenization
	for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) /** PMIDs : i */
	{
	    String Pmid = BioCDocobj.PMIDs.get(i);

	    /*
	     * Collect Gene mentions :
	     * 
	     * GeneMention-taxid -> "ID" : geneid -> "type" : "Gene" -> start1-last1 : "" ->
	     * start2-last2 : "" -> start3-last3 : ""
	     */
	    HashMap<String, HashMap<String, String>> GeneMention_hash = new HashMap<>();
	    HashMap<String, String> Mention_hash = new HashMap<>();
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) /** Paragraphs : j */
	    {
		/*
		 * Boundary check for String version 05/01/2018 Dongwook Shin
		 */
		if (j < BioCDocobj.Annotations.get(i).size())
		    for (int k = 0; k < BioCDocobj.Annotations.get(i).get(j).size(); k++) /** Annotation : k */
		    {
			String anno[] = BioCDocobj.Annotations.get(i).get(j).get(k).split("\t");
			String start = anno[0];
			String last = anno[1];
			String mentions = anno[2];
			String type = anno[3];
			String taxid = anno[4];
			taxid = taxid.replaceAll("(Focus|Right|Left|Prefix):", "");
			if (type.matches("Gene")) {
			    if (GeneMention_hash.containsKey(mentions + "\t" + taxid)) {
				GeneMention_hash.get(mentions + "\t" + taxid).put(start + "\t" + last, "");
			    } else {
				HashMap<String, String> offset_hash = new HashMap<>();
				offset_hash.put(start + "\t" + last, "");
				GeneMention_hash.put(mentions + "\t" + taxid, offset_hash);
				GeneMention_hash.get(mentions + "\t" + taxid).put("type", type);
				Mention_hash.put(mentions, "Gene");
			    }
			} else if (type.matches("(FamilyName|DomainMotif)")) {
			    String GMs[] = mentions.split("\\|");
			    for (int g = 0; g < GMs.length; g++) {
				String mention = GMs[g];
				Mention_hash.put(mention, "FamilyDomain");
			    }
			}

		    }
	    }

	    /*
	     * Gene id refinement: 1. Official name 2. only one gene
	     */
	    HashMap<String, String> GuaranteedGene2ID = new HashMap<>();
	    HashMap<String, String> MultiGene2ID = new HashMap<>();
	    for (String GeneMentionTax : GeneMention_hash.keySet()) {
		String GT[] = GeneMentionTax.split("\\t");
		String mentions = GT[0];
		String tax = GT[1];
		String GMs[] = mentions.split("\\|");
		for (int ms = 0; ms < GMs.length; ms++) {
		    String mention = GMs[ms];
		    String IDstr = gNormPlus.getPT_Gene().MentionMatch(mention); /** searched by PT_Gene */
		    String IDs[] = IDstr.split("\\|");
		    String geneid = "";
		    for (int c = 0; c < IDs.length; c++) {
			String tax2ID[] = IDs[c].split(":"); // tax2ID[0] =
							     // taxid ;
							     // tax2ID[1] =
							     // geneids
			if (tax2ID[0].equals(tax)) {
			    geneid = tax2ID[1];
			    GeneMention_hash.get(GeneMentionTax).put("ID", geneid);
			    break;
			}
		    }

		    // geneid refinement
		    if (GeneMention_hash.get(GeneMentionTax).containsKey("ID")) {
			Pattern ptmp = Pattern.compile("\\*([0-9]+(\\-[0-9]+|))");
			Matcher mtmp = ptmp.matcher(GeneMention_hash.get(GeneMentionTax).get("ID"));

			if (mtmp.find()) // 1. Official Name
			{
			    GeneMention_hash.get(GeneMentionTax).put("ID", mtmp.group(1));
			    GuaranteedGene2ID.put(GeneMentionTax, mtmp.group(1));
			} else if (GeneMention_hash.get(GeneMentionTax).get("ID").matches("[0-9]+(\\-[0-9]+|)")) // 2.
														 // only
														 // one
														 // gene
			{
			    GuaranteedGene2ID.put(GeneMentionTax, GeneMention_hash.get(GeneMentionTax).get("ID"));
			} else {
			    String ID[] = GeneMention_hash.get(GeneMentionTax).get("ID").split(",");
			    boolean FoundByChroLoca = false;
			    for (int idcount = 0; idcount < ID.length; idcount++) {
				if (gNormPlus.getPmid2ChromosomeGene_hash().containsKey(Pmid + "\t" + ID[idcount])) // 3.
														    // Chromosome
														    // location
				{
				    GuaranteedGene2ID.put(GeneMentionTax, ID[idcount]);
				    FoundByChroLoca = true;
				    break;
				}
			    }
			    if (FoundByChroLoca == false) {
				MultiGene2ID.put(GeneMentionTax, GeneMention_hash.get(GeneMentionTax).get("ID"));
			    }
			}
		    }
		}
	    }

	    /*
	     * Gene id refinement: 3. multiple genes but can be inferred by 1. and 2.
	     */
	    for (String GeneMentionTax_M : MultiGene2ID.keySet()) {
		for (String GeneMentionTax_G : GuaranteedGene2ID.keySet()) {
		    String MG[] = MultiGene2ID.get(GeneMentionTax_M).split(",");
		    for (int m = 0; m < MG.length; m++) {
			if (MG[m].equals(GuaranteedGene2ID.get(GeneMentionTax_G))) {
			    GeneMention_hash.get(GeneMentionTax_M).put("ID", MG[m]);
			}
		    }
		}
	    }

	    /*
	     * Gene id refinement: 4. FullName -> Abbreviation
	     */
	    for (String GeneMentionTax : GeneMention_hash.keySet()) {
		String MT[] = GeneMentionTax.split("\\t");
		if (gNormPlus.getPmidLF2Abb_hash().containsKey(Pmid + "\t" + MT[0])) {
		    String GeneMentionTax_Abb = gNormPlus.getPmidLF2Abb_hash().get(Pmid + "\t" + MT[0]) + "\t" + MT[1];
		    if (GeneMention_hash.containsKey(GeneMentionTax_Abb)
			    && GeneMention_hash.get(GeneMentionTax).containsKey("ID")) {
			GeneMention_hash.get(GeneMentionTax_Abb).put("ID",
				GeneMention_hash.get(GeneMentionTax).get("ID"));
		    }
		}

	    }

	    /*
	     * Gene id refinement: 5. Ranking by scoring function (inference network)
	     */
	    for (String GeneMentionTax : GeneMention_hash.keySet()) {
		if (GeneMention_hash.get(GeneMentionTax).containsKey("ID")
			&& GeneMention_hash.get(GeneMentionTax).get("ID").matches(".+,.+")) {
		    String geneids = GeneMention_hash.get(GeneMentionTax).get("ID");
		    String geneid[] = geneids.split(",");

		    String OutputStyle = "Top1";
		    if (OutputStyle.equals("Top1")) {
			// only return the best one
			double max_score = 0.0;
			String target_geneid = "";
			for (int g = 0; g < geneid.length; g++) {
			    String MT[] = GeneMentionTax.split("\\t");
			    String LF = "";
			    if (gNormPlus.getPmidAbb2LF_hash().containsKey(Pmid + "\t" + MT[0])) {
				LF = gNormPlus.getPmidAbb2LF_hash().get(Pmid + "\t" + MT[0]);
			    }
			    double score = ScoringFunction(geneid[g], Mention_hash, LF);
			    if (score > max_score) {
				max_score = score;
				target_geneid = geneid[g];
			    } else if (score == 0.0) {
				// System.out.println(GeneMentionTax);
			    }
			}
			GeneMention_hash.get(GeneMentionTax).put("ID", target_geneid);
		    } else // "All"
		    {
			// return all geneids
			String geneSTR = "";
			for (int g = 0; g < geneid.length; g++) {
			    String MT[] = GeneMentionTax.split("\\t");
			    String LF = "";
			    if (gNormPlus.getPmidAbb2LF_hash().containsKey(Pmid + "\t" + MT[0])) {
				LF = gNormPlus.getPmidAbb2LF_hash().get(Pmid + "\t" + MT[0]);
			    }
			    double score = ScoringFunction(geneid[g], Mention_hash, LF);
			    String hoge = df.format(score);
			    score = Double.parseDouble(hoge);

			    if (geneSTR.equals("")) {
				geneSTR = geneid[g] + "-" + score;
			    } else {
				geneSTR = geneSTR + "," + geneid[g] + "-" + score;
			    }
			}
			GeneMention_hash.get(GeneMentionTax).put("ID", geneSTR);
		    }
		}
	    }

	    /*
	     * Gene id refinement: - removed (Reason: cause too much False Positive) 6.
	     * Abbreviation -> FullName
	     * 
	     */
	    for (String GeneMentionTax : GeneMention_hash.keySet()) {
		String MT[] = GeneMentionTax.split("\\t");
		if (gNormPlus.getPmidAbb2LF_hash().containsKey(Pmid + "\t" + MT[0])) {
		    String GeneMentionTax_LF = gNormPlus.getPmidAbb2LF_hash().get(Pmid + "\t" + MT[0]) + "\t" + MT[1];
		    if (GeneMention_hash.containsKey(GeneMentionTax_LF)
			    && GeneMention_hash.get(GeneMentionTax).containsKey("ID")) {
			GeneMention_hash.get(GeneMentionTax_LF).put("ID",
				GeneMention_hash.get(GeneMentionTax).get("ID"));
		    }
		}
	    }

	    /*
	     * Gene id refinement: 7. The inference network tokens of Abbreviation.ID should
	     * contain at least LF tokens 8. The short mention should be filtered if not
	     * long form support
	     */
	    ArrayList<String> removeGMT = new ArrayList<>();
	    for (String GeneMentionTax : GeneMention_hash.keySet()) {
		String GT[] = GeneMentionTax.split("\\t");
		String mentions = GT[0];
		String tax = GT[1];
		if (GeneMention_hash.get(GeneMentionTax).containsKey("type")
			&& GeneMention_hash.get(GeneMentionTax).get("type").equals("Gene")
			&& GeneMention_hash.get(GeneMentionTax).containsKey("ID")) {
		    String type = GeneMention_hash.get(GeneMentionTax).get("type");
		    String id = GeneMention_hash.get(GeneMentionTax).get("ID");
		    String geneid = "";
		    Pattern ptmp1 = Pattern.compile("^([0-9]+)\\-([0-9]+)$");
		    Pattern ptmp2 = Pattern.compile("^([0-9]+)$");
		    Matcher mtmp1 = ptmp1.matcher(id);
		    Matcher mtmp2 = ptmp2.matcher(id);
		    if (mtmp1.find()) {
			geneid = "Homo:" + mtmp1.group(2);
		    } else if (mtmp2.find()) {
			geneid = "Gene:" + mtmp2.group(1);
		    }

		    boolean LongFormTknMatch = false;
		    boolean LongFormExist = true;
		    if (gNormPlus.getGeneScoring_hash().containsKey(geneid)) {
			if (gNormPlus.getPmidAbb2LF_lc_hash().containsKey(Pmid + "\t" + mentions.toLowerCase())) {
			    /*
			     * token in lexicon : tkn_lexicon token in mention : tkn_mention
			     */
			    String l[] = gNormPlus.getGeneScoring_hash().get(geneid).split("\t"); // Gene:2664293
												  // cmk-1,cytidylate-1,kinase-1,mssa-1
												  // 0.4096
												  // 4
												  // 0.0625
												  // 1
												  // 2.0
			    String tkns_Gene[] = l[0].split(",");
			    ArrayList<String> tkn_lexicon = new ArrayList<>();
			    for (int ti = 0; ti < tkns_Gene.length; ti++) {
				String Tkn_Freq[] = tkns_Gene[ti].split("-");
				tkn_lexicon.add(Tkn_Freq[0]);
			    }
			    String tkn_mention[] = gNormPlus.getPmidAbb2LF_lc_hash()
				    .get(Pmid + "\t" + mentions.toLowerCase()).split("[\\W\\-\\_]");

			    for (int tl = 0; tl < tkn_lexicon.size(); tl++) {
				for (int tm = 0; tm < tkn_mention.length; tm++) {
				    if (tkn_lexicon.get(tl).equals(tkn_mention[tm])
					    && (!tkn_mention[tm].matches("[0-9]+"))) {
					LongFormTknMatch = true;
				    }
				}
			    }
			} else {
			    LongFormExist = false;
			}
		    } else {
			LongFormTknMatch = true;
		    } // exception

		    if (LongFormTknMatch == false && LongFormExist == true) // 7.
		    {
			removeGMT.add(GeneMentionTax); // remove short form
			removeGMT.add(gNormPlus.getPmidAbb2LF_hash().get(Pmid + "\t" + mentions) + "\t" + tax); // remove
														// long
														// form
		    } else if (mentions.length() <= 2 && LongFormExist == false) // 8.
		    {
			removeGMT.add(GeneMentionTax);
		    }
		}
	    }

	    for (int gmti = 0; gmti < removeGMT.size(); gmti++) // remove
	    {
		GeneMention_hash.remove(removeGMT.get(gmti));
	    }

	    // Append gene ids
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) // Paragraphs : j
	    {
		/*
		 * Boundary check for String version 05/01/2018 Dongwook Shin
		 */
		if (j < BioCDocobj.Annotations.get(i).size())
		    for (int k = 0; k < BioCDocobj.Annotations.get(i).get(j).size(); k++) // Annotation : k
		    {
			String anno[] = BioCDocobj.Annotations.get(i).get(j).get(k).split("\t");
			String start = anno[0];
			String last = anno[1];
			String mentions = anno[2];
			String type = anno[3];
			String taxid_org = anno[4];
			String taxid = taxid_org.replaceAll("(Focus|Right|Left|Prefix):", "");
			String GMs[] = mentions.split("\\|");

			if (type.equals("Gene")) {
			    BioCDocobj.Annotations.get(i).get(j).set(k,
				    BioCDocobj.Annotations.get(i).get(j).get(k) + "|");

			    if (GeneMention_hash.containsKey(mentions + "\t" + taxid)
				    && GeneMention_hash.get(mentions + "\t" + taxid).containsKey("ID")) {
				BioCDocobj.Annotations.get(i).get(j).set(k, BioCDocobj.Annotations.get(i).get(j).get(k)
					+ GeneMention_hash.get(mentions + "\t" + taxid).get("ID") + ",");
			    } else // cannot find appropriate species
			    {
				// System.out.println(mention+"\t"+taxid);
			    }
			    BioCDocobj.Annotations.get(i).get(j).set(k, BioCDocobj.Annotations.get(i).get(j).get(k)
				    .substring(0, BioCDocobj.Annotations.get(i).get(j).get(k).length() - 1)); // remove
																								     // ",$"
			}
		    }
	    }

	    // Extend to all gene mentions
	    HashMap<String, String> GeneMentions = new HashMap<>(); // Extending
								    // Gene
								    // mentions
	    HashMap<String, String> GeneMentionLocation = new HashMap<>(); // Extending
									   // Gene
									   // mentions
	    for (int j = 0; j < BioCDocobj.Annotations.get(i).size(); j++) // Paragraph
	    {
		for (int k = 0; k < BioCDocobj.Annotations.get(i).get(j).size(); k++) // Annotation
										      // :
										      // k
		{
		    String anno[] = BioCDocobj.Annotations.get(i).get(j).get(k).split("\t");
		    int start = Integer.parseInt(anno[0]);
		    int last = Integer.parseInt(anno[1]);
		    String mentions = anno[2];
		    String type = anno[3];
		    String id = anno[4];
		    if (type.equals("Gene")
			    && id.matches("(Focus|Right|Left|Prefix)\\:([0-9]+)\\|([0-9]+)\\-([0-9]+)")) {
			GeneMentions.put(mentions.toLowerCase(), id);
			for (int s = start; s <= last; s++) {
			    GeneMentionLocation.put(j + "\t" + s, "");
			}
		    } else if (type.equals("Gene") && id.matches("(Focus|Right|Left|Prefix)\\:([0-9]+)\\|([0-9]+)")) {
			GeneMentions.put(mentions.toLowerCase(), id);
			for (int s = start; s <= last; s++) {
			    GeneMentionLocation.put(j + "\t" + s, "");
			}
		    }
		}
	    }
	    for (int j = 0; j < BioCDocobj.Annotations.get(i).size(); j++) // Paragraph
	    {
		String PassageContexts = " " + BioCDocobj.PassageContexts.get(i).get(j) + " ";
		String PassageContexts_tmp = PassageContexts.toLowerCase();
		for (String gm : GeneMentions.keySet()) {
		    String id = GeneMentions.get(gm);
		    if (gm.length() >= 3) {
			gm = gm.replaceAll("[ ]*[\\|]*$", "");
			gm = gm.replaceAll("^[\\|]*[ ]*", "");
			gm = gm.replaceAll("[\\|][\\|]+", "\\|");
			if (!gm.matches("[\\W\\-\\_]*")) {
			    gm = gm.replaceAll("([^A-Za-z0-9\\| ])", "\\\\$1");
			    Pattern ptmp = Pattern.compile("^(.*[\\W\\-\\_])(" + gm + ")([\\W\\-\\_].*)$");
			    Matcher mtmp = ptmp.matcher(PassageContexts_tmp);
			    while (mtmp.find()) {
				String pre = mtmp.group(1);
				String gmtmp = mtmp.group(2);
				String post = mtmp.group(3);

				int start = pre.length() - 1;
				int last = start + gmtmp.length();
				String mention = PassageContexts.substring(start + 1, last + 1);
				if (!GeneMentionLocation.containsKey(j + "\t" + start)
					&& !GeneMentionLocation.containsKey(j + "\t" + last)) {
				    BioCDocobj.Annotations.get(i).get(j)
					    .add(start + "\t" + last + "\t" + mention + "\tGene\t" + id);
				}
				gmtmp = gmtmp.replaceAll(".", "\\@");
				PassageContexts_tmp = pre + "" + gmtmp + "" + post;
				mtmp = ptmp.matcher(PassageContexts_tmp);
			    }
			}
		    }
		}
	    }
	}
	if (GeneIDMatch == true) {
	    // BioCDocobj.BioCOutput(Filename,FilenameBioC,BioCDocobj.Annotations,false);
	} else {
	    BioCDocobj.BioCOutput(Filename, null, BioCDocobj.Annotations, true);
	}
    }

    /*
     * Search Potential GeneID in the Prefix Tree
     */
    public ArrayList<String> SearchGeneIDLocation(String Doc) {
	ArrayList<String> location = new ArrayList<>();

	String Doc_tmp = " " + Doc + " ";
	Pattern ptmp = Pattern.compile(
		"^(.*[^A-Za-z0-9]+)([0-9]+\\S*[A-Za-z]+|[A-Za-z]+\\S*[0-9]+|[0-9]+\\S*[A-Za-z]+\\S*[0-9]+|[A-Za-z]+\\S*[0-9]+\\S*[A-Za-z]+)([^A-Za-z0-9]+.*)$");
	Matcher mtmp = ptmp.matcher(Doc_tmp);
	while (mtmp.find()) {
	    String str1 = mtmp.group(1);
	    String str2 = mtmp.group(2);
	    String str3 = mtmp.group(3);
	    for (int m = str1.length(); m <= (str1.length() + str2.length()); m++) {
		int start = str1.length() - 1;
		int last = start + str2.length();
		String mention = Doc.substring(start, last);
		if (!mention.matches(".*[\\'\\;\\[\\]\\+\\*\\\\].*")) {
		    if (last - start > 6 && (mention.matches(".*\\(.*\\).*") || mention.matches("[^\\(\\)]+"))) {
			Pattern ptmp1 = Pattern.compile("^(.+[^0-9])([0-9]+)\\-([0-9]+)$");
			Matcher mtmp1 = ptmp1.matcher(mention);
			Pattern ptmp2 = Pattern.compile("^(.+[^0-9])([0-9]+)\\-(.+[^0-9])([0-9]+)$");
			Matcher mtmp2 = ptmp2.matcher(mention);
			if (mtmp1.find()) {
			    String S1 = mtmp1.group(1);
			    if (mtmp1.group(2).length() <= 6 && mtmp1.group(3).length() <= 6) {
				int Num1 = Integer.parseInt(mtmp1.group(2));
				int Num2 = Integer.parseInt(mtmp1.group(3));
				String prefix = "";
				Pattern ptmp3 = Pattern.compile("^([0]+)");
				Matcher mtmp3 = ptmp3.matcher(mtmp1.group(2));
				if (mtmp3.find()) {
				    prefix = mtmp3.group(1);
				}
				if (Num2 - Num1 > 0 && (Num2 - Num1 <= 20)) {
				    for (int n = Num1; n <= Num2; n++) {
					String StrNum = S1 + prefix + n;
					if (StrNum.length() >= 5) {
					    location.add(start + "\t" + last + "\t" + StrNum + "\tGeneID");
					}
				    }
				}
			    }
			} else if (mtmp2.find()) {
			    if (mtmp2.group(2).length() <= 6 && mtmp2.group(4).length() <= 6) {
				String S1 = mtmp2.group(1);
				int Num1 = Integer.parseInt(mtmp2.group(2));
				String S2 = mtmp2.group(3);
				int Num2 = Integer.parseInt(mtmp2.group(4));
				if (S1.equals(S2)) {
				    String prefix = "";
				    Pattern ptmp3 = Pattern.compile("^([0]+)");
				    Matcher mtmp3 = ptmp3.matcher(mtmp2.group(2));
				    if (mtmp3.find()) {
					prefix = mtmp3.group(1);
				    }
				    if (Num2 - Num1 > 0 && (Num2 - Num1 <= 20)) {
					for (int n = Num1; n <= Num2; n++) {
					    String StrNum = S1 + prefix + n;
					    if (StrNum.length() >= 5) {
						location.add(start + "\t" + last + "\t" + StrNum + "\tGeneID");
					    }
					}
				    }
				}
			    }
			}
		    }
		    location.add(start + "\t" + last + "\t" + mention + "\tGeneID");
		}
	    }
	    String men = "";
	    for (int m = 0; m < str2.length(); m++) {
		men = men + "@";
	    }
	    Doc_tmp = str1 + men + str3;
	    mtmp = ptmp.matcher(Doc_tmp);
	}
	return location;
    }

    // public void GeneIDRecognition(String Filename, BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus,
    public List<String> GeneIDRecognition(BioCDocString BioCDocobj, GNormPlusStringWrapper gNormPlus)
	    throws IOException, XMLStreamException {

	for (int i = 0; i < BioCDocobj.PMIDs.size(); i++) /** PMIDs : i */
	{
	    for (int j = 0; j < BioCDocobj.PassageNames.get(i).size(); j++) /** Paragraphs : j */
	    {
		String PassageContext = BioCDocobj.PassageContexts.get(i).get(j); // Passage context
		/**
		 * GeneID recognition by patternmatch
		 */
		ArrayList<String> locations = SearchGeneIDLocation(PassageContext);
		for (int k = 0; k < locations.size(); k++) {
		    String anno[] = locations.get(k).split("\t");
		    String mention = anno[2].toLowerCase();
		    mention = mention.replaceAll("[\\W\\-\\_]+", "");
		    // System.out.println("mention : " + mention);
		    if (gNormPlus.getGeneIDs_hash().containsKey(mention)) {
			if (j < BioCDocobj.Annotations.get(i).size()) {
			    BioCDocobj.Annotations.get(i).get(j)
				    .add(locations.get(k) + "\tGeneID:" + gNormPlus.getGeneIDs_hash().get(mention)); // paragraph
			}
		    }
		}
	    }
	}
	return BioCDocobj.BioCOutput(BioCDocobj.PassageContexts.get(0).get(0), null, BioCDocobj.Annotations, true);
    }
}