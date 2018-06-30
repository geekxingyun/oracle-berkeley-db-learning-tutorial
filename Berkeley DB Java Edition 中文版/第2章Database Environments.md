# 第2章 Database Environments
无论您使用的是Direct Persistence Layer（DPL)还是基础API，都必须使用数据库环境。数据库环境封装一个或多个数据库。通过允许为环境中包含的每个数据库使用单个内存中缓存，此封装为您的线程提供了对数据库的有效访问。此封装还允许您在单个事务内对多个数据库执行的操作进行分组（有关更多信息，请参阅Berkeley DB，Java Edition Getting Started with Transaction Processing guide）。

如果您使用的是基本API，则通常使用数据库环境来创建和打开数据库（使用单个数据库句柄关闭各个数据库）。您还可以使用环境来删除和重命名数据库。对于事务性应用程序，您可以使用该环境启动事务。对于非事务性应用程序，您可以使用环境将内存缓存同步到磁盘。

如果您正在使用DPL，所有这些事情仍在进行中，但DPL会为您处理。在DPL下，您将明确使用环境的最常见事情是获取事务句柄。

无论您使用哪种API，还可以使用数据库环境进行与数据库日志文件和内存中缓存相关的管理和配置活动。请参阅管理Berkeley DB Java版应用程序以获取更多信息。

要了解如何在受事务保护的应用程序中使用环境，请参阅Berkeley DB，Java Edition Getting Started with Transaction Processing guide.

## 2.1 Opening Database Environments(打开数据库环境)

通过实例化一个Environment对象来打开一个数据库环境。 您必须向构造函数提供环境所在的磁盘目录的名称。 该目录位置必须存在否则打开将失败。

默认情况下，如果环境不存在，则不会为您创建环境。 如果您希望创建环境，请将creation属性设置为true。 例如：
<pre>
<code>
import java.io.File;
import java.util.IllegalFormatCodePointException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 *  Open the environment 
 *  打开数据库环境
 *  
 * */
public class Study001_Open_Single_Environment {

	//配置数据库环境文件路径,
	private final static String BDB_001_ENV_HOME_FILE_PATH="bdb_001_env_home";
	private final static File BDB_001_ENV_HOME_File=new File(BDB_001_ENV_HOME_FILE_PATH);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//创建环境对象引用
		Environment myDbEnvironment = null;
		try {
			
			//创建环境配置对象引用并实例化一个环境配置对象
		    EnvironmentConfig envConfig = new EnvironmentConfig();
		    //设置允许如果不存在就创建
		    envConfig.setAllowCreate(true);
		    
		    //如果该文件路径不存在则创建
		    if (!BDB_001_ENV_HOME_File.exists()) {
				BDB_001_ENV_HOME_File.mkdirs();
			}
		   
		    //通过实例化一个Environment对象来打开一个数据库环境。
		    myDbEnvironment = new Environment(BDB_001_ENV_HOME_File, envConfig); //( 设置数据库环境路径 , 加载环境配置对象)
		    
		    //打印配置文件
		    System.out.println("------数据库配置文件开始---------");
		    System.out.println(myDbEnvironment.getConfig());
		    System.out.println("-----数据库配置文件结束---------");
		    
		} catch (DatabaseException dbe) {
		    // Exception handling goes here
			dbe.printStackTrace();
			System.err.println(dbe.toString());
		} 
	}
}
</code>
</pre>


打开环境通常会导致启动一些后台线程。 JE使用这些线程进行日志文件清理和一些管理任务。 但是，这些线程仅在每个进程中打开一次，因此如果您在同一进程中多次打开相同的环境，则不会对应用程序造成任何性能影响。 此外，如果以只读方式打开环境，则后台线程（除了evictor线程）不会启动。

请注意，打开您的环境会导致正常恢复运行。 这会使您的数据库进入与您的日志文件中找到的已更改数据相关的一致状态。 有关更多信息，请参阅数据库和日志文件。

### 2.1.1 Multiple Environments 多种环境

大多数JE应用程序只需要一个数据库环境，因为可以在单个环境中创建任意数量的数据库，并且环境中的数据总大小不受限制。 也就是说，您的应用程序可以打开并使用尽可能多的环境，因为您可以管理磁盘和内存。 另外，您可以为同一物理环境实例化多个Environment对象。

