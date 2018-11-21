package gov.nih.nlm.umls;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.sem.ImplicitRelation;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for hypernym processing
 * 
 * @author Zeshan Peng
 */
public class HypernymProcessingTest 
    extends TestCase
{
	private static Logger log = Logger.getLogger(HypernymProcessingTest.class.getName());
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HypernymProcessingTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HypernymProcessingTest.class );
    }
    


    /**
     * @throws IOException 
     */
    public void testHypernymProcessing() throws IOException
    {
    	SemRep.initLogging();
    	Document doc = SemRep.lexicoSyntacticAnalysis("0", "the analgesic aspirin");
    	SemRep.processForSemantics(doc);
    	LinkedHashSet<SemanticItem> seList = Document.getSemanticItemsByClass(doc, ImplicitRelation.class);
		assertTrue(seList.size() != 0);
    }
    
}

