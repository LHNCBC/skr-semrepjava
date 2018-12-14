package gov.nih.nlm.umls.lexicon;

import java.util.List;

import gov.nih.nlm.nls.lexCheck.Lib.LexRecord;
import gov.nih.nlm.semrep.core.TokenInfo;

/**
 * A class that represents a match of a text fragment with a UMLS Specialist Lexicon record.
 * The match is many-to-many: a sequence of one or more tokens can map to a set of lexical records.
 * 
 * @author Halil Kilicoglu
 *
 */

public class LexiconMatch {
	List<TokenInfo> tokens; 
    List<LexRecord> lexRecords;
    
	public LexiconMatch(List<TokenInfo> tokens, List<LexRecord> lexRecords) {
		this.tokens = tokens;
		this.lexRecords = lexRecords;
	}

	public List<TokenInfo> getMatch() {
		return tokens;
	}

	public void setToken(List<TokenInfo> token) {
		this.tokens = token;
	}

	public List<LexRecord> getLexRecords() {
		return lexRecords;
	}

	public void setLexRecords(List<LexRecord> lexRecords) {
		this.lexRecords = lexRecords;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (TokenInfo t : tokens) {
			buf.append(t.toString() + " ");
		}
		buf.append("\n");
		for (LexRecord rec : lexRecords) {
			buf.append(rec.GetText());
			buf.append("\n");
		}
		return buf.toString().trim();
	}
}
