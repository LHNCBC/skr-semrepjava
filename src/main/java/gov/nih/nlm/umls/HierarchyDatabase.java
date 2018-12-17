package gov.nih.nlm.umls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import gov.nih.nlm.umls.lexicon.LexiconWrapper;

/**
 * Class that provides access to UMLS hierarchical relation file.
 * 
 * @author Zeshan Peng
 *
 */
public class HierarchyDatabase
{
    private static Logger log = Logger.getLogger(HierarchyDatabase.class.getName());
    
    private Environment env;
    private static final String HIERARCHY_DATABASE = "hierarchy_database";
    private Database hierarchyDb;
    
    public HierarchyDatabase(String homeDirectory, boolean query)
    {
    	log.info("Opening BerkeleyDB instance in: " + homeDirectory);

        EnvironmentConfig envConfig = new EnvironmentConfig();
        if(query) {
        	envConfig.setReadOnly(true);
    	}else {
        	envConfig.setReadOnly(false);
        	envConfig.setTransactional(false);
            envConfig.setAllowCreate(true);
        }
        //envConfig.setCacheSize(3* 1024 * 1024);
        //envConfig.setCachePercent(90);
        //envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_CLEANER,  "false");
        //envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_CHECKPOINTER, "false");
        //envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_IN_COMPRESSOR, "false");
        
        try {
        	env = new Environment(new File(homeDirectory), envConfig);
        }catch(DatabaseException e) {
        	log.severe("Unable to open UMLS concept hierarchy DB.");
        	e.printStackTrace();
        }
        DatabaseConfig dbConfig = new DatabaseConfig();
        if(query)
        	dbConfig.setReadOnly(true);
        else {
        	dbConfig.setReadOnly(false);
        	dbConfig.setTransactional(false);
            dbConfig.setAllowCreate(true);
            dbConfig.setKeyPrefixing(true);
        }
        
        hierarchyDb = env.openDatabase(null, HIERARCHY_DATABASE, dbConfig);
    }

    public void close()
        throws DatabaseException
    {
    	hierarchyDb.close();
    	env.close();
    }
    
    public final Environment getEnvironment()
    {
        return env;
    }
    
    public final Database getHierarchyDatabase()
    {
        return hierarchyDb;
    }
    
    public void putDataIntoDatabase(String filename) throws IOException {
    	BufferedReader in = new BufferedReader(new FileReader(filename));
       log.info("Opened file " + filename + " for reading.");
        String line = new String();
        byte[] bytes;
        DatabaseEntry entry;
        int i = 0;
        String empty = "";
        while ( (line = in.readLine()) != null)
        {
        	//System.out.println("Processing: " + line);
        	i++;
        	bytes = line.getBytes();
        	entry = new DatabaseEntry(bytes);
        	hierarchyDb.put(null, entry, new DatabaseEntry(empty.getBytes()));
        }
        in.close();
        System.out.println(i);
    }
    
    public boolean contains(String key) {
    	DatabaseEntry entry = new DatabaseEntry(key.getBytes());
    	String empty = "";
    	OperationStatus status = hierarchyDb.get(null, entry, new DatabaseEntry(empty.getBytes()), LockMode.DEFAULT);
    	if (status == OperationStatus.SUCCESS)
    		return true;
    	return false;
    }
}
