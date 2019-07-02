package gov.nih.nlm.umls.lexicon;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import gov.nih.nlm.nls.lexAccess.Api.LexAccessApi;
import gov.nih.nlm.nls.lexAccess.Api.LexAccessApiResult;
import gov.nih.nlm.nls.lexCheck.Api.ToJavaObjApi;
import gov.nih.nlm.nls.lexCheck.Lib.LexRecord;
import gov.nih.nlm.semrep.core.TokenInfo;

/**
 * Wrapper class for the UMLS SPECIALIST Lexicon.
 * 
 * @author Dongwook Shin
 * @author Halil Kilicoglu
 *
 */
public class LexiconWrapper {
    private static Logger log = Logger.getLogger(LexiconWrapper.class.getName());
    private LexAccessApi lexAccessApi;
    private static LexiconWrapper lexWrapper = null;

    // translations between the POS tags used in the Lexicon and the tags in the Penn Tagset 
    public static Map<String, List<String>> POS_TRANSLATION = null;
    public static Map<String, List<String>> REVERSE_POS_TRANSLATION = null;

    static {
	POS_TRANSLATION = new HashMap<>(40);
	POS_TRANSLATION.put("CC", Arrays.asList("conj"));
	POS_TRANSLATION.put("CD", Arrays.asList("num"));
	POS_TRANSLATION.put("DT", Arrays.asList("det"));
	POS_TRANSLATION.put("EX", Arrays.asList("adv"));
	POS_TRANSLATION.put("FW", null);
	POS_TRANSLATION.put("IN", Arrays.asList("prep", "conj", "compl"));
	POS_TRANSLATION.put("JJ", Arrays.asList("adj", "verb"));
	POS_TRANSLATION.put("JJR", Arrays.asList("adj", "verb"));
	POS_TRANSLATION.put("JJS", Arrays.asList("adj", "verb"));
	POS_TRANSLATION.put("LS", null);
	POS_TRANSLATION.put("MD", Arrays.asList("modal"));
	POS_TRANSLATION.put("NN", Arrays.asList("noun"));
	POS_TRANSLATION.put("NNS", Arrays.asList("noun"));
	POS_TRANSLATION.put("NNP", Arrays.asList("noun"));
	POS_TRANSLATION.put("NNPS", Arrays.asList("noun"));
	POS_TRANSLATION.put("PDT", Arrays.asList("det"));
	POS_TRANSLATION.put("POS", Arrays.asList("noun"));
	POS_TRANSLATION.put("PRP", Arrays.asList("pron"));
	POS_TRANSLATION.put("PRP$", Arrays.asList("pron"));
	POS_TRANSLATION.put("RB", Arrays.asList("adv"));
	POS_TRANSLATION.put("RBR", Arrays.asList("adv"));
	POS_TRANSLATION.put("RBS", Arrays.asList("adv"));
	POS_TRANSLATION.put("RP", null);
	POS_TRANSLATION.put("SYM", Arrays.asList("noun"));
	POS_TRANSLATION.put("TO", Arrays.asList("adv"));
	POS_TRANSLATION.put("UH", null);
	POS_TRANSLATION.put("VB", Arrays.asList("aux", "verb"));
	POS_TRANSLATION.put("VBD", Arrays.asList("aux", "verb"));
	POS_TRANSLATION.put("VBG", Arrays.asList("aux", "verb"));
	POS_TRANSLATION.put("VBN", Arrays.asList("aux", "verb"));
	POS_TRANSLATION.put("VBP", Arrays.asList("aux", "verb"));
	POS_TRANSLATION.put("VBZ", Arrays.asList("aux", "verb"));
	POS_TRANSLATION.put("WDT", Arrays.asList("pron"));
	POS_TRANSLATION.put("WP", Arrays.asList("pron")); // 	Wh-pronoun  who, what, whom
	POS_TRANSLATION.put("WP$", Arrays.asList("pron")); // 	Possessive wh-pronoun whose
	POS_TRANSLATION.put("WRB", Arrays.asList("adv")); //wh-adverb how, however, when, whenever, where, whereby, why

	REVERSE_POS_TRANSLATION = new HashMap<>();
	REVERSE_POS_TRANSLATION.put("adj", Arrays.asList("JJ", "JJR", "JJS"));
	REVERSE_POS_TRANSLATION.put("adv", Arrays.asList("EX", "RB", "RBR", "RBS", "TO", "WRB"));
	REVERSE_POS_TRANSLATION.put("aux", Arrays.asList("VB", "VBG", "VBN", "VBP", "VBZ"));
	REVERSE_POS_TRANSLATION.put("compl", Arrays.asList("IN"));
	REVERSE_POS_TRANSLATION.put("conj", Arrays.asList("CC", "IN"));
	REVERSE_POS_TRANSLATION.put("det", Arrays.asList("DT", "PDT"));
	REVERSE_POS_TRANSLATION.put("modal", Arrays.asList("MD"));
	REVERSE_POS_TRANSLATION.put("noun", Arrays.asList("NN", "NNS", "NNP", "NNPS", "POS", "SYM", "FW"));
	REVERSE_POS_TRANSLATION.put("num", Arrays.asList("CD"));
	REVERSE_POS_TRANSLATION.put("prep", Arrays.asList("IN"));
	REVERSE_POS_TRANSLATION.put("pron", Arrays.asList("PRP", "PRP$", "WDT", "WP", "WP$"));
	REVERSE_POS_TRANSLATION.put("verb", Arrays.asList("VB", "VBG", "VBN", "VBP", "VBZ"));
    }

