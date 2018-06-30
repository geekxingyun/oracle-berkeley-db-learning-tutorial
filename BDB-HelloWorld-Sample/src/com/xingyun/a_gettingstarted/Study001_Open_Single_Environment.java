package com.xingyun.a_gettingstarted;

import java.io.File;
import java.util.IllegalFormatCodePointException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Open the environment 打开数据库环境
 * 
 */
public class Study001_Open_Single_Environment {

	// 配置数据库环境文件路径,
	private final static String BDB_001_ENV_HOME_FILE_PATH = "bdb_001_env_home";
	private final static File BDB_001_ENV_HOME_File = new File(BDB_001_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 创建环境对象引用
		Environment myDbEnvironment = null;
		try {

			// 创建环境配置对象引用并实例化一个环境配置对象
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// 如果为true，则数据库环境在打开时创建。
			//如果为false，则如果环境不存在，则环境打开失败。 
			//如果数据库环境已存在，则此属性没有意义。 默认为false。
			envConfig.setAllowCreate(true);
			//如果为true，则配置数据库环境以支持事务。 默认为false。
			envConfig.setTransactional(true);

			// 如果该文件路径不存在则创建
			if (!BDB_001_ENV_HOME_File.exists()) {
				BDB_001_ENV_HOME_File.mkdirs();
			}

			// 通过实例化一个Environment对象来打开一个数据库环境。
			myDbEnvironment = new Environment(BDB_001_ENV_HOME_File, envConfig); // ( 设置数据库环境路径 , 加载环境配置对象)

			// 打印配置文件
			System.out.println("------数据库配置文件开始---------");
			System.out.println(myDbEnvironment.getConfig());
			System.out.println("-----数据库配置文件结束---------");

		} catch (DatabaseException dbe) {
			// Exception handling goes here
			dbe.printStackTrace();
			System.err.println(dbe.toString());
		}

		// 关闭数据库环境
		if (myDbEnvironment != null) {
			myDbEnvironment.cleanLog(); // Clean the log before closing
			myDbEnvironment.close();
		}
	}
}
