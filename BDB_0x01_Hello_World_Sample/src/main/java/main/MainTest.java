package main;

import java.io.File;
import java.util.List;

import dao.UserDA;
import model.User;
import util.BDBEnvironmentManager;

public class MainTest {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//打开数据库和存储环境
		BDBEnvironmentManager.getInstance(new File("bdb"),false);
		
		UserDA userDA=new UserDA(BDBEnvironmentManager.getMyEntityStore());
		
		userDA.saveUser(new User("A","root1"));
		userDA.saveUser(new User("admin","root2"));
		userDA.saveUser(new User("A","root3"));
		userDA.saveUser(new User("admin","root4"));
		
		System.out.println(userDA.findAllUserCount());
		System.out.println(userDA.findAllUserByUserNameCount("admin"));
		
		printAllDataByUserName(userDA,"admin");
		
		printAllData(userDA);
		
		userDA.removedUserById(2L);
		
		printAllData(userDA);
		
		userDA.removedUserByUserName("admin");
		
		printAllData(userDA);
		
		userDA.saveUser(new User(1L,"admin","root1"));
		
		printAllData(userDA);
		
		BDBEnvironmentManager.getMyEnvironment().sync();
		
//		BDBEnvironmentManager.close();
	}
	
	private static void printAllData(UserDA userDA) {
		// TODO Auto-generated method stub
		System.out.println("------start--------");
		 List<User> userList=userDA.findAllUser();
		for (User user : userList) {
			    System.out.println(user.getUserId());
				System.out.println(user.getUserName());
				System.out.println(user.getPassword());
		}
		System.out.println("------end--------");
	}
	
	private static void printAllDataByUserName(UserDA userDA,String userName) {
		// TODO Auto-generated method stub
		System.out.println("------start--------");
		 List<User> userList=userDA.findAllUserByUserName(userName);
		for (User user : userList) {
			    System.out.println(user.getUserId());
				System.out.println(user.getUserName());
				System.out.println(user.getPassword());
		}
		System.out.println("------end--------");
	}
}
