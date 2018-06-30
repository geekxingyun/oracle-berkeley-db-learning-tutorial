#第3章 Direct Persistence Layer First Steps

内容目录

- Entity Stores （存储实体对象）
- Opening and Closing Environments and Stores （打开和关闭环境和存储对象）
- Persistent Objects （持久对象）
- Saving and Retrieving Data （保存和持久对象）

本章将指导您完成在您的应用程序中使用DPL所需的前几个步骤。 这些步骤包括：

- 按照[这篇博文](https://blog.csdn.net/hadues/article/details/80810845)打开您的environment
- 打开您的Entity Store
- 确定您想要在JE中存储的类作为持久类还是实体。

完成这些工作后，您可以将类写入JE数据库，从数据库中读取它们，将它们从数据库中删除，等等。 这些活动在本手册本部分后面的章节中介绍。

## 3.1 Entity Stores

Entity Store是您与DPL一起使用的基本存储单元。 也就是说，它是您想要存储在JE中的类的封装单元。 在实际情况下，它实际上与JE数据库交互，但DPL从底层的JE API提供了一个抽象层。 因此，Store 提供了一种简化的机制，通过它可以读取和写入存储的类。 通过使用Store，您可以访问比直接与数据库交互更简单的类，但这种简化访问的代价是降低了灵活性。

Entity Stores的配置与环境具有配置的方式相同。 

您可以使用StoreConfig对象来识别Entity属性。 

其中有一些方法可以让你声明：

- 如果Store在打开时不存在，则可以创建该Store。 使用StoreConfig.setAllowCreate（）方法来设置它。

- Store允许延迟写入。 使用StoreConfig.setDeferredWrite（）方法来设置它。 有关延迟写入数据库的一般信息，请参阅延迟写入数据库。

- 该Store是只读的。 使用StoreConfig.setReadOnly（）方法来设置它。

- 该Store支持事务。 使用StoreConfig.setTransactional（）方法来设置它。

编写JE事务性应用程序在Berkeley DB，Java Edition“事务处理入门指南”中进行了描述。

EntityStore对象还提供了用于检索有关Store的信息的方法，例如：

- Store的名字。 使用EntityStore.getStoreName（）方法来检索它。

- 处理打开Store的环境的句柄。 使用EntityStore.getEnvironment方法来检索此句柄。

您还可以使用EntityStore检索与存储中包含的给定类型的实体对象相关的所有主索引和二级索引。 请参阅使用索引获取更多信息。

## 3.2 Opening and Closing Environments and Stores

如[Database Environment](https://blog.csdn.net/hadues/article/details/80810845)中所述，Environment是JE数据库的封装单位。 它还提供了一个可以管理数据库之间通用活动的处理方法。

要使用EntityStore，您必须先打开一个环境，然后将该环境句柄提供给EntityStore构造函数。

例如，以下代码片段配置Environment和EntityStore，以便可以在不存在的情况下创建它们。 然后打开Environment和EntityStore。

<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class Study_DPL_001_OpenEnvironmentAndStore {
	
	// 配置数据库环境文件路径,
	private final static String BDB_DPL_001_ENV_HOME_FILE_PATH = "bdb_dpl_001_env_home";
	private final static File BDB_PDL_001_ENV_HOME_File = new File(BDB_DPL_001_ENV_HOME_FILE_PATH);
	public static void main(String[] args) {
		
		Environment myEnvironment=null;
		
		EnvironmentConfig myEnvironmentConfig=new EnvironmentConfig();
		myEnvironmentConfig.setAllowCreate(true);
		
		//文件目录不存在就自动重建
		if(!BDB_PDL_001_ENV_HOME_File.exists()) {
			BDB_PDL_001_ENV_HOME_File.mkdirs();
		}
		
		myEnvironment=new Environment(BDB_PDL_001_ENV_HOME_File,myEnvironmentConfig);
		
		StoreConfig myStoreConfig=new StoreConfig();
		myStoreConfig.setAllowCreate(true);
		
		EntityStore myEntityStore=new EntityStore(myEnvironment,"myStoreName", myStoreConfig);
		
		System.out.println("-------- ok---------------------------");
		
		//关闭存储单元
		if (myEntityStore != null) {
		    try {
		    	myEntityStore.close();
		    } catch(DatabaseException dbe) {
		        System.err.println("Error closing store: " +
		                            dbe.toString());
		        System.exit(-1);
		    }
		}

		if (myEnvironment != null) {
		    try {
		        //清理日志
		    	myEnvironment.cleanLog();
		    	// Finally, close environment.
		        myEnvironment.close();
		    } catch(DatabaseException dbe) {
		        System.err.println("Error closing MyDbEnv: " +
		                            dbe.toString());
		        System.exit(-1);
		    }
		} 
	}
}
</code>
</pre>
## 3.3 Persistent Objects 持久对象

使用DPL时，通过使对象持久化来将数据存储在底层JE数据库中。 您可以使用Java注解来执行此操作，这些注解既可以识别您正在声明的持久对象的类型，也可以识别主要和次要索引。

以下是您将用于DPL持久类的注释：

<table>

<th>
Annotation
</th>
<th>
Description
</th>

<tr>
<td>
@Entity
</td>
<td>
声明一个实体类; 即具有主索引和可选的一个或多个索引的类。
</td>
</tr>

<tr>
<td>
@Persistent
</td>
<td>
声明一个持久化类; 也就是一个实体类使用的类。 它们没有索引，而是在实体类直接使用它们时存储或检索。
</td>
</tr>

<tr>
<td>
@PrimaryKey
</td>
<td>
将实体类中的特定数据成员声明为该对象的主键。 每个实体类都必须使用此注解。
</td>
</tr>

<tr>
<td>
@SecondaryKey
</td>
<td>
声明实体类中的特定数据成员以作为该对象的辅助键。 此注解是可选的，可以多次用于实体类。
</td>
</tr>
</table>

<pre>
<code>
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class ExampleEntity {

	// The primary key must be unique in the database.
    @PrimaryKey
    private String aPrimaryKey;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String aSecondaryKey;

	public String getaPrimaryKey() {
		return aPrimaryKey;
	}

	public void setaPrimaryKey(String aPrimaryKey) {
		this.aPrimaryKey = aPrimaryKey;
	}

	public String getaSecondaryKey() {
		return aSecondaryKey;
	}

	public void setaSecondaryKey(String aSecondaryKey) {
		this.aSecondaryKey = aSecondaryKey;
	}
    
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return super.toString();
    }
}
</code>
</pre>

## 3.4 Saving and Retrieving Data

使用DPL存储的所有数据都有一个主索引和与其关联的零个或多个辅助索引。 （有时这些被称为主键和辅助键。）因此，要在DPL下存储数据，您必须：

- 声明一个类是一个实体类。

- 识别代表索引材料的类中的特征。

- 使用EntityStore.getPrimaryIndex（）方法检索给定类的Store主索引。

- 使用PrimaryIndex.put（）方法将类对象放入Store。

为了从Store中检索对象，您可以使用最适合您目的的索引。 这可能是主索引，也可能是您在实体类中声明的其他二级索引。

您获得的主索引与将对象放入存储时相同：使用EntityStore.getPrimaryIndex（）。 您可以使用EntityStore.getSecondaryIndex（）方法获得Store的二级索引。 请注意，getSecondaryIndex（）需要您在调用它时提供一个PrimaryIndex类实例，因此从实体存储中检索对象时总是需要一个类的主索引。

通常所有关于保存和检索数据的活动都是在专门用于此目的的一个或多个类中进行组织的。 我们在SimpleDA.class中描述这些数据访问器类的构造。 但在您执行任何Entity Store活动之前，您需要了解索引。 因此我们在下一章中描述它们。