多个环境的主要原因是应用程序必须管理多个唯一的数据集。 通过将每个数据集放置在单独的环境中，应用程序可以在数据的可管理性和应用程序性能方面获得真正的优势。 通过将每个数据集放置在独特的环境中，将创建一组单独的日志文件并将其保存在一个单独的目录中，因此您可以单独操作每个数据集的日志文件。 也就是说，您可以：

- 通过复制或删除其环境的文件来单独备份，还原或删除单个数据集。

- 通过将单个数据集的文件从一台机器移动到另一台机器来平衡机器之间的负载。

- 通过将每个数据集放在单独的物理磁盘上来提高I / O性能。

- 通过删除环境的日志文件非常有效地删除单个数据集。 这比删除单个数据库记录更有效，并且比删除数据库更高效，如果您要管理必须经常删除的大型临时数据集，这可能是一个真正的好处。

请注意，使用多个环境存在缺点。尤其要明白，单个事务不能包含在多个环境中进行的更改。如果您需要以原子方式（使用单个事务）在多个数据集中执行一组操作，请使用单个环境并使用其他方法区分数据集。

例如，为多个客户端运行托管服务的应用程序可能希望将每个客户端的数据集分开。您可以在多个环境中执行此操作，但是您可以对所有数据集进行原子操作。如果您需要在单个事务中封装多个数据集的操作，请考虑其他一些将数据集分开的方法。

例如，您可以使用单个数据库中的唯一密钥范围来区分每个数据集。或者您可以创建一个标识数据集的辅助键。或者你可以为每个数据集使用不同的数据库。所有这些方法都允许您在单个环境中维护多个不同的数据集，但显然每个数据集都会为您的代码增加一层复杂度，而不是仅为每个数据集使用独特的环境。

### 2.1.2 Multiple Environment Subdirectories（多个环境子目录）

您可以将JE环境分布在多个子目录中。这使您可以通过将磁盘I / O分布到多个磁盘或文件系统来提高数据吞吐量。环境子目录驻留在环境主目录中，并且连续命名为data001 /通过dataNNN /，其中NNN是要使用的子目录的数目。通常，每个dataNNN /名称都是指向驻留在单独文件系统或磁盘上的实际目录的符号链接。或者，每个子目录都可以是驻留在不同磁盘驱动器上的文件系统的挂载点。

您可以通过je.properties文件中的je.log.nDataDirectories属性控制要使用的子目录的数量。此值必须在打开环境之前设置，并且此时子目录必须已经存在。为此属性设置的值不能在环境的生命周期中更改，或者在尝试打开环境时引发异常。

je.log.nDataDirectories的默认值为0，这意味着环境中没有子目录正在使用。大于0的值表示要使用的子目录的数量，并且在打开环境之前必须存在该子目录的数量。

例如，如果将je.log.nDataDirectories设置为3，那么首次打开环境（并且在此之后打开每个环境时），环境主目录必须包含三个名为data001，data002和data003的子目录。这会导致您的JE日志文件（* .jdb文件）在这三个子目录中均匀分布。最后，如果在没有完全删除环境的情况下更改je.log.nDataDirectories的值，那么当您打开环境时，应用程序将抛出异​​常。

## 2.1.3 Configuring a Shared Cache for Multiple Environments（为多个环境配置共享缓存）

默认情况下，每个不同的JE环境都有一个单独的专用内存中缓存。如果单个JVM进程将同时打开多个环境，则强烈建议将所有此类环境配置为使用共享缓存。共享缓存比单独的私有缓存更有效地利用内存。

例如，假设您在一个进程中打开了5个环境，并且共有500 MB的内存可用于缓存。使用专用缓存，可以将每个缓存配置为100 MB。如果其中一个环境具有比其他环境更大的活动数据集，则它将无法利用其他环境缓存中未使用的内存。通过使用共享缓存，多个开放环境将更好地利用内存，因为缓存LRU算法应用于共享缓存的所有环境中的所有信息。

