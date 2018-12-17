package gov.nih.nlm.umls;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.sem.ImplicitRelation;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nu.xom.ParsingException;

/**
 * Unit test for hypernym processing
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
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
    public void testHypernymProcessing() throws IOException, ParsingException
    {
    	SemRep.initLogging();
		Properties props = System.getProperties();
		Properties semrepProps = FileUtils.loadPropertiesFromFile("semrepjava.properties");
		props.putAll(semrepProps);
		System.setProperties(props);
		Document doc = new Document("00000000","the analgesic aspirin");
		SemRep.lexicalSyntacticAnalysis(doc);
//    	SemRep.processForSemantics(doc);
		SemRep.referentialAnalysis(doc);
		SemRep.hypernymAnalysis(doc);
    	LinkedHashSet<SemanticItem> seList = Document.getSemanticItemsByClass(doc, ImplicitRelation.class);
    	for (SemanticItem s: seList) {
    		ImplicitRelation r  = (ImplicitRelation)s;
    		log.info(r.toString());
    	}
		assertTrue(seList.size() != 0);
    }
    
}

