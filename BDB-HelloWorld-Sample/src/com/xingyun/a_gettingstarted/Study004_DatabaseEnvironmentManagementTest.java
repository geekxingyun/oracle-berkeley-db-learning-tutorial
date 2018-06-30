package com.xingyun.a_gettingstarted;

import java.io.File;

import com.sleepycat.je.DatabaseException;

public class Study004_DatabaseEnvironmentManagementTest {

	// 配置数据库环境文件路径,
	private final static String BDB_004_ENV_HOME_FILE_PATH = "bdb_004_env_home";
	private final static File BDB_004_ENV_HOME_File = new File(BDB_004_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Study004_DatabaseEnvironmentManagement study004_DatabaseEnvironmentManagement = new Study004_DatabaseEnvironmentManagement();

		//如果数据库环境文件路径不存在则创建它
		if(!BDB_004_ENV_HOME_File.exists()) {
			BDB_004_ENV_HOME_File.mkdirs();
		}
		
		try {
			study004_DatabaseEnvironmentManagement.setup(BDB_004_ENV_HOME_File, false);
			
			//打印配置信息
			System.out.println("-----------------------Print  Environment Config start---------------------------------------");
			System.out.println(study004_DatabaseEnvironmentManagement.getEnv().getConfig());
			System.out.println("-----------------------Print  Environment Config end--------------------------------------");

		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			study004_DatabaseEnvironmentManagement.close();
		}
	}
}
