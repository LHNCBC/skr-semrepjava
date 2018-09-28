package gov.nih.nlm.semrepjava;

import java.io.IOException;
import java.sql.SQLException;

import gov.nih.nlm.nls.lexAccess.Api.LexAccessApi;
import gov.nih.nlm.nls.lexAccess.Api.LexAccessApiResult;
import gov.nih.nlm.semrep.utils.LexiconWrapper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LexiconTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
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
	LexAccessApiResult result = lexWrapper.getLexResult("cold");
	System.out.println(result.GetText());
    }
}
