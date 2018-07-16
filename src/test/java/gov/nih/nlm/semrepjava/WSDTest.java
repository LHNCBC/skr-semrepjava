package gov.nih.nlm.semrepjava;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ner.metamap.MetaMapLiteClient;
import gov.nih.nlm.ner.wsd.WSDClient;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WSDTest extends TestCase {
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
     * Rigourous Test :-)
     * @throws IOException 
     */
    public void testWSD() throws IOException
    {
    	MetaMapLiteClient client = new MetaMapLiteClient();
		Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
		Document doc = SemRep.processingFromText("0", "breast cancer");
		client.annotate(doc, System.getProperties(), annotations);
		WSDClient wsdClient = new WSDClient();
		wsdClient.disambiguate(doc, System.getProperties(), annotations);
        assertTrue( doc.getAllSemanticItems().size() != 0 );
    }
}
