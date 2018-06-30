# 第5章 Saving and Retrieving Objects 保存和检索对象

要将对象存储在EntityStore中，您必须对类进行相应的注解，然后将其存储

<pre>
<code>
PrimaryIndex.put()
</code>
</pre>

为了从EntityStore中取出对象，可以使用PrimaryIndex或SecondaryIndex中的get（）方法，以最适合您的应用程序的方式为准。

在这两种情况下，如果您创建数据访问器类来组织索引，它会大大简化事情。

在接下来的几节中，我们将：

1.创建一个准备好存储在实体存储中的实体类。 这个类将同时声明一个主索引（必需）和一个辅助索引（这是可选的）。请参阅下一节的这个实现。

2.创建一个用于组织数据的数据访问器类。

有关此实现的信息，请参阅SimpleDA.class。

3.创建一个用于将对象放入实体商店的简单类。

请参阅SimpleDA.class的实现

4.创建另一个从我们的实体商店中检索对象的类。

有关此实现，请参阅从实体存储中检索对象。

## 5.1 A Simple Entity Class 一个简单的实体类

为了清楚起见，这个实体类像我们可以写的那样简单。 它只包含两个数据成员，这两个数据成员都是由简单的setter和getter方法设置和检索的。 除此之外，这个班级在设计上并没有特别的兴趣。

其实施如下

