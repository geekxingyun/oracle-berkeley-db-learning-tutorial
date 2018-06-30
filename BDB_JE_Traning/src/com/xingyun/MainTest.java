package com.xingyun;

import java.io.File;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.xingyun.model.User;
import com.xingyun.util.BDBEnvironmentMangager;

public class MainTest {
	
	// 配置数据库环境文件路径,
	private final static String BDB_ENV_HOME_FILE_PATH = "bdb_env_home";
	//数据库环境文件
	private final static File BDB_ENV_HOME_File = new File(BDB_ENV_HOME_FILE_PATH);
	public static void main(String[] args) {
		
		BDBEnvironmentMangager bdbEnvironmentMangager=new BDBEnvironmentMangager();
		
		//如果数据库环境文件对象不存在
		if(!BDB_ENV_HOME_File.exists()) {
			//创建这个文件路径
		    BDB_ENV_HOME_File.mkdirs();
		}
		
		//数据库环境初始化操作
		bdbEnvironmentMangager.setup(BDB_ENV_HOME_File,false);
			
		//存放数据
		EntityStore entityStore=bdbEnvironmentMangager.getMyEntityStore();
		//封装对象
		User user=new User();
		user.setUserId(1L);
		user.setUserName("admin");
		user.setPassword("admin");
	
		//创建对象索引
		PrimaryIndex<Long,User> myUserData=entityStore.getPrimaryIndex(Long.class,User.class);  
		//存放对象
	    myUserData.put(user);
	    
	    //查询数据
	    User myUser=myUserData.get(1L);
	    
	    System.out.println(myUser.getUserId());
	    System.out.println(myUser.getUserName());
	    System.out.println(myUser.getPassword());
	   
	    //关闭BDB环境
	    bdbEnvironmentMangager.close();
	}
}