为了配置环境以使用共享缓存，请将EnvironmentConfig.setSharedCache（）设置为true。这必须针对您要使用共享缓存的进程中的每个环境进行设置。例如：
<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class Study002_SharedCache_Multi_Environment {

    	//配置数据库环境文件路径,
		private final static String BDB_002_01_ENV_HOME_FILE_PATH="bdb_002_01_env_home";
		private final static String BDB_002_02_ENV_HOME_FILE_PATH="bdb_002_02_env_home";
		private final static File BDB_002_01_ENV_HOME_File=new File(BDB_002_01_ENV_HOME_FILE_PATH);
		private final static File BDB_002_02_ENV_HOME_File=new File(BDB_002_02_ENV_HOME_FILE_PATH);
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Environment myEnv1 = null;
		Environment myEnv2 = null;
		
		try {
		    EnvironmentConfig envConfig = new EnvironmentConfig();
            //允许重建
		    envConfig.setAllowCreate(true);
		    /**
		     * 为了配置环境以使用共享缓存，请将EnvironmentConfig.setSharedCache（）设置为true。
		     * 这必须针对您要使用共享缓存的进程中的每个环境进行设置。
		     * */
		    envConfig.setSharedCache(true);
		    
		    //如果环境文件路径不存在则创建
		    if(!BDB_002_01_ENV_HOME_File.exists()) {
		    	BDB_002_01_ENV_HOME_File.mkdirs();
		    }
		    if(!BDB_002_02_ENV_HOME_File.exists()) {
		    	BDB_002_02_ENV_HOME_File.mkdirs();
		    }

		    myEnv1 = new Environment(BDB_002_01_ENV_HOME_File, envConfig);
		    myEnv2 = new Environment(BDB_002_02_ENV_HOME_File, envConfig);
		    
		    System.out.println("------数据库配置文件开始---------");
		    System.out.println(myEnv1.getConfig());
		    System.out.println(myEnv2.getConfig());
		    System.out.println("------数据库配置文件结束---------");
		    
		} catch (DatabaseException dbe) {
		    // Exception handling goes here
		} 
	}
}
</code>
</pre>
## 2.2 Closing Database Environments

通过调用Environment.close（）方法关闭您的环境。 此方法执行检查点，因此在调用它之前不必显式执行同步或检查点。 有关检查点的信息，请参阅Berkeley DB，Java Edition“事务处理入门指南”。 有关同步的信息，请参阅数据库修改和同步。

<pre>
<code>
import java.io.File;
import java.util.IllegalFormatCodePointException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Open the environment 打开数据库环境
 * 
 */
public class Study001_Open_Single_Environment {

	// 配置数据库环境文件路径,
	private final static String BDB_001_ENV_HOME_FILE_PATH = "bdb_001_env_home";
	private final static File BDB_001_ENV_HOME_File = new File(BDB_001_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 创建环境对象引用
		Environment myDbEnvironment = null;
		try {

			// 创建环境配置对象引用并实例化一个环境配置对象
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// 设置允许如果不存在就创建
			envConfig.setAllowCreate(true);

			// 如果该文件路径不存在则创建
			if (!BDB_001_ENV_HOME_File.exists()) {
				BDB_001_ENV_HOME_File.mkdirs();
			}

			// 通过实例化一个Environment对象来打开一个数据库环境。
			myDbEnvironment = new Environment(BDB_001_ENV_HOME_File, envConfig); // ( 设置数据库环境路径 , 加载环境配置对象)

			// 打印配置文件
			System.out.println("------数据库配置文件开始---------");
			System.out.println(myDbEnvironment.getConfig());
			System.out.println("-----数据库配置文件结束---------");

		} catch (DatabaseException dbe) {
			// Exception handling goes here
			dbe.printStackTrace();
			System.err.println(dbe.toString());
		}

		// 关闭数据库环境
		if (myDbEnvironment != null) {
			myDbEnvironment.close();
		}
	}
}
</code>
</pre>
如果您使用的是DPL，则只有在所有其他store活动完成并关闭了当前在该环境中打开的所有Store后，才能关闭您的环境。 如果您使用的是基本API，则只有在所有其他数据库活动完成并且您已关闭当前在该环境中打开的任何数据库后才能关闭您的环境。