<pre>
<code>
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class SimpleEntityClass {

	@PrimaryKey
	private String pKey;//主键
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE)
	private String sKey;//二级索引

	public String getpKey() {
		return pKey;
	}

	public void setpKey(String pKey) {
		this.pKey = pKey;
	}

	public String getsKey() {
		return sKey;
	}

	public void setsKey(String sKey) {
		this.sKey = sKey;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
</code>
</pre>
## 5.2 SimpleDA.class 数据访问器类

如上所述，我们使用专门的数据访问器类来组织我们的主要和次要索引。 这个类存在的主要原因是为我们的实体类提供对所有使用的索引的方便访问（参见上一节，简单实体类）。

有关在DPL下检索主索引和二级索引的说明，请参阅使用索引

<pre>
<code>
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class SimpleDA {
	
	 // Index Accessors 索引访问器
    PrimaryIndex<String,SimpleEntityClass> pIdx;//一级索引
    SecondaryIndex<String,String,SimpleEntityClass> sIdx;//二级索引
	public SimpleDA(EntityStore entityStore) {
		// Primary key for SimpleEntityClass classes
        pIdx = entityStore.getPrimaryIndex(String.class, SimpleEntityClass.class);

        // Secondary key for SimpleEntityClass classes
        // Last field in the getSecondaryIndex() method must be
        // the name of a class member; in this case, an 
        // SimpleEntityClass.class data member.
        sIdx = entityStore.getSecondaryIndex(pIdx, String.class, "sKey");
	}
}
</code>
</pre>
## 5.3 Placing Objects in an Entity Store 将对象存放到Entity Store中

为了将对象放置在DPL Entity Store中，您必须：

1. 打开环境并存储。
2. 实例化对象。
3. 使用对象主索引的put（）方法将对象放入Store。

> 注意
> 
> 此方法的一个版本允许您为要插入的记录指定Time to Live值。 请参阅使用时间以获取更多信息。
> 
以下示例使用我们在SimpleDA.class中显示的SimpleDA类将SimpleEntityClass对象（请参阅简单实体类）放入EntityStore

<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class SimpleStorePut {

	/**
	 * 最后，为了完成我们的类，我们需要一个main（）方法，它只是简单地调用我们的run（）方法。
	 * */
	public static void main(String[] args) {
		SimpleStorePut ssp = new SimpleStorePut();
		try {
			ssp.run();
		} catch (DatabaseException dbe) {
			System.err.println("SimpleStorePut: " + dbe.toString());
			dbe.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception: " + e.toString());
			e.printStackTrace();
		}
		System.out.println("All done.");
	}

	// 配置数据库环境文件路径,
	private final static String BDB_DPL_002_ENV_HOME_FILE_PATH = "bdb_dpl_002_env_home";
	private final static File BDB_PDL_002_ENV_HOME_File = new File(BDB_DPL_002_ENV_HOME_FILE_PATH);

	private Environment envmnt;
	private EntityStore store;
	private SimpleDA sda;

	// 接下来我们创建一个方法，为我们打开我们的数据库环境和EntityStore。
	public void setup() throws DatabaseException {

		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();

		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		
		if(!BDB_PDL_002_ENV_HOME_File.exists()) {
			BDB_PDL_002_ENV_HOME_File.mkdirs();
		}

		// Open the environment and entity store
		envmnt = new Environment(BDB_PDL_002_ENV_HOME_File, envConfig);
		store = new EntityStore(envmnt, "EntityStore", storeConfig);
	}

	// 我们需要我一个方法关闭环境和Store
	// Close our environment and store.
	public void shutdown() throws DatabaseException {
		store.close();
		envmnt.close();
	}

	/**
	 * 现在我们需要创建一个方法来实际将对象写入我们的商店。
	 * 此方法创建一个SimpleDA对象（请参阅SimpleDA.class），我们将使用该对象来访问我们的索引。
	 * 然后我们实例化一系列SimpleEntityClass（请参阅简单实体类）对象，我们将在我们的商店中放置这些对象。
	 * 最后，我们使用我们的主索引（从SimpleDA类实例中获得）实际将这些对象放在我们的商店中。
	 **/
	// Populate the entity store
	private void run() {

		setup();

		// Open the data accessor. This is used to store
		// persistent objects.
		sda = new SimpleDA(store);

		// Instantiate and store some entity classes
		SimpleEntityClass sec1 = new SimpleEntityClass();
		SimpleEntityClass sec2 = new SimpleEntityClass();
		SimpleEntityClass sec3 = new SimpleEntityClass();
		SimpleEntityClass sec4 = new SimpleEntityClass();
		SimpleEntityClass sec5 = new SimpleEntityClass();

		sec1.setpKey("keyone");
		sec1.setsKey("skeyone");

		sec2.setpKey("keytwo");
		sec2.setsKey("skeyone");

		sec3.setpKey("keythree");
		sec3.setsKey("skeytwo");

		sec4.setpKey("keyfour");
		sec4.setsKey("skeythree");

		sec5.setpKey("keyfive");
		sec5.setsKey("skeyfour");

		sda.pIdx.put(sec1);
		sda.pIdx.put(sec2);
		sda.pIdx.put(sec3);
		sda.pIdx.put(sec4);
		sda.pIdx.put(sec5);

		shutdown();
	}
}
</code>
</pre>

## 5.4 Retrieving Objects from an Entity Store 从一个Entity Store中查找对象

<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class SimpleStoreGet {
      
	 // main
    public static void main(String args[]) {
        SimpleStoreGet ssg = new SimpleStoreGet();
        try {
            ssg.run();
        } catch (DatabaseException dbe) {
            System.err.println("SimpleStoreGet: " + dbe.toString());
            dbe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        }
        System.out.println("All done.");
    }
    
	// 配置数据库环境文件路径,
	private final static String BDB_DPL_002_ENV_HOME_FILE_PATH = "bdb_dpl_002_env_home";
	private final static File BDB_PDL_002_ENV_HOME_File = new File(BDB_DPL_002_ENV_HOME_FILE_PATH);

	    private Environment envmnt;
	    private EntityStore store;
	    private SimpleDA sda; 
	    
	 // The setup() method opens the environment and store
	    // for us.
	    public void setup()
	        throws DatabaseException {

	        EnvironmentConfig envConfig = new EnvironmentConfig();
	        StoreConfig storeConfig = new StoreConfig();

	        envConfig.setAllowCreate(true);
	        storeConfig.setAllowCreate(true);
	        
	        if(!BDB_PDL_002_ENV_HOME_File.exists()) {
	        	BDB_PDL_002_ENV_HOME_File.mkdirs();
	        }

	        // Open the environment and entity store
	        envmnt = new Environment(BDB_PDL_002_ENV_HOME_File, envConfig);
	        store = new EntityStore(envmnt, "EntityStore", storeConfig);
	    } 
	    
	    //检索数据
	    // Retrieve some SimpleEntityClass objects from the store.
	    private void run()
	        throws DatabaseException {

	        setup();

	        // Open the data accessor. This is used to store
	        // persistent objects.
	        sda = new SimpleDA(store);

	        // Instantiate and store some entity classes
	        SimpleEntityClass sec1 = sda.pIdx.get("keyone");
	        SimpleEntityClass sec2 = sda.pIdx.get("keytwo");

	        SimpleEntityClass sec4 = sda.sIdx.get("skeythree");

	        System.out.println("sec1: " + sec1.getpKey());
	        System.out.println("sec2: " + sec2.getpKey());
	        System.out.println("sec4: " + sec4.getpKey());


	        shutdown();
	    } 
	    
	 // Close our environment and store.
	    public void shutdown()
	        throws DatabaseException {

	        store.close();
	        envmnt.close();
	    } 
}
</code>
</pre>

## 5.5 Retrieving Multiple Objects 检索多个对象

可以遍历特定索引引用的每个对象。 例如，如果您想检查或修改特定主索引可访问的每个对象，则可能需要执行此操作。

另外，一些索引导致多个对象的检索。 例如，对于任何给定的键（也称为重复键），MANY_TO_ONE辅助索引可能会导致多个对象。 当出现这种情况时，您必须遍历所得到的一组对象，以便依次检查每个对象。

有两种方法可以迭代由索引返回的对象集合。 一种是使用标准Java Iterator，您可以使用EntityCursor获得该Iterator，然后您可以从PrimaryIndex获取该Iterator：

<pre>
<code>
PrimaryIndex<String,SimpleEntityClass> pi =
    store.getPrimaryIndex(String.class, SimpleEntityClass.class);
EntityCursor<SimpleEntityClass> pi_cursor = pi.entities();
try {
    Iterator<SimpleEntityClass> i = pi_cursor.iterator();
    while (i.hasNext()) {
        // Do something here
    }
} finally {
    // Always close the cursor
    pi_cursor.close();
} 
</code>
</pre>

或者，您可以使用Java“foreach”语句遍历对象集：

<pre>
<code>
PrimaryIndex<String,SimpleEntityClass> pi =
    store.getPrimaryIndex(String.class, SimpleEntityClass.class);
EntityCursor<SimpleEntityClass> pi_cursor = pi.entities();
try {
    for (SimpleEntityClass seci : pi_cursor) {
        // do something with each object "seci"
    }
// Always make sure the cursor is closed when we are done with it.
} finally {
    pi_cursor.close();
} 
</code>
</pre>

### 5.5.1 Cursor Initialization 游标初始化

当光标第一次打开时，它不会被定位到任何值; 也就是说，它没有被初始化。 大多数移动游标的EntityCursor方法都会将其初始化为第一个或最后一个对象，具体取决于操作是向前移动游标（所有下一个...方法）还是向后（所有prev ...）方法。

你也可以通过调用EntityCursor.first（）来强制一个游标，不管它是否被初始化，都返回第一个对象。 同样，您可以使用EntityCursor.last（）强制返回最后一个对象。

不移动游标的操作（如EntityCursor.current（）或EntityCursor.delete（））将在未初始化的游标上使用时引发IllegalStateException。

### 5.5.2 Working with Duplicate Keys 使用重复键

如果您有重复的辅助键，则可以使用SecondaryIndex.subIndex（）为它们返回一个EntityIndex类对象。然后，使用该对象的entities（）方法获取EntityCursor实例。

例如：

<pre>
<code>
PrimaryIndex<String,SimpleEntityClass> pi =
    store.getPrimaryIndex(String.class, SimpleEntityClass.class);

SecondaryIndex<String,String,SimpleEntityClass> si = 
    store.getSecondaryIndex(pi, String.class, "sKey");

EntityCursor<SimpleEntityClass> sec_cursor = 
    si.subIndex("skeyone").entities(); 

try {
for (SimpleEntityClass seci : sec_cursor) {
        // do something with each object "seci"
    }
// Always make sure the cursor is closed when we are done with it.
} finally {
    sec_cursor.close(); } 
</code>
</pre>

请注意，如果您使用重复键，则可以通过使用以下EntityCursor方法来控制游标迭代的工作方式：

- nextDup（）

使用与光标当前引用相同的键将光标移动到下一个对象。也就是说，这个方法返回下一个重复的对象。如果不存在这样的对象，则此方法返回null。

- prevDup（）

使用与光标当前引用相同的键将光标移动到前一个对象。也就是说，这个方法返回游标的对象集合中的前一个重复对象。如果不存在这样的对象，则此方法返回null。

- nextNoDup（）

将光标移到光标组中具有与光标当前所引用的键不同的键的下一个对象。也就是说，此方法将跳过所有重复的对象，并返回游标的对象集合中的下一个非重复对象。如果不存在这样的对象，则此方法返回null。

- prevNoDup（）

将光标移至光标组中具有与光标当前所引用的键不同的键的前一个对象。也就是说，此方法将跳过所有重复对象并返回游标的对象集中的前一个非重复对象。如果不存在这样的对象，则此方法返回null。

<pre>
<code>
PrimaryIndex<String,SimpleEntityClass> pi =
    store.getPrimaryIndex(String.class, SimpleEntityClass.class);

SecondaryIndex<String,String,SimpleEntityClass> si = 
    store.getSecondaryIndex(pi, String.class, "sKey");

EntityCursor<SimpleEntityClass> sec_cursor = 
    si.subIndex("skeyone").entities(); 

try {
    SimpleEntityClass sec;
    Iterator<SimpleEntityClass> i = sec_cursor.iterator();
    while (sec = i.nextNoDup() != null) {
        // Do something here
    }
// Always make sure the cursor is closed when we are done with it.
} finally {
    sec_cursor.close(); } 
</code>
</pre>

### 5.5.3 Key Ranges 主要范围

您可以通过在创建光标时指定范围来限制光标移动的范围。 光标永远不能放置在指定范围之外。

指定范围时，通过为每个范围提供布尔值来指示范围界限是包含范围还是排他性。 true表示所提供的边界是包含性的，而false表示它是唯一的。

您在调用PrimaryIndex.entities（）或SecondaryIndex.entities（）时提供此信息。 例如，假设你有一个由数字信息索引的类。 进一步假设您只想检查索引值为100 - 199的对象。然后（假设数字信息是主索引），可以按如下方式限制游标：

<pre>
<code>
EntityCursor<SomeEntityClass> cursor = 
    primaryIndex.entities(100, true, 200, false);

try {
    for (SomeEntityClass sec : cursor {
        // Do something here to objects ranged from 100 to 199
    }
// Always make sure the cursor is closed when we are done with it.
} finally {
    cursor.close(); } 
</code>
</pre>

## 5.6 Join Cursors 加入游标

如果您为实体对象设置了两个或多个二级索引，则可以基于多个二级索引值的交集来检索对象集。 您可以使用EntityJoin类来完成此操作。

例如，假设你有一个代表汽车的实体类。 在这种情况下，您可能会存储有关汽车的信息，例如颜色，车门数量，燃油里程，汽车类型，乘客人数，品牌，型号和年份等等。

如果您根据此信息创建了二级索引，那么您可以使用EntityJoin将所有代表汽车的对象返回，例如，在2002年建造的两扇门，这些门是绿色的。

要创建连接游标，您需要：

1. 打开要执行连接的实体类的主索引。
2. 打开要用于连接的二级索引。
3. 实例化一个EntityJoin对象（您使用主索引来执行此操作）。
4. 对EntityJoin.addCondition（）使用两个或更多调用来标识要用于相等匹配的二级索引及其值。
5. 调用EntityJoin.entities（）以获取可用于遍历连接结果的游标。

例如，假设我们有一个包含以下特征的实体类：
<pre>
<code>
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Automobiles {
	
	// Primary key is the vehicle identification number
    @PrimaryKey
    private String vin;

    // Secondary key is the vehicle's make
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String make;

    // Secondary key is the vehicle's color
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String color;

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
    
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return super.toString();
    }
}
</code>
</pre>
然后，我们可以执行一个实体加入，以搜索丰田制造的所有红色汽车，如下所示

<pre>
<code>
PrimaryIndex<String,Automobiles> vin_pidx;
SecondaryIndex<String,String,Automobiles> make_sidx;
SecondaryIndex<String,String,Automobiles> color_sidx;

EntityJoin<String,Automobiles> join = new EntityJoin(vin_pidx);
join.addCondition(make_sidx,"Toyota");
join.addCondition(color_sidx,"Red");

// Now iterate over the results of the join operation
ForwardCursor<Automobiles> join_cursor = join.entities();
try {
    for (Automobiles autoi : join_cursor) {
        // do something with each object "autoi"
    }
// Always make sure the cursor is closed when we are done with it.
} finally {
    join_cursor.close();
} 
</code>
</pre>

## 5.7 Deleting Entity Objects 删除实体对象

从Entity Store中删除对象的最简单方法是通过其主索引删除它。 例如，使用我们前面在本文档中创建的SimpleDA类（请参阅SimpleDA.class），可以使用keyone的主键删除SimpleEntityClass对象，如下所示：

sda.pIdx.delete（ “keyone”）;
您也可以通过它们的辅助键来删除对象。 当你这样做的时候，所有与二级键相关的对象都会被删除，除非这个键是一个外部对象。

例如，以下内容用skeyone的辅助键删除所有SimpleEntityClass：

sda.sIdx.delete（ “skeyone”）;
您可以通过将游标定位到该对象，然后调用游标的delete（）方法来删除任何单个对象。

<pre>
<code>
PrimaryIndex<String,SimpleEntityClass> pi =
    store.getPrimaryIndex(String.class, SimpleEntityClass.class);

SecondaryIndex<String,String,SimpleEntityClass> si = 
    store.getSecondaryIndex(pi, String.class, "sKey");

EntityCursor<SimpleEntityClass> sec_cursor = 
    si.subIndex("skeyone").entities(); 

try {
    SimpleEntityClass sec;
    Iterator<SimpleEntityClass> i = sec_cursor.iterator();
    while (sec = i.nextDup() != null) {
        if (sec.getSKey() == "some value") {
            i.delete();
        }
    }
// Always make sure the cursor is closed when we are done with it.
} finally {
    sec_cursor.close(); } 
</code>
</pre>
最后，如果您使用外键进行索引，则删除键的结果由您为索引设置的外键约束确定。 有关更多信息，请参阅外键约束。

## 5.7 Replacing Entity Objects 替换Entity 对象

要修改存储的实体对象，检索它，更新它，然后将其放回实体存储区：

<pre>
<code>
SimpleEntityClass sec = sda.pIdx.get（“keyone”）;
sec.setSKey（ “skeyoneupdated”）;
sda.pIdx.put（秒）;
</code>
</pre>

请注意，因为我们更新了作为辅助键的对象上的字段，所以此对象现在可以通过skeyoneupdated的辅助键来访问，而不是先前的值，即skeyone

请注意，如果您修改对象的主键，则行为会有所不同。在这种情况下，您将在Store中创建对象的新实例，而不是替换现有实例：

<pre>
<code>
//在商店中产生两个对象。一个与一个
//主键索引为“keyfive”，另一个主索引为
// 'keyfivenew'。
SimpleEntityClass sec = sda.pIdx.get（"keyfive"）;
sec.setPKey（"keyfivenew"）;
sda.pIdx.put（sec）;
</code>
</pre>
最后，如果使用EntityCursor遍历一组对象，则可以使用EntityCursor.update（）依次更新每个对象。但是请注意，您必须使用PrimaryIndex进行迭代;如果您正在使用SecondaryIndex，则不允许此操作。

例如，以下内容遍历实体存储中的每个SimpleEntityClass对象，并且它将全部更改它们，以便它们具有updatedskey的二级索引：

<pre>
<code>
EntityCursor<SimpleEntityClass> sec_pcursor = sda.pIdx.entities();
for (SimpleEntityClass sec : sec_pcursor) {
    sec.setSKey("updatedskey");
    sec_pcursor.update(item);
}
sec_pcursor.close(); 
</code>
</pre>
