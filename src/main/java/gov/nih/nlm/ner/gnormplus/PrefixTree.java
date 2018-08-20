/**
 * Project: GNormPlus
 * Function: Dictionary lookup by Prefix Tree
 */

package gov.nih.nlm.ner.gnormplus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PrefixTree {
    private Tree Tr = new Tree();

    /*
     * Read Dictionary and insert Mention into the Prefix Tree
     */
    public static HashMap<String, String> StopWord_hash = new HashMap<>();

    public void Hash2Tree(HashMap<String, String> ID2Names) {
	for (String ID : ID2Names.keySet()) {
	    Tr.insertMention(ID2Names.get(ID), ID);
	}
    }

    public void Dictionary2Tree_Combine(String Filename, String StopWords, String MentionType) {
	try {
	    //System.out.println("Dictionary2Tree_Combine : " + Filename);

	    /** Stop Word */
	    BufferedReader br = new BufferedReader(new FileReader(StopWords));
	    String line = "";
	    while ((line = br.readLine()) != null) {
		StopWord_hash.put(line, "StopWord");
	    }
	    br.close();

	    BufferedReader inputfile = new BufferedReader(new FileReader(Filename));
	    line = "";
	    //int count=0;
	    while ((line = inputfile.readLine()) != null) {
		//count++;
		//if(count%10000==0){	System.out.println(count);	}
		String Column[] = line.split("\t");
		if (Column.length > 1) {
		    Column[0] = Column[0].replace("species:ncbi:", "");
		    Column[1] = Column[1].replaceAll(" strain=", " ");
		    Column[1] = Column[1].replaceAll(
			    "[\\W\\-\\_](str.|strain|substr.|substrain|var.|variant|subsp.|subspecies|pv.|pathovars|pathovar|br.|biovar)[\\W\\-\\_]",
			    " ");
		    Column[1] = Column[1].replaceAll("[\\(\\)]", " ");
		    String SpNameColumn[] = Column[1].split("\\|");
		    for (int i = 0; i < SpNameColumn.length; i++) {
			String tmp = SpNameColumn[i];
			tmp = tmp.replaceAll("[\\W\\-\\_0-9]", "");

			/*
			 * Criteria for Species
			 */
			if (MentionType.equals("Species") && (!SpNameColumn[i].substring(0, 1).matches("[\\W\\-\\_]"))
				&& (!SpNameColumn[i].matches("a[\\W\\-\\_].*")) && tmp.length() >= 3) {
			    if (!StopWord_hash.containsKey(SpNameColumn[i].toLowerCase())) {
				Tr.insertMention(SpNameColumn[i], Column[0]);
			    }
			}
			/*
			 * Criteria for Gene
			 */
			else if (MentionType.equals("Gene") && (!SpNameColumn[i].substring(0, 1).matches("[\\W\\-\\_]"))
				&& tmp.length() >= 3) {
			    if (!StopWord_hash.containsKey(SpNameColumn[i].toLowerCase())) {
				Tr.insertMention(SpNameColumn[i], Column[0]);
			    }
			}
			/*
			 * Criteria for Cell
			 */
			else if (MentionType.equals("Cell") && (!SpNameColumn[i].substring(0, 1).matches("[\\W\\-\\_]"))
				&& tmp.length() >= 3) {
			    if (!StopWord_hash.containsKey(SpNameColumn[i].toLowerCase())) {
				Tr.insertMention(SpNameColumn[i], Column[0]);
			    }
			}
		    }
		}
	    }
	    inputfile.close();
	} catch (IOException e1) {
	    System.out.println("[Dictionary2Tree_Combine]: Input file is not exist.");
	}
    }

    public void Dictionary2Tree_UniqueGene(String Filename, String StopWords, String Preifx) {
	try {
	    //System.out.println("Dictionary2Tree_UniqueGene : " + Filename);

	    /** Stop Word */
	    BufferedReader br = new BufferedReader(new FileReader(StopWords));
	    String line = "";
	    while ((line = br.readLine()) != null) {
		StopWord_hash.put(line, "StopWord");
	    }
	    br.close();

	    BufferedReader inputfile = new BufferedReader(new FileReader(Filename));
	    line = "";
	    //int count=0;
	    while ((line = inputfile.readLine()) != null) {
		//count++;
		//if(count%10000==0){	System.out.println(count);	}
		String Column[] = line.split("\t");
		if (Column.length > 1) {
		    if (!StopWord_hash.containsKey(Column[0].toLowerCase())) {
			if (Preifx.equals("")) {
			    Tr.insertMention(Column[0], Column[1]);
			} else if (Preifx.equals("Num") && Column[0].matches("[0-9].*")) {
			    Tr.insertMention(Column[0], Column[1]);
			} else if (Preifx.equals("AZNum") && Column[0].matches("[a-z][0-9].*")) {
			    Tr.insertMention(Column[0], Column[1]);
			} else if (Preifx.equals("lo") && Column[0].length() > 2
				&& Column[0].substring(0, 2).equals(Preifx)) {
			    if (!Column[0].matches("loc[0-9]+")) {
				Tr.insertMention(Column[0], Column[1]);
			    }
			} else if (Preifx.equals("un") && Column[0].length() > 2
				&& Column[0].substring(0, 2).equals(Preifx)) {
			    if (Column[0].length() >= 6 && Column[0].substring(0, 6).equals("unchar")) {
				// remove uncharacterized
			    } else {
				Tr.insertMention(Column[0], Column[1]);
			    }
			} else if (Column[0].length() > 2 && Column[0].substring(0, 2).equals(Preifx)) {
			    Tr.insertMention(Column[0], Column[1]);
			}
		    }
		}
	    }
	    inputfile.close();
	} catch (IOException e1) {
	    System.out.println("[Dictionary2Tree_UniqueGene]: Input file is not exist.");
	}
    }

    public void TreeFile2Tree(String Filename) {
	try {
	    //System.out.println("TreeFile2Tree : " + Filename);

	    BufferedReader inputfile = new BufferedReader(new FileReader(Filename));
	    String line = "";
	    int count = 0;
	    while ((line = inputfile.readLine()) != null) {
		String Anno[] = line.split("\t");
		//if(Anno.length<2){System.out.println(count);} //check error
		String LocationInTree = Anno[0];
		String token = Anno[1];
		String identifier = "";
		if (Anno.length == 3) {
		    identifier = Anno[2];
		}
		String LocationsInTree[] = LocationInTree.split("-");
		TreeNode tmp = Tr.root;
		for (int i = 0; i < LocationsInTree.length - 1; i++) {
		    tmp = tmp.links.get(Integer.parseInt(LocationsInTree[i]) - 1);
		}
		tmp.InsertToken(token, identifier);
		//if(count%10000==0){System.out.println(count);}
		count++;
	    }
	    inputfile.close();
	} catch (IOException e1) {
	    System.out.println("[TreeFile2Tee]: Input file is not exist.");
	}
    }

    /*
     * Search target mention in the Prefix Tree
     */
    public String MentionMatch(String Mentions) {
	ArrayList<String> location = new ArrayList<>();
	String Menlist[] = Mentions.split("\\|");
	for (int m = 0; m < Menlist.length; m++) {
	    String Mention = Menlist[m];
	    String Mention_lc = Mention.toLowerCase();
	    Mention_lc = Mention_lc.replaceAll("[\\W\\-\\_]+", "");
	    Mention_lc = Mention_lc.replaceAll("([0-9])([a-z])", "$1 $2");
	    Mention_lc = Mention_lc.replaceAll("([a-z])([0-9])", "$1 $2");
	    String Tkns[] = Mention_lc.split(" ");

	    int PrefixTranslation = 0;
	    int i = 0;
	    boolean find = false;
	    TreeNode tmp = Tr.root;
	    while (i < Tkns.length && tmp.CheckChild(Tkns[i], PrefixTranslation) >= 0) //Find Tokens in the links
	    {
		if (i == Tkns.length - 1) {
		    PrefixTranslation = 1;
		}
		tmp = tmp.links.get(tmp.CheckChild(Tkns[i], PrefixTranslation)); //move point to the link
		find = true;
		i++;
	    }
	    if (find == true) {
		if (i == Tkns.length) {
		    if (!tmp.Concept.equals("")) {
			return tmp.Concept;
		    } else {
			return "-1";
			//gene id is not found.
		    }
		} else {
		    return "-2";
		    //the gene mention matched a substring in PrefixTree.
		}
	    } else {
		return "-3";
		//mention is not found
	    }
	}
	return "-3"; //mention is not found
    }

    /*
     * Search target mention in the Prefix Tree ConceptType:
     * Species|Genus|Cell|CTDGene
     */
    public ArrayList<String> SearchMentionLocation(String Doc, String ConceptType) {
	ArrayList<String> location = new ArrayList<>();
	String Doc_org = Doc;
	Doc = Doc.toLowerCase();
	String Doc_lc = Doc;
	Doc = Doc.replaceAll("([0-9])([A-Za-z])", "$1 $2");
	Doc = Doc.replaceAll("([A-Za-z])([0-9])", "$1 $2");
	Doc = Doc.replaceAll("[\\W^;:,]+", " ");

	/*
	 * = keep special characters =
	 * 
	 * String regex="\\s+|(?=\\p{Punct})|(?<=\\p{Punct})"; String
	 * DocTkns[]=Doc.split(regex);
	 */

	String DocTkns[] = Doc.split(" ");
	int Offset = 0;
	int Start = 0;
	int Last = 0;
	int FirstTime = 0;

	while (Doc_lc.length() > 0 && Doc_lc.substring(0, 1).matches("[\\W]")) //clean the forward whitespace
	{
	    Doc_lc = Doc_lc.substring(1);
	    Offset++;
	}

	for (int i = 0; i < DocTkns.length; i++) {
	    TreeNode tmp = Tr.root;
	    boolean find = false;
	    int PrefixTranslation = 2;
	    while (tmp.CheckChild(DocTkns[i], PrefixTranslation) >= 0) //Find Tokens in the links
	    {
		tmp = tmp.links.get(tmp.CheckChild(DocTkns[i], PrefixTranslation)); //move point to the link
		if (Start == 0 && FirstTime > 0) {
		    Start = Offset;
		} //Start <- Offset 
		if (Doc_lc.substring(0, DocTkns[i].length()).equals(DocTkns[i])) {
		    if (DocTkns[i].length() > 0) {
			Doc_lc = Doc_lc.substring(DocTkns[i].length());
			Offset = Offset + DocTkns[i].length();
		    }
		}
		Last = Offset;
		while (Doc_lc.length() > 0 && Doc_lc.substring(0, 1).matches("[\\W]")) //clean the forward whitespace
		{
		    Doc_lc = Doc_lc.substring(1);
		    Offset++;
		}
		i++;

		if (ConceptType.equals("Species")) {
		    if (i < DocTkns.length - 2 && DocTkns[i].matches(
			    "(str|strain|substr|substrain|subspecies|subsp|var|variant|pathovars|pv|biovar|bv)")) {
			Doc_lc = Doc_lc.substring(DocTkns[i].length());
			Offset = Offset + DocTkns[i].length();
			Last = Offset;
			while (Doc_lc.length() > 0 && Doc_lc.substring(0, 1).matches("[\\W]")) //clean the forward whitespace
			{
			    Doc_lc = Doc_lc.substring(1);
			    Offset++;
			}
			i++;
		    }
		}

		find = true;
		if (i >= DocTkns.length) {
		    break;
		} else if (i == DocTkns.length - 1) {
		    PrefixTranslation = 2;
		}
	    }

	    if (find == true) {
		if (!tmp.Concept.equals("")) //the last matched token has concept id 
		{
		    location.add(Start + "\t" + Last + "\t" + Doc_org.substring(Start, Last) + "\t" + tmp.Concept);
		}
		Start = 0;
		Last = 0;
		if (i > 0) {
		    i--;
		}
	    } else //if(find == false)
	    {
		if (Doc_lc.substring(0, DocTkns[i].length()).equals(DocTkns[i])) {
		    if (DocTkns[i].length() > 0) {
			Doc_lc = Doc_lc.substring(DocTkns[i].length());
			Offset = Offset + DocTkns[i].length();
		    }
		}
	    }

	    while (Doc_lc.length() > 0 && Doc_lc.substring(0, 1).matches("[\\W]")) //clean the forward whitespace
	    {
		Doc_lc = Doc_lc.substring(1);
		Offset++;
	    }
	    FirstTime++;
	}
	return location;
    }

    /*
     * Print out the Prefix Tree
     */
    public String PrintTree() {
	return Tr.PrintTree_preorder(Tr.root, "");
    }
}

