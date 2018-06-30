package com.xingyun.a_gettingstarted;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;

/**
 * 
 * 
 */
public class Study003_EnvironmentMutableConfig {

	// 配置数据库环境文件路径,
	private final static String BDB_003_ENV_HOME_FILE_PATH = "bdb_003_env_home";
	private final static File BDB_003_ENV_HOME_File = new File(BDB_003_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 创建环境对象引用
		Environment myDbEnvironment = null;
		try {

			// 创建环境配置对象引用并实例化一个环境配置对象
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// 如果为true，则数据库环境在打开时创建。
			// 如果为false，则如果环境不存在，则环境打开失败。
			// 如果数据库环境已存在，则此属性没有意义。 默认为false。
			envConfig.setAllowCreate(true);
			// 如果为true，则配置数据库环境以支持事务。 默认为false。
			envConfig.setTransactional(true);

			// 创建环境配置对象引用并实例化一个环境多配置对象
			EnvironmentMutableConfig envMultableConfig = new EnvironmentMutableConfig();

			// 确定由于事务提交而创建的更改记录是否写入磁盘上的后备日志文件。 值为true会导致数据不会刷新到磁盘。
			envMultableConfig.setTxnNoSyncVoid(false);
			// 确定日志是否在事务提交时被刷新（但日志仍然被写入）。
			// 通过将此值设置为true，与通过提交刷新日志相比，您可能获得更好的性能，但是您会因失去一些事务持久性保证而获得更好的性能。
			envMultableConfig.setTxnWriteNoSyncVoid(false);

			// 如果该文件路径不存在则创建
			if (!BDB_003_ENV_HOME_File.exists()) {
				BDB_003_ENV_HOME_File.mkdirs();
			}

			// 通过实例化一个Environment对象来打开一个数据库环境。
			myDbEnvironment = new Environment(BDB_003_ENV_HOME_File, envConfig); // ( 设置数据库环境路径 )
			myDbEnvironment.setMutableConfig(envMultableConfig);
			
			//Environment.getStats（）只能从应用程序的进程中获取统计信息
			//此统计信息返回缓存中不可用的数据库对象的请求总数。
			long cacheMisses = myDbEnvironment.getStats(null).getNCacheMiss();

			System.out.println(cacheMisses);
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
