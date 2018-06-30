package com.xingyun.dpl.model.simple;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

/**
 * 存放数据
 * */
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