class Tree {
    /*
     * Prefix Tree - root node
     */
    public TreeNode root;

    public Tree() {
	root = new TreeNode("-ROOT-");
    }

    /*
     * Insert mention into the tree
     */
    public void insertMention(String Mention, String Identifier) {
	Mention = Mention.toLowerCase();

	Mention = Mention.replaceAll("([0-9])([A-Za-z])", "$1 $2");
	Mention = Mention.replaceAll("([A-Za-z])([0-9])", "$1 $2");
	Mention = Mention.replaceAll("[\\W\\-\\_]+", " ");
	/*
	 * = keep special characters =
	 * 
	 * String regex="\\s+|(?=\\p{Punct})|(?<=\\p{Punct})"; String
	 * Tokens[]=Mention.split(regex);
	 */
	String Tokens[] = Mention.split(" ");
	TreeNode tmp = root;
	for (int i = 0; i < Tokens.length; i++) {
	    if (tmp.CheckChild(Tokens[i], 0) >= 0) {
		tmp = tmp.links.get(tmp.CheckChild(Tokens[i], 0)); //go through next generation (exist node)
		if (i == Tokens.length - 1) {
		    tmp.Concept = Identifier;
		}
	    } else //not exist
	    {
		if (i == Tokens.length - 1) {
		    tmp.InsertToken(Tokens[i], Identifier);
		} else {
		    tmp.InsertToken(Tokens[i]);
		}
		tmp = tmp.links.get(tmp.NumOflinks - 1); //go to the next generation (new node)
	    }
	}
    }

