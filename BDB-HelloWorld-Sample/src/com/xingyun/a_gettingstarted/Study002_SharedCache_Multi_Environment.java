package com.xingyun.a_gettingstarted;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class Study002_SharedCache_Multi_Environment {

    	//配置数据库环境文件路径,
		private final static String BDB_002_01_ENV_HOME_FILE_PATH="bdb_002_01_env_home";
		private final static String BDB_002_02_ENV_HOME_FILE_PATH="bdb_002_02_env_home";
		private final static File BDB_002_01_ENV_HOME_File=new File(BDB_002_01_ENV_HOME_FILE_PATH);
		private final static File BDB_002_02_ENV_HOME_File=new File(BDB_002_02_ENV_HOME_FILE_PATH);
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Environment myEnv1 = null;
		Environment myEnv2 = null;
		
		try {
		    EnvironmentConfig envConfig = new EnvironmentConfig();
		    //如果环境不存在则重建
		    envConfig.setAllowCreate(true);
		    /**
		     * 为了配置环境以使用共享缓存，请将EnvironmentConfig.setSharedCache（）设置为true。
		     * 这必须针对您要使用共享缓存的进程中的每个环境进行设置。
		     * */
		    envConfig.setSharedCache(true);
		    
		    //如果环境文件路径不存在则创建
		    if(!BDB_002_01_ENV_HOME_File.exists()) {
		    	BDB_002_01_ENV_HOME_File.mkdirs();
		    }
		    if(!BDB_002_02_ENV_HOME_File.exists()) {
		    	BDB_002_02_ENV_HOME_File.mkdirs();
		    }

		    myEnv1 = new Environment(BDB_002_01_ENV_HOME_File, envConfig);
		    myEnv2 = new Environment(BDB_002_02_ENV_HOME_File, envConfig);
		    
		    System.out.println("------数据库配置文件开始---------");
		    System.out.println(myEnv1.getConfig());
		    System.out.println(myEnv2.getConfig());
		    System.out.println("------数据库配置文件结束---------");
		    
		} catch (DatabaseException dbe) {
		    // Exception handling goes here
		} 
	}
}
