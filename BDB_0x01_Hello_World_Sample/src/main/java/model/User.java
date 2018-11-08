package model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

//声明一个实体类; 即具有主索引和可选的一个或多个索引的类。
@Entity 
public class User {
	
	//主键 如果插入时不指定ID 就是插入,指定ID如果存在就插入已存在就是修改
	//通过指定要用于主键的序列来避免需要为类的主索引显式设置值。 这会导致一个唯一的整数值被用作每个存储对象的主键。
	@PrimaryKey(sequence = "ID")
	private Long userId;
	
	//辅助键
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	private String userName;
	private String password;
	
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public User(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
	}


	public User(Long userId, String userName, String password) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.password = password;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