    /*
     * Print the tree by pre-order
     */
    public String PrintTree_preorder(TreeNode node, String LocationInTree) {
	String opt = "";
	if (!node.token.equals("-ROOT-"))//Ignore root
	{
	    if (node.Concept.equals("")) {
		opt = opt + LocationInTree + "\t" + node.token + "\n";
	    } else {
		opt = opt + LocationInTree + "\t" + node.token + "\t" + node.Concept + "\n";
	    }
	}
	if (!LocationInTree.equals("")) {
	    LocationInTree = LocationInTree + "-";
	}
	for (int i = 0; i < node.NumOflinks; i++) {
	    opt = opt + PrintTree_preorder(node.links.get(i), LocationInTree + (i + 1));
	}
	return opt;
    }
}

class TreeNode {
    String token; //token of the node
    int NumOflinks; //Number of links
    public String Concept;
    ArrayList<TreeNode> links;

    public TreeNode(String Tok, String ID) {
	token = Tok;
	NumOflinks = 0;
	Concept = ID;
	links = new ArrayList<>();
    }

    public TreeNode(String Tok) {
	token = Tok;
	NumOflinks = 0;
	Concept = "";
	links = new ArrayList<>();
    }

    public TreeNode() {
	token = "";
	NumOflinks = 0;
	Concept = "";
	links = new ArrayList<>();
    }

