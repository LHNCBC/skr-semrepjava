package gov.nih.nlm.semrep.utils;

import java.sql.SQLException;
import java.util.logging.Logger;

import gov.nih.nlm.ner.gnormplus.GNormPlusStringWrapper;
import gov.nih.nlm.nls.lexAccess.Api.LexAccessApi;
import gov.nih.nlm.nls.lexAccess.Api.LexAccessApiResult;

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

    public static void main(String[] argv) {
	try {
	    LexiconWrapper lexWrapper = LexiconWrapper.getInstance();
	    LexAccessApi lexAccess = lexWrapper.initializeLexicon("lexAccess.properties");
	    String input = new String("Aspirin is treatment for headache");
	    String compo[] = input.split(" ");
	    for (int i = 0; i < compo.length; i++) {
		System.out.println("input: " + compo[i]);
		System.out.println("---------- start of lexicon output ----- ");
		LexAccessApiResult result = lexWrapper.getLexResult(compo[i]);
		System.out.println(result.GetText());
		System.out.println("----------- end of lexicon output ------ \n");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
