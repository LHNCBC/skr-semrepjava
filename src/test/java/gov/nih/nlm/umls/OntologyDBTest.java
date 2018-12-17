package gov.nih.nlm.umls;

import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OntologyDBTest 
extends TestCase
{
private static Logger log = Logger.getLogger(OntologyDBTest.class.getName());

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public OntologyDBTest( String testName )
	{
	    super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
	    return new TestSuite( OntologyDBTest.class );
	}



	/**
	 * @throws IOException 
	 */
	public void testOntologyDB() throws IOException
	{
		OntologyDatabase ontDB = new OntologyDatabase("ontologyDB", true);
		assertTrue(ontDB.contains("topp-uses-carb"));
	}

}