package gov.nih.nlm.semrep.utils;

import java.sql.SQLException;
/**
 * Wrapper class for Lexicon
 * 
 * @author Dongwook Shin
 *
 */
import java.util.logging.Logger;

import gov.nih.nlm.ner.gnormplus.GNormPlusStringWrapper;
import gov.nih.nlm.nls.lexAccess.Api.LexAccessApi;
import gov.nih.nlm.nls.lexAccess.Api.LexAccessApiResult;
import gov.nih.nlm.nls.lexCheck.Api.ToJavaObjApi;
import gov.nih.nlm.nls.lexCheck.Lib.LexRecord;

public class LexiconWrapper {
    private static Logger log = Logger.getLogger(GNormPlusStringWrapper.class.getName());
    private static LexAccessApi lexAccessApi = null;
    private static LexiconWrapper lexWrapper = null;

    public static LexiconWrapper getInstance() {
	if (lexWrapper == null) {
	    lexWrapper = new LexiconWrapper();
	}
	return lexWrapper;
    }

    public static LexAccessApi initializeLexicon(String configFile) {
	if (lexAccessApi == null) {
	    log.info("Initializing a Lexicon instance...");
	    lexAccessApi = new LexAccessApi(configFile);
	}
	return lexAccessApi;
    }

    public static LexAccessApiResult getLexResult(String word) throws SQLException {
	LexAccessApiResult result = lexAccessApi.GetLexRecords(word);
	return result;
    }

    /*
     * findLexicon finds a longest matching lexicon from input Input: Words string
     * Output: LexiconOutput which consists of lexiconOutput, matched lexicon words
     * and remaining words
     */
    public static LexiconOutput findLexicon(String inputwords) throws SQLException {
	String[] wordArray = inputwords.split("\\s+");

	LexAccessApiResult curResult = null;
	LexiconOutput lexOut = new LexiconOutput();
	LexRecord lexRecord = new LexRecord();
	StringBuffer lexFoundBuffer = new StringBuffer();
	for (int index = 0; index < wordArray.length; index++) {
	    String word = wordArray[index];
	    LexAccessApiResult tempResult = null;
	    if (lexFoundBuffer.length() > 0) // If there are previous words that match with lexicon 
		tempResult = lexWrapper.getLexResult(lexFoundBuffer.toString() + " " + word);
	    else // If not, find the lexicon info for the current word
		tempResult = lexWrapper.getLexResult(word);

	    if (tempResult.GetTotalRecordNumber() > 0) {
		curResult = tempResult;
		if (lexFoundBuffer.length() > 0)
		    lexFoundBuffer.append(" " + word);
		else
		    lexFoundBuffer.append(word);
	    } else { // If the lexicon info for the current word returns nothing
		if (lexFoundBuffer.length() > 0) { // If there is previous matched lexicon, return those
		    lexOut.setLexRecords(ToJavaObjApi.ToJavaObjsFromText(curResult.GetText()));
		    // ToJavaObjApi toJava = new ToJavaObjApi();
		    lexRecord = ToJavaObjApi.ToJavaObjFromText(curResult.GetText());
		    lexOut.matchedString = lexFoundBuffer.toString();
		    StringBuffer remainingBuffer = new StringBuffer();
		    for (int i = index; i < wordArray.length; i++)
			remainingBuffer.append(wordArray[i] + " ");

		    lexOut.remainingString = remainingBuffer.toString().trim();
		    lexFoundBuffer = new StringBuffer();
		    break;
		} else {
		    continue;
		}
	    }
	}
	/*
	 * If there is still left-out matched lexicon at the end of input string, return
	 * the lexicon and the remainingString needs to be empty
	 */
	if (lexFoundBuffer.length() > 0) { // return those and
	    lexOut.setLexRecords(ToJavaObjApi.ToJavaObjsFromText(curResult.GetText()));
	    // ToJavaObjApi toJava = new ToJavaObjApi();
	    lexRecord = ToJavaObjApi.ToJavaObjFromText(curResult.GetText());
	    lexOut.matchedString = lexFoundBuffer.toString();
	    lexOut.remainingString = new String("");

	}
	return lexOut;
    }

    public static void main(String[] argv) {
	try {
	    LexiconWrapper lexWrapper = LexiconWrapper.getInstance();
	    LexAccessApi lexAccess = lexWrapper.initializeLexicon("lexAccess.properties");
	    // String input = new String("Aspirin is treatment for headache");
	    String input = new String("kkkkkkk");
	    String compo[] = input.split(" ");
	    for (int i = 0; i < compo.length; i++) {
		System.out.println("input: " + compo[i]);
		System.out.println("---------- start of lexicon output ----- ");
		LexAccessApiResult result = lexWrapper.getLexResult(compo[i]);
		if (result.GetTotalRecordNumber() > 0) {
		    System.out.println("output = " + result.GetText());
		}
		System.out.println("----------- end of lexicon output ------ \n");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
