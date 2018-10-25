package gov.nih.nlm.ner;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Concept;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the multi-threaded NER client.
 * 
 * @author Halil Kilicoglu
 *
 */
public class MultiThreadClientTest 
    extends TestCase
{
	private static Logger log = Logger.getLogger(MultiThreadClientTest.class.getName());	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MultiThreadClientTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MultiThreadClientTest.class );
    }

    /**
     * @throws IOException 
     */
    public void testMultiThreadClient() throws IOException
    {
    	SemRep.initLogging();
    	Properties props = FileUtils.loadPropertiesFromFile("semrepjava.properties");
    	MultiThreadClient client = new MultiThreadClient(props);
		Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
		Document doc = SemRep.lexicoSyntacticAnalysis("0", "breast cancer and BRCA1");
		client.annotate(doc, props, annotations);
    	Set<SpanList> set = annotations.keySet();
    	for (SpanList s1 : set) {
    	    Set<Ontology> onts = annotations.get(s1);
    	    for (Ontology ont : onts) {
    		   Concept gpc = (Concept) ont;
    		    log.info("Concept: "  + s1.toString() + "\t" + gpc.toString());
    		}    
    	}
        assertTrue( annotations.size() != 0 );
    }
}
