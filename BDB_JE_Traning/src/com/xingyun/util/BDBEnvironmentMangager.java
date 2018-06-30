package com.xingyun.util;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

/**
 *  Berkeley Database Java Edition 
 *  环境管理器
 * @author fairy
 * **/
public class BDBEnvironmentMangager {
	private Environment myEnvironment;
    private EntityStore myEntityStore;
    
    //空的构造器
    public BDBEnvironmentMangager() {}
    
    /**
     * 初始化BDB环境
     * */
    public void setup(File envHome, boolean readOnly)
            throws DatabaseException {

    	    //创建一个BDB 环境配置对象
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            //创建一个数据存储配置对象
            StoreConfig myStoreConfig = new StoreConfig();

            //设置该环境是否为只读,true 为只读，false 为可读写
            myEnvConfig.setReadOnly(readOnly);
          //设置数据存储配置是否为只读,true 为只读，false 为可读写
            myStoreConfig.setReadOnly(readOnly);

            //如果该环境不存在是否重建，true 允许重建，false 不可重建
            myEnvConfig.setAllowCreate(!readOnly);
            //如果该存储配置不存在是否重建，true 允许重建，false 不可重建
            myStoreConfig.setAllowCreate(!readOnly);

            // 打开 environment 和 entity store
            myEnvironment = new Environment(envHome, myEnvConfig);
            myEntityStore = new EntityStore(myEnvironment, "EntityStore", myStoreConfig);

        }
    
    // Close the store and environment.
    public void close() {
    	//判断存储对象是否为空
        if (myEntityStore != null) {
            try {
            	//尝试关闭存储对象
                myEntityStore.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing store: " +dbe.toString());
               System.exit(-1);
            }
        }
        //判断环境是否为空
        if (myEnvironment != null) {
            try {
                // 关闭环境
                myEnvironment.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing MyDbEnv: " + dbe.toString());
               System.exit(-1);
            }
        }
    }

    //Getter and Setter
	public Environment getMyEnvironment() {
		return myEnvironment;
	}

	public void setMyEnvironment(Environment myEnvironment) {
		this.myEnvironment = myEnvironment;
	}

	public EntityStore getMyEntityStore() {
		return myEntityStore;
	}

	public void setMyEntityStore(EntityStore myEntityStore) {
		this.myEntityStore = myEntityStore;
	}
}