    /**
     * Returns a singleton Lexicon access object.
     * 
     * @param configFile
     *            The file from which to read Lexicon configuration.
     * 
     * @return the singleton Lexicon access object
     */
    public static LexiconWrapper getInstance() {
	if (lexWrapper == null) {
		lexWrapper = new LexiconWrapper("semrepjava.properties");
	}
	return lexWrapper;
    }

    // private LexiconWrapper(String configFile) {
    private LexiconWrapper(String configFile) {
	log.info("Initializing a Lexicon instance...");
	lexAccessApi = new LexAccessApi(configFile);
//	Properties properties = System.getProperties();
//	Hashtable<java.lang.String, java.lang.String> hashProp = (Hashtable) System.getProperties();
//	lexAccessApi = new LexAccessApi(hashProp);
    }

    /**
     * Queries the Lexicon for lexical entries for a given list of tokens. The
     * lexical entries are longest-matching items.
     * 
     * @param tokens
     *            the input list of tokens
     * @return a list of <code>LexiconMatch</code> objects that represent the
     *         lexical entries corresponding to the list of input tokens
     * 
     * @throws SQLException
     *             when there is a problem with accessing the Lexicon
     */
    public List<LexiconMatch> findLexiconMatches(List<TokenInfo> tokens) throws SQLException {
	List<LexiconMatch> lexMatches = new ArrayList<>();
	List<String> tokenList = TokenInfo.getTokens(tokens);
	StringBuilder lexBuf = new StringBuilder();
	LexAccessApiResult curResult = null;
	int begIndex = 0;
	for (int i = 0; i < tokens.size(); i++) {
	    TokenInfo token = tokens.get(i);
	    String tok = token.getToken();
	    // ignore punctuations
	    if (Character.isLetterOrDigit(tok.charAt(0)) == false) {
		if (curResult != null) {
		    List<TokenInfo> matchedTokens = tokens.subList(begIndex, i);
		    LexAccessApiResult filtered = filterLexRecords(matchedTokens, curResult);
		    LexiconMatch l = new LexiconMatch(matchedTokens,
			    ToJavaObjApi.ToJavaObjsFromText(filtered.GetText()));
		    lexMatches.add(l);
		    begIndex = i;
		}
		lexBuf = new StringBuilder();
		curResult = null;
		continue;
	    }

	    LexAccessApiResult tempResult = null;
	    if (lexBuf.length() > 0)
		tempResult = lexAccessApi.GetLexRecords(lexBuf.toString().trim() + " " + tok);
	    else {
		tempResult = lexAccessApi.GetLexRecords(tok);
		begIndex = i;
	    }

	    if (tempResult.GetTotalRecordNumber() > 0) {
		curResult = tempResult;
	    } else { // If the lexicon info for the current word returns nothing
		if (lexBuf.length() > 0) { // If there is previous matched lexicon, return those
		    if (curResult != null) {
			List<TokenInfo> matchedTokens = tokens.subList(begIndex, i);
			LexAccessApiResult filtered = filterLexRecords(matchedTokens, curResult);
			LexiconMatch l = new LexiconMatch(matchedTokens,
				ToJavaObjApi.ToJavaObjsFromText(filtered.GetText()));
			lexMatches.add(l);
		    }
		    begIndex = i;
		    curResult = lexAccessApi.GetLexRecords(tok);
		} else {
		    curResult = null;
		}
		lexBuf = new StringBuilder();
	    }
	    lexBuf.append(tok + " ");
	}

	if (lexBuf.length() > 0) { // remaining tokens
	    if (curResult != null) {
		List<TokenInfo> matchedTokens = tokens.subList(begIndex, tokenList.size());
		LexAccessApiResult filtered = filterLexRecords(matchedTokens, curResult);
		LexiconMatch l = new LexiconMatch(matchedTokens, ToJavaObjApi.ToJavaObjsFromText(filtered.GetText()));
		lexMatches.add(l);
	    }
	}
	return lexMatches;
    }

