package com.xingyun.a_gettingstarted;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class Study004_DatabaseEnvironmentManagement {

	private Environment myEnv;

	// 我们使用类构造函数来实例化EnvironmentConfig对象，该对象用于在打开它时配置我们的环境。
	public Study004_DatabaseEnvironmentManagement() {
	}

	public void setup(File envHome, boolean readOnly) throws DatabaseException {

		// Instantiate an environment configuration object
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();

		// 配置是否只读
		myEnvConfig.setReadOnly(readOnly);
		// 如果为true，则数据库环境在打开时创建。
		// 如果为false，则如果环境不存在，则环境打开失败。
		// 如果数据库环境已存在，则此属性没有意义。 默认为false。
		myEnvConfig.setAllowCreate(!readOnly);
		// 如果为true，则配置数据库环境以支持事务。 默认为false。
		myEnvConfig.setTransactional(!readOnly);

		// Instantiate the Environment. This opens it and also possibly
		// creates it.
		myEnv = new Environment(envHome, myEnvConfig);
	}

	// Getter methods
	// 接下来我们提供一个getter方法，它允许我们直接检索环境。 这是本指南后面的例子所需要的。
	public Environment getEnv() {
		return myEnv;
	}

	// Close the environment
	// 最后，我们需要一种方法来关闭我们的环境。 我们将这个操作封装在一个try块中，以便它可以在finally语句中正常使用。
	public void close() {
		if (myEnv != null) {
			try {
				myEnv.cleanLog();
				myEnv.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing environment" + dbe.toString());
			}
		}
	}
}
