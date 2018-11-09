package gov.nih.nlm.semrep.utils;

import java.util.Vector;

import gov.nih.nlm.nls.lexCheck.Lib.LexRecord;

/**
 * An output class for Lexicon
 * 
 * @author Dongwook Shin
 *
 */

public class LexiconOutput {
    Vector<LexRecord> lexRecords;
    String matchedString;
    String remainingString;

    public void setLexRecords(Vector<LexRecord> records) {
	this.lexRecords = records;
    }

    public Vector<LexRecord> getLexRecords() {
	return this.lexRecords;
    }

    public void setMatchedString(String instring) {
	this.matchedString = instring;
    }

    public String getMatchedString() {
	return this.matchedString;
    }

    public void setRemainingString(String instring) {
	this.remainingString = instring;
    }

    public String getRemainingString() {
	return this.remainingString;
    }
}
