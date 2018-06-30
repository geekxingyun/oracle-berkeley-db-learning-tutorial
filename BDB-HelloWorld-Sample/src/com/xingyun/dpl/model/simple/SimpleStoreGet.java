package com.xingyun.dpl.model.simple;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class SimpleStoreGet {
      
	 // main
    public static void main(String args[]) {
        SimpleStoreGet ssg = new SimpleStoreGet();
        try {
            ssg.run();
        } catch (DatabaseException dbe) {
            System.err.println("SimpleStoreGet: " + dbe.toString());
            dbe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        }
        System.out.println("All done.");
    }
    
	// 配置数据库环境文件路径,
	private final static String BDB_DPL_002_ENV_HOME_FILE_PATH = "bdb_dpl_002_env_home";
	private final static File BDB_PDL_002_ENV_HOME_File = new File(BDB_DPL_002_ENV_HOME_FILE_PATH);

	    private Environment envmnt;
	    private EntityStore store;
	    private SimpleDA sda; 
	    
	 // The setup() method opens the environment and store
	    // for us.
	    public void setup()
	        throws DatabaseException {

	        EnvironmentConfig envConfig = new EnvironmentConfig();
	        StoreConfig storeConfig = new StoreConfig();

	        envConfig.setAllowCreate(true);
	        storeConfig.setAllowCreate(true);
	        
	        if(!BDB_PDL_002_ENV_HOME_File.exists()) {
	        	BDB_PDL_002_ENV_HOME_File.mkdirs();
	        }

	        // Open the environment and entity store
	        envmnt = new Environment(BDB_PDL_002_ENV_HOME_File, envConfig);
	        store = new EntityStore(envmnt, "EntityStore", storeConfig);
	    } 
	    
	    //检索数据
	    // Retrieve some SimpleEntityClass objects from the store.
	    private void run()
	        throws DatabaseException {

	        setup();

	        // Open the data accessor. This is used to store
	        // persistent objects.
	        sda = new SimpleDA(store);

	        // Instantiate and store some entity classes
	        SimpleEntityClass sec1 = sda.pIdx.get("keyone");
	        SimpleEntityClass sec2 = sda.pIdx.get("keytwo");

	        SimpleEntityClass sec4 = sda.sIdx.get("skeythree");

	        System.out.println("sec1: " + sec1.getpKey());
	        System.out.println("sec2: " + sec2.getpKey());
	        System.out.println("sec4: " + sec4.getpKey());


	        shutdown();
	    } 
	    
	 // Close our environment and store.
	    public void shutdown()
	        throws DatabaseException {

	        store.close();
	        envmnt.close();
	    } 
}
