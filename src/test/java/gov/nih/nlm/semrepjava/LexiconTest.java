package gov.nih.nlm.semrepjava;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Logger;

import gov.nih.nlm.nls.lexAccess.Api.LexAccessApi;
import gov.nih.nlm.nls.lexCheck.Lib.LexRecord;
import gov.nih.nlm.semrep.utils.LexiconOutput;
import gov.nih.nlm.semrep.utils.LexiconWrapper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the LexiconWrapper.
 * 
 * @author Dongwook Shin
 *
 */

public class LexiconTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    private static Logger log = Logger.getLogger(LexiconTest.class.getName());

    public LexiconTest(String testName) {
	super(testName);

    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
	return new TestSuite(LexiconTest.class);
    }

    /**
     * Rigourous Test :-)
     * 
     * @throws IOException
     */
    public void testLexicon() throws IOException, SQLException {

	LexiconWrapper lexWrapper = LexiconWrapper.getInstance();
	LexAccessApi lexAccess = lexWrapper.initializeLexicon("lexAccess.properties");
	// LexiconOutput result = lexWrapper.findLexicon("breast cancer is curable");
	// LexiconOutput result = lexWrapper.findLexicon("cancer is treatable.");
	LexiconOutput result = lexWrapper.findLexicon("treat");
	Vector<LexRecord> lexRecords = result.getLexRecords();
	for (LexRecord lexRecord : lexRecords) {
	    log.info("base: " + lexRecord.GetBase() + ", category: " + lexRecord.GetCategory() + "variant: "
		    + lexRecord.GetVariants());
	}

	assertTrue(lexRecords.size() == 2);
    }
}
