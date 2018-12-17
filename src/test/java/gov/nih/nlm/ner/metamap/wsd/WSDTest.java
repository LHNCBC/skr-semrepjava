package gov.nih.nlm.ner.metamap.wsd;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.ner.metamap.MetaMapLiteClient;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for word sense disambiguation
 * 
 * @author Halil Kilicoglu
 */
public class WSDTest extends TestCase {
	private static Logger log = Logger.getLogger(WSDTest.class.getName());	
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WSDTest( String testName )
    {
        super( testName );    
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( WSDTest.class );
    }

    /**
     * @throws IOException 
     */
    public void testWSD() throws IOException
    {
    	SemRep.initLogging();
    	Properties props = FileUtils.loadPropertiesFromFile("semrepjava.properties");
    	MetaMapLiteClient client = new MetaMapLiteClient(props);
		Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
		Document doc = new Document("00000000","cold");
		SemRep.lexicalSyntacticAnalysis(doc);
		client.annotate(doc, System.getProperties(), annotations);
		SpanList sp = new SpanList(0,4);
		log.info("Disambiguation result: " + doc.getText() + " " + sp.toString() + "\t" + annotations.get(sp).iterator().next());
		assertTrue(annotations.get(sp ).size() == 1);
    }
}
