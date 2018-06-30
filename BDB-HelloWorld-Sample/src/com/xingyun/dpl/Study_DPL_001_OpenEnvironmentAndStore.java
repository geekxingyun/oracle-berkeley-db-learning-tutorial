package com.xingyun.dpl;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class Study_DPL_001_OpenEnvironmentAndStore {
	
	// 配置数据库环境文件路径,
	private final static String BDB_DPL_001_ENV_HOME_FILE_PATH = "bdb_dpl_001_env_home";
	private final static File BDB_PDL_001_ENV_HOME_File = new File(BDB_DPL_001_ENV_HOME_FILE_PATH);
	public static void main(String[] args) {
		
		Environment myEnvironment=null;
		
		EnvironmentConfig myEnvironmentConfig=new EnvironmentConfig();
		myEnvironmentConfig.setAllowCreate(true);
		
		//文件目录不存在就自动重建
		if(!BDB_PDL_001_ENV_HOME_File.exists()) {
			BDB_PDL_001_ENV_HOME_File.mkdirs();
		}
		
		myEnvironment=new Environment(BDB_PDL_001_ENV_HOME_File,myEnvironmentConfig);
		
		StoreConfig myStoreConfig=new StoreConfig();
		myStoreConfig.setAllowCreate(true);
		
		EntityStore myEntityStore=new EntityStore(myEnvironment,"myStoreName", myStoreConfig);
		
		System.out.println("-------- ok---------------------------");
		
		//关闭存储单元
		if (myEntityStore != null) {
		    try {
		    	myEntityStore.close();
		    } catch(DatabaseException dbe) {
		        System.err.println("Error closing store: " +
		                            dbe.toString());
		        System.exit(-1);
		    }
		}

		if (myEnvironment != null) {
		    try {
		        //清理日志
		    	myEnvironment.cleanLog();
		    	// Finally, close environment.
		        myEnvironment.close();
		    } catch(DatabaseException dbe) {
		        System.err.println("Error closing MyDbEnv: " +
		                            dbe.toString());
		        System.exit(-1);
		    }
		} 
	}
}