    private LexAccessApiResult filterLexRecords(List<TokenInfo> match, LexAccessApiResult in) {
	Vector<LexRecord> out = new Vector<>();
	Vector<LexRecord> inrecs = in.GetJavaObjs();
	if (match.size() == 1) {
	    TokenInfo m = match.get(0);
	    String s = m.getToken();
	    // don't consider acronyms of prepositions
	    /*
	     * if (StringUtils.isAllUpperCase(w.getText()) == false &&
	     * (w.getPos().equals("IN") || w.getPos().equals("TO"))) { for (LexRecord r:
	     * inrecs) { if (r.GetCategory().equals("prep") == false) continue; out.add(r);
	     * } } else return in;
	     */
	    // this is the place for filtering out unwanted lexical entries -- can be expanded
	    if (s.toLowerCase().equals("lower")) {
		for (LexRecord r : inrecs) {
		    if (r.GetBase().equals("lour"))
			continue;
		    out.add(r);
		}
	    } else {
		return in;
	    }
	} else
	    return in;

	LexAccessApiResult o = new LexAccessApiResult();
	o.SetJavaObjs(out);
	if (out.size() == 0) {
	    log.warning("No  lexical record for " + match);
	    log.warning(in.GetText());
	}
	return o;
    }

    /**
     * Given a list of lexical matches, filter out those where the POS tag is not
     * compatible with the tag found through POS tagging.
     * 
     * @param lexmatches
     *            The list of lexical matches to check
     * @return the subset of the input that is compatible POS-wise
     */
    public List<LexiconMatch> filterLexMatchesByPOS(List<LexiconMatch> lexmatches) {
	List<LexiconMatch> out = new ArrayList<>();
	for (LexiconMatch match : lexmatches) {
	    List<LexRecord> lexrecs = match.getLexRecords();
	    List<TokenInfo> tokens = match.getMatch();
	    if (tokens.size() == 1) {
		TokenInfo token = tokens.get(0);
		String pos = token.getPos();
		List<String> possiblePos = POS_TRANSLATION.get(pos);
		List<LexRecord> upd = new ArrayList<>();
		for (LexRecord r : lexrecs) {
		    if (possiblePos != null && possiblePos.contains(r.GetCategory()) == false)
			continue;
		    upd.add(r);
		}
		LexiconMatch lm = new LexiconMatch(tokens, upd);
		out.add(lm);
	    } else
		out.add(match);
	}
	for (LexiconMatch match : out) {
	    log.finest(match.toString());
	}
	return out;
    }
}