> 在JE的清洁线程完成工作之前，环境可能会关闭。 如果您在关闭环境之前立即执行大量删除，则会发生这种情况。 其结果是你的日志文件可能会比你期望的大得多，因为更干净的线程没有机会完成它的工作。
> 
> 有关吸尘器螺纹的详细信息，请参阅吸尘器螺纹。
> 
> 如果您想确保在清理环境之前清理器已经完成运行，请在调用Environment.close（）之前调用Environment.cleanLog（）：

关闭应用程序中的最后一个环境句柄会导致释放所有内部数据结构，并停止后台线程。 如果有任何已打开的数据库，那么JE会在关闭它们之前进行投诉。 此时，任何正在进行的交易都会中止。 此时还有任何公开的游标也被关闭。 但是，建议您在使用它们后立即关闭所有的游标句柄，以确保并发性并释放资源，例如页面锁定。

## 2.3 Environment Properties

-  您可以使用EnvironmentConfig类为环境设置属性。
-  您还可以使用EnvironmentMutableConfig为特定的Environment实例设置属性。

### 2.3.1 The EnvironmentConfig Class

EnvironmentConfig类为您提供了大量的字段和方法。 描述所有这些调整参数超出了本手册的范围。 但是，您可能希望设置一些属性。 这里描述它们。

请注意，对于您通常可以设置的每个属性，都有一个相应的getter方法。 此外，您始终可以使用Environment.getConfig（）方法检索环境使用的EnvironmentConfig对象。

您可以在EnvironmentConfig类中使用以下方法设置环境配置参数：

- EnvironmentConfig.setAllowCreate（）

如果为true，则数据库环境在打开时创建。 如果为false，则如果环境不存在，则环境打开失败。 如果数据库环境已存在，则此属性没有意义。 默认为false。

- EnvironmentConfig.setReadOnly（）

如果为true，则在此环境中打开的所有数据库必须以只读方式打开。 如果您正在编写多进程应用程序，那么除了其中一个进程外，其他所有进程都必须将此值设置为true。 默认为false。

您还可以使用env_home / je.properties文件中的je.env.isReadOnly参数来设置此属性。

- EnvironmentConfig.setTransactional（）

如果为true，则配置数据库环境以支持事务。 默认为false。

您还可以使用env_home / je.properties文件中的je.env.isTransactional参数来设置此属性。
<pre>
<code>
import java.io.File;
import java.util.IllegalFormatCodePointException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Open the environment 打开数据库环境
 * 
 */
public class Study001_Open_Single_Environment {

	// 配置数据库环境文件路径,
	private final static String BDB_001_ENV_HOME_FILE_PATH = "bdb_001_env_home";
	private final static File BDB_001_ENV_HOME_File = new File(BDB_001_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 创建环境对象引用
		Environment myDbEnvironment = null;
		try {

			// 创建环境配置对象引用并实例化一个环境配置对象
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// 如果为true，则数据库环境在打开时创建。
			//如果为false，则如果环境不存在，则环境打开失败。 
			//如果数据库环境已存在，则此属性没有意义。 默认为false。
			envConfig.setAllowCreate(true);
			//如果为true，则配置数据库环境以支持事务。 默认为false。
			envConfig.setTransactional(true);

			// 如果该文件路径不存在则创建
			if (!BDB_001_ENV_HOME_File.exists()) {
				BDB_001_ENV_HOME_File.mkdirs();
			}

			// 通过实例化一个Environment对象来打开一个数据库环境。
			myDbEnvironment = new Environment(BDB_001_ENV_HOME_File, envConfig); // ( 设置数据库环境路径 , 加载环境配置对象)

			// 打印配置文件
			System.out.println("------数据库配置文件开始---------");
			System.out.println(myDbEnvironment.getConfig());
			System.out.println("-----数据库配置文件结束---------");

		} catch (DatabaseException dbe) {
			// Exception handling goes here
			dbe.printStackTrace();
			System.err.println(dbe.toString());
		}

		// 关闭数据库环境
		if (myDbEnvironment != null) {
			myDbEnvironment.cleanLog(); // Clean the log before closing
			myDbEnvironment.close();
		}
	}
}
</code>
</pre>
### 2.3.2 EnvironmentMutableConfig

