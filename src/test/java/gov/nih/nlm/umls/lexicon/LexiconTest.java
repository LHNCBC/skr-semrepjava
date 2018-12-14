package gov.nih.nlm.umls.lexicon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.nls.lexCheck.Lib.LexRecord;
import gov.nih.nlm.semrep.SemRep;
import gov.nih.nlm.semrep.core.SRSentence;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for access to UMLS Specialist Lexicon.
 * 
 * @author Dongwook Shin
 * @author Halil Kilicoglu
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

    public void testLexicon() throws IOException, SQLException {
    	SemRep.initLogging();
		Document doc = new Document("00000000","treat");
		SemRep.lexicalSyntacticAnalysis(doc);
	   SRSentence sent = (SRSentence)doc.getSentences().get(0);
	   List<LexiconMatch> matches = sent.getLexicalItems();
	   assertTrue(matches.size() ==1);
	   List<LexRecord> lexRecords =matches.get(0).getLexRecords();
		for (LexRecord lexRecord : lexRecords) {
		    log.info("base: " + lexRecord.GetBase() + ", category: " + lexRecord.GetCategory() + ", variant: "
			    + lexRecord.GetVariants());
		}
		assertTrue(lexRecords.size() == 2);
    }
}
