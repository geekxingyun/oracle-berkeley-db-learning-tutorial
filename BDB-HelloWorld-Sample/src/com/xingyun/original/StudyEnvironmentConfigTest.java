package com.xingyun.original;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class StudyEnvironmentConfigTest {

	// 配置数据库环境文件路径,
	private final static String BDB_ORGINAL_ENV_HOME_FILE_PATH = "original_env_home";
	private final static File BDB_ORGINAL_ENV_HOME_FILE = new File(BDB_ORGINAL_ENV_HOME_FILE_PATH);
	// 配置数据库文件名称
	private final static String BDB_FILE_NAME = "admin.db";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// -----------------------数据库环境----------------------------
		// 创建数据库环境引用
		Environment myEnvironment = null;

		// -----------------------数据库环境配置----------------------------
		// 创建数据库环境配置对象
		EnvironmentConfig myEnvironmentConfig = new EnvironmentConfig();
		// 以只读方式打开,默认为false
		myEnvironmentConfig.setReadOnly(false);
		// 设置该环境支持事务
		myEnvironmentConfig.setTransactional(true);
		// 当环境不存在的时候自动创建环境,默认为false
		myEnvironmentConfig.setAllowCreate(true);

		// 环境路径不存在则创建
		if (!BDB_ORGINAL_ENV_HOME_FILE.exists()) {
			BDB_ORGINAL_ENV_HOME_FILE.mkdirs();
		}

		// 创建数据库环境对象并加载数据库环境配置
		myEnvironment = new Environment(BDB_ORGINAL_ENV_HOME_FILE, myEnvironmentConfig);

		// 创建数据库环境多配置对象
		EnvironmentMutableConfig myEnvironmentMutableConfig = new EnvironmentMutableConfig();
		// 当提交事务的时候是否把缓存中的内容同步到磁盘中去。true 表示不同步，也就是说不写磁盘
		myEnvironmentMutableConfig.setTxnNoSyncVoid(false);
		// 当提交事务的时候，是否把缓冲的log写到磁盘上 true 表示不同步，也就是说不写磁盘
		myEnvironmentMutableConfig.setTxnWriteNoSyncVoid(false);
		// 设置当前环境能够使用的RAM占整个JVM内存的百分比
		myEnvironmentMutableConfig.setCachePercent(90);
		// 设置当前环境能够使用的最大RAM。单位BYTE
		myEnvironmentMutableConfig.setCacheSize(98304);
		
		myEnvironment.setMutableConfig(myEnvironmentMutableConfig);

		// 创建数据库配置对象
		DatabaseConfig myDatabaseConfig = new DatabaseConfig();
		// 设置该数据库支持事务
		myDatabaseConfig.setTransactional(true);
		// 如果数据库不存在就创建,默认为false
		myDatabaseConfig.setAllowCreate(true);
		// 以独占的方式打开，也就是说同一个时间只能有一实例打开这个database。
		myDatabaseConfig.setExclusiveCreate(false);
		// 只读方式打开,默认为false
		myDatabaseConfig.setReadOnly(false);
		
		// 创建数据库文件并加载数据库配置
		Database myDatabase = myEnvironment.openDatabase(null, BDB_FILE_NAME, myDatabaseConfig);
		
		System.out.println("-------------数据库环境配置信息Start---------------------------------");
		System.err.println("数据库环境配置信息:" + myEnvironment.getConfig());
		System.err.println("返回当前环境下的数据库列表:" + myEnvironment.getDatabaseNames());
		System.out.println("-------------数据库环境配置信息end---------------------------------");

		// 将aKey 和aData 放入数据库
		try {
			String aKey = "myFirstKey";
			String aData = "myFirstData";
			DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry(aData.getBytes("UTF-8"));
			myDatabase.put(null, theKey, theData);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 根据aKey 查找aData
		String aKey = "myFirstKey";
		try {
			DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry();

			if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				byte[] retData = theData.getData();
				String foundData = new String(retData, "UTF-8");
				System.out.println("For key: '" + aKey + "' found data: '" + foundData + "'.");
			} else {
				System.out.println("No record found for key '" + aKey + "'.");
			}
		} catch (Exception e) {
			// Exception handling goes here
			e.printStackTrace();
			System.out.println("未知异常");
		}

		System.out.println("-------------数据库的配置信息Start---------------------------------");
		System.err.println(myDatabase.getConfig());
		System.out.println("取得数据库的名称:" + myDatabase.getDatabaseName());
		System.out.println("取得包含这个database的环境信息:" + myDatabase.getEnvironment());
		System.out.println("-------------数据库的配置信息end---------------------------------");

		// 如果数据库不为空
		if (myDatabase != null) {
			// 关闭数据库
			myDatabase.close();
		}
		// 关闭数据库环境
		if (myEnvironment != null) {
			// 在关闭环境前清理下日志
			myEnvironment.cleanLog();
			// 关闭数据库 环境
			myEnvironment.close();
		}
	}
}