EnvironmentMutableConfig管理在构建Environment对象后可以重置的属性。 另外，EnvironmentConfig扩展了EnvironmentMutableConfig，所以你可以在环境施工时根据需要设置这些可变属性。

EnvironmentMutableConfig类允许你设置下列属性：

- setCachePercent（）

确定可供JE高速缓存使用的JVM内存的百分比。 有关更多信息，请参阅调整缓存大小。

- setCacheSize（）

确定可用于数据库缓存的总内存量。 有关更多信息，请参阅调整缓存大小。

- setTxnNoSync（）

确定由于事务提交而创建的更改记录是否写入磁盘上的后备日志文件。 值为true会导致数据不会刷新到磁盘。 请参阅Berkeley DB，Java Edition入门“事务处理指南”。

- setTxnWriteNoSync（）

确定日志是否在事务提交时被刷新（但日志仍然被写入）。 通过将此值设置为true，与通过提交刷新日志相比，您可能获得更好的性能，但是您会因失去一些事务持久性保证而获得更好的性能。

还有一个相应的getter方法（getTxnNoSync（））。 而且，您可以使用Environment.getMutableConfig（）方法始终检索环境的EnvironmentMutableConfig对象。

<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;

/**
 * 
 * 
 */
public class Study003_EnvironmentMutableConfig {

	// 配置数据库环境文件路径,
	private final static String BDB_003_ENV_HOME_FILE_PATH = "bdb_003_env_home";
	private final static File BDB_003_ENV_HOME_File = new File(BDB_003_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 创建环境对象引用
		Environment myDbEnvironment = null;
		try {

			// 创建环境配置对象引用并实例化一个环境配置对象
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// 如果为true，则数据库环境在打开时创建。
			// 如果为false，则如果环境不存在，则环境打开失败。
			// 如果数据库环境已存在，则此属性没有意义。 默认为false。
			envConfig.setAllowCreate(true);
			// 如果为true，则配置数据库环境以支持事务。 默认为false。
			envConfig.setTransactional(true);

			// 创建环境配置对象引用并实例化一个环境多配置对象
			EnvironmentMutableConfig envMultableConfig = new EnvironmentMutableConfig();

			// 确定由于事务提交而创建的更改记录是否写入磁盘上的后备日志文件。 值为true会导致数据不会刷新到磁盘。
			envMultableConfig.setTxnNoSyncVoid(false);
			// 确定日志是否在事务提交时被刷新（但日志仍然被写入）。
			// 通过将此值设置为true，与通过提交刷新日志相比，您可能获得更好的性能，但是您会因失去一些事务持久性保证而获得更好的性能。
			envMultableConfig.setTxnWriteNoSyncVoid(false);

			// 如果该文件路径不存在则创建
			if (!BDB_003_ENV_HOME_File.exists()) {
				BDB_003_ENV_HOME_File.mkdirs();
			}

			// 通过实例化一个Environment对象来打开一个数据库环境。
			myDbEnvironment = new Environment(BDB_003_ENV_HOME_File, envConfig); // ( 设置数据库环境路径 )
			myDbEnvironment.setMutableConfig(envMultableConfig);

			// 打印配置文件
			System.out.println("------数据库配置文件开始---------");
			System.out.println(myDbEnvironment.getConfig());
			System.out.println("-----数据库配置文件结束---------");

		} catch (DatabaseException dbe) {
			// Exception handling goes here
			dbe.printStackTrace();
			System.err.println(dbe.toString());
		}

		// 关闭数据库环境
		if (myDbEnvironment != null) {
			myDbEnvironment.cleanLog(); // Clean the log before closing
			myDbEnvironment.close();
		}
	}
}
</code>
</pre>
## 2.4 Environment Statistics(环境统计)

JE提供了大量关于您的环境操作的信息。 大部分信息涉及的数字仅与JE开发人员有关，因此对这些统计数据的描述超出了本手册的范围。

但是，一个非常重要的统计信息（特别是对于长时间运行的应用程序）是EnvironmentStats.getNCacheMiss（）。 此统计信息返回缓存中不可用的数据库对象的请求总数。 这个数字对尝试确定内存中缓存的正确大小的应用程序管理员很重要。 详情请参阅调整缓存大小。