    /*
     * Insert an new node under the target node
     */
    public void InsertToken(String Tok) {
	TreeNode NewNode = new TreeNode(Tok);
	links.add(NewNode);
	NumOflinks++;
    }

    public void InsertToken(String Tok, String ID) {
	TreeNode NewNode = new TreeNode(Tok, ID);
	links.add(NewNode);
	NumOflinks++;
    }

    /*
     * Check the tokens of children
     */
    public int CheckChild(String Tok, Integer PrefixTranslation) {
	for (int i = 0; i < links.size(); i++) {
	    if (Tok.equals(links.get(i).token)) {
		return (i);
	    }
	}

	if (PrefixTranslation == 1 && Tok.matches("(alpha|beta|gamam|[abg]|[12])")) // SuffixTranslationMap
	{
	    for (int i = 0; i < links.size(); i++) {
		GNormPlusStringWrapper GNP = GNormPlusStringWrapper.getInstance();
		if (GNP.SuffixTranslationMap.contains(Tok + "-" + links.get(i).token)) {
		    return (i);
		}
	    }
	} else if (PrefixTranslation == 2 && Tok.matches("[1-5]")) // for CTDGene feature
	{
	    for (int i = 0; i < links.size(); i++) {
		if (links.get(i).token.matches("[1-5]")) {
		    return (i);
		}
	    }
	}

	return (-1);
    }
}
