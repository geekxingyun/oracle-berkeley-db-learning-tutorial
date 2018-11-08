package util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.EnvironmentNotFoundException;
import com.sleepycat.je.VersionMismatchException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.StoreExistsException;
import com.sleepycat.persist.StoreNotFoundException;
import com.sleepycat.persist.evolve.IncompatibleClassException;

/**
 * Berkeley Database Java Edition 环境管理器
 * 
 * @author fairy
 */
public class BDBEnvironmentManager {

	private final static Logger logger = LoggerFactory.getLogger(BDBEnvironmentManager.class);

	private static volatile BDBEnvironmentManager bdbEnvironmentManager = null;

	// 数据库环境对象
	private static Environment myEnvironment = null;
	// 数据存储基本单元
	private static EntityStore myEntityStore = null;

	// 空的构造器
	private BDBEnvironmentManager() {
	}

	/**
	 * @return
	 */
	public static Environment getMyEnvironment() {
		if (myEnvironment != null) {
			return myEnvironment;
		} else {
			return null;
		}
	}

	/**
	 * @return
	 */
	public static EntityStore getMyEntityStore() {
		if (myEntityStore != null) {
			return myEntityStore;
		} else {
			return null;
		}
	}

	// 懒汉式
	/**
	 * @param envHome
	 * @param readOnly
	 * @return
	 */
	public static BDBEnvironmentManager getInstance(File envHome, Boolean readOnly) {

		if (envHome == null) {
			return null;
		}
		if (bdbEnvironmentManager == null) {
			// 添加同步锁,会更安全高效
			synchronized (BDBEnvironmentManager.class) {
				if (bdbEnvironmentManager == null) {

					// 代码在这里执行确保应用程序中只有一个实例
					bdbEnvironmentManager = new BDBEnvironmentManager();
					// 创建一个BDB 环境配置对象
					EnvironmentConfig myEnvConfig = new EnvironmentConfig();
					// 创建一个数据存储配置对象
					StoreConfig myStoreConfig = new StoreConfig();

					if (readOnly == null) {
						readOnly = false;
					}

					// 设置该环境是否为只读,true 为只读，false 为可读写
					myEnvConfig.setReadOnly(readOnly);
					// 设置数据存储配置是否为只读,true 为只读，false 为可读写
					myStoreConfig.setReadOnly(readOnly);

					// 如果该环境不存在是否重建，true 允许重建，false 不可重建
					myEnvConfig.setAllowCreate(!readOnly);
					// 如果该存储配置不存在是否重建，true 允许重建，false 不可重建
					myStoreConfig.setAllowCreate(!readOnly);
					
					//数据库环境是否支持事务
					myEnvConfig.setTransactional(!readOnly);
					//存储环境是否支持事务
					myStoreConfig.setTransactional(!readOnly);
   

					// 如果文件不存在则创建
					if (!envHome.exists()) {
						try {
							envHome.mkdir();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					// 打开 environment 和 entity store
					if ( myEnvironment == null || myEntityStore == null) {
						try {
							myEnvironment = new Environment(envHome, myEnvConfig);
							myEntityStore = new EntityStore(myEnvironment, "EntityStore", myStoreConfig);
						} catch (EnvironmentNotFoundException e) {
							// TODO Auto-generated catch block
							logger.error("EnvironmentNotFoundException----",e.toString());
						} catch (EnvironmentLockedException e) {
							// TODO Auto-generated catch block
							logger.error("EnvironmentLockedException----",e.toString());
						} catch (VersionMismatchException e) {
							// TODO Auto-generated catch block
							logger.error("VersionMismatchException----",e.toString());
						} catch (StoreExistsException e) {
							// TODO Auto-generated catch block
							logger.error("StoreExistsException----",e.toString());
						} catch (StoreNotFoundException e) {
							// TODO Auto-generated catch block
							logger.error("StoreNotFoundException----",e.toString());
						} catch (IncompatibleClassException e) {
							// TODO Auto-generated catch block
							logger.error("IncompatibleClassException----",e.toString());
						} catch (DatabaseException e) {
							// TODO Auto-generated catch block
							logger.error("DatabaseException----",e.toString());
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							logger.error("IllegalArgumentException----",e.toString());
						}
					}
				}
			}
		}
		return bdbEnvironmentManager;
	}

	// Close the store and environment.
	/**
	 * 
	 */
	public static void close() {
		// 判断存储对象是否为空
		if (myEntityStore != null) {
			try {
				// 尝试关闭存储对象
				myEntityStore.close();
			} catch (DatabaseException dbe) {
				logger.error("Error closing store: " + dbe.toString());
			}
		}
		// 判断环境是否为空
		if (myEnvironment != null) {
			try {
				// 关闭环境
				myEnvironment.close();
			} catch (DatabaseException dbe) {
				logger.error("Error DatabaseException: " + dbe.toString());
			}
		}
	}
}
