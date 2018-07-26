package gov.nih.nlm.semrepjava;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ner.metamap.MetaMapLiteClient;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for MetaMapLite server
 */
public class MetaMapLiteTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MetaMapLiteTest( String testName )
    {
        super( testName );
        
        
        
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MetaMapLiteTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws IOException 
     */
    public void testMetaMapLite() throws IOException
    {
    	MetaMapLiteClient client = new MetaMapLiteClient();
		Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
		Document doc = SemRep.processingFromText("0", "breast cancer");
		client.annotate(doc, System.getProperties(), annotations);
        assertTrue( annotations.size() != 0 );
    }
    
}