要从您的环境中获取此统计信息，请调用Environment.getStats（）以返回EnvironmentStats对象。 然后可以调用EnvironmentStats.getNCacheMiss（）方法。 例如：
<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;

/**
 * 
 * 
 */
public class Study003_EnvironmentMutableConfig {

	// 配置数据库环境文件路径,
	private final static String BDB_003_ENV_HOME_FILE_PATH = "bdb_003_env_home";
	private final static File BDB_003_ENV_HOME_File = new File(BDB_003_ENV_HOME_FILE_PATH);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 创建环境对象引用
		Environment myDbEnvironment = null;
		try {

			// 创建环境配置对象引用并实例化一个环境配置对象
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// 如果为true，则数据库环境在打开时创建。
			// 如果为false，则如果环境不存在，则环境打开失败。
			// 如果数据库环境已存在，则此属性没有意义。 默认为false。
			envConfig.setAllowCreate(true);
			// 如果为true，则配置数据库环境以支持事务。 默认为false。
			envConfig.setTransactional(true);

			// 创建环境配置对象引用并实例化一个环境多配置对象
			EnvironmentMutableConfig envMultableConfig = new EnvironmentMutableConfig();

			// 确定由于事务提交而创建的更改记录是否写入磁盘上的后备日志文件。 值为true会导致数据不会刷新到磁盘。
			envMultableConfig.setTxnNoSyncVoid(false);
			// 确定日志是否在事务提交时被刷新（但日志仍然被写入）。
			// 通过将此值设置为true，与通过提交刷新日志相比，您可能获得更好的性能，但是您会因失去一些事务持久性保证而获得更好的性能。
			envMultableConfig.setTxnWriteNoSyncVoid(false);

			// 如果该文件路径不存在则创建
			if (!BDB_003_ENV_HOME_File.exists()) {
				BDB_003_ENV_HOME_File.mkdirs();
			}

			// 通过实例化一个Environment对象来打开一个数据库环境。
			myDbEnvironment = new Environment(BDB_003_ENV_HOME_File, envConfig); // ( 设置数据库环境路径 )
			myDbEnvironment.setMutableConfig(envMultableConfig);
			
			//Environment.getStats（）只能从应用程序的进程中获取统计信息
			//此统计信息返回缓存中不可用的数据库对象的请求总数。
			long cacheMisses = myDbEnvironment.getStats(null).getNCacheMiss();

			System.out.println(cacheMisses);
			// 打印配置文件
			System.out.println("------数据库配置文件开始---------");
			System.out.println(myDbEnvironment.getConfig());
			System.out.println("-----数据库配置文件结束---------");

		} catch (DatabaseException dbe) {
			// Exception handling goes here
			dbe.printStackTrace();
			System.err.println(dbe.toString());
		}

		// 关闭数据库环境
		if (myDbEnvironment != null) {
			myDbEnvironment.cleanLog(); // Clean the log before closing
			myDbEnvironment.close();
		}
	}
}
</code>
</pre>

请注意，Environment.getStats（）只能从应用程序的进程中获取统计信息。 为了让应用程序管理员获得此统计信息，您必须使用JMX来检索统计信息（请参阅JConsole和JMX支持），或者必须打印它才能进行检查（例如，每分钟记录一次值）。

请记住，对于缓存大小来说真正重要的是随着时间的推移这个值的变化，而不是实际值本身。 所以你可能会考虑提供从这个统计量到下一个统计量的增量（一个0的增量是理想的，而大的增量是一个缓存太小的指示）。

## 2.5 Database Environment Management Example

这个例子提供了一个完整的类，可以打开和关闭一个环境。 在本书的后续示例中，它都被扩展并用于打开和关闭环境和数据库。 我们这样做是为了使示例代码更简单，更易于管理。 你可以在以下地点找到这门课：

JE_HOME/examples/je/gettingStarted/MyDbEnv.java
JE_HOME是放置JE分发的位置。

### 2.5.1 Example 2.1 Database Environment Management Class

<pre>
<code>
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
</code>
</pre>
代码调用
<pre>
<code>
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
</code>
</pre>