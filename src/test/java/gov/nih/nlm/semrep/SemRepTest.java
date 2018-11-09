package gov.nih.nlm.semrep;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.sem.Predication;
import gov.nih.nlm.ling.sem.SemanticItem;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for SemRep 
 * 
 * @author Halil Kilicoglu
 */
public class SemRepTest 
    extends TestCase
{
	private static Logger log = Logger.getLogger(SemRepTest.class.getName());	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SemRepTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SemRepTest.class );
    }

    /**
     * @throws IOException 
     */
    public void testSemRep() throws IOException
    {
		SemRep.init();
		Document doc = SemRep.lexicoSyntacticAnalysis("0", "Aspirin treats headache.");
		SemRep.processForSemantics(doc);
		LinkedHashSet<SemanticItem> preds = Document.getSemanticItemsByClass(doc, Predication.class);
        assertTrue( preds.size() != 0 );
    }
    
}
