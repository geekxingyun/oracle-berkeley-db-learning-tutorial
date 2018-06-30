# 第6章 A DPL Example一个DPL 例子 

为了说明DPL的用法，我们在本章中提供了一个完整的工作示例。 本示例读取和写入虚构业务的清单和供应商信息。 

该应用程序由以下类组成：

几个类用于封装我们的应用程序的数据。 请参阅Vendor.java和Inventory.java。

用于打开和关闭我们的环境和Entity Store的便利课程。 请参阅MyDbEnv。

将数据加载到Store中的类。 请参阅ExampleDatabasePut.java。

最后，一个从Store读取数据的类。 请参阅ExampleInventoryRead.java。

请注意，可以在JE发行版中的以下位置找到此示例：

JE_HOME/例子/坚持/ gettingStarted

JE_HOME是放置JE分发的位置

## 6.1 Vendor.java

我们的示例想要存储的最简单的类包含供应商联系信息。 这个类不包含二级索引，所以我们所要做的就是将它识别为一个实体类并标识用于主键的类中的字段。

在以下示例中，我们将供应商数据成员标识为包含主键。 此数据成员旨在包含供应商的名称。 由于我们将使用我们的EntityStore，因此为数据成员提供的值在存储中必须是唯一的，否则将导致运行时错误。

与DPL一起使用时，我们的供应商类别如下所示。 注意，@Entity注解出现在类声明的前面，并且@PrimaryKey注解紧接在供应商数据成员声明之前出现。

<pre>
<code>
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * 供应商实体类
 * 
 * **/
//注解标识这是一个DPL实体类
@Entity
public class Vendor {

	private String address;
    private String bizPhoneNumber;
    private String city;
    private String repName;
    private String repPhoneNumber;
    private String state;
    
    //标识该字段是一个主键在数据库中是唯一的
    @PrimaryKey
    private String vendor;//供应商名称

    private String zipcode;//邮政编码

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getBizPhoneNumber() {
		return bizPhoneNumber;
	}

	public void setBizPhoneNumber(String bizPhoneNumber) {
		this.bizPhoneNumber = bizPhoneNumber;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRepName() {
		return repName;
	}

	public void setRepName(String repName) {
		this.repName = repName;
	}

	public String getRepPhoneNumber() {
		return repPhoneNumber;
	}

	public void setRepPhoneNumber(String repPhoneNumber) {
		this.repPhoneNumber = repPhoneNumber;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
    
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return super.toString();
    }
}
</code>
</pre>

对于此类，通过setVendorName（）方法为单个Vendor类对象设置供应商值。 如果我们的示例代码在存储对象之前未能设置此值，则用于存储主键的数据成员被设置为空值。 这会导致运行时错误。

## 6.2 Inventory.java

我们的示例库存类与供应商类非常相似，因为它仅用于封装数据。 但是，在这种情况下，我们希望能够以两种不同的方式访问对象：按产品SKU和按产品名称。

在我们的数据集中，产品SKU必须是唯一的，因此我们将其用作主键。 然而，产品名称并非唯一值，因此我们将其设置为辅助键。

这个类在我们的例子中显示如下：

<pre>
<code>
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * 库存类
 */
public class Inventory {

	// 库存量在数据库中必须是唯一的，所以我们将其设置为主键
	@PrimaryKey
	private String sku;// 库存量

	// 产品名称并非是唯一的，所以我们将其设置为辅助键
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	private String itemName;

	private String category;
	private String vendor;
	private int vendorInventory;
	private float vendorPrice;

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public int getVendorInventory() {
		return vendorInventory;
	}

	public void setVendorInventory(int vendorInventory) {
		this.vendorInventory = vendorInventory;
	}

	public float getVendorPrice() {
		return vendorPrice;
	}

	public void setVendorPrice(float vendorPrice) {
		this.vendorPrice = vendorPrice;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
</code>
</pre>

## 6.3 MyDbEnv.java

我们为示例构建的应用程序都必须打开和关闭环境和EntityStore。 我们的一个应用程序正在写入实体存储，因此此应用程序需要以读写方式打开存储。 它也希望能够创建Store，如果它不存在。

我们的第二个应用程序只从Store读取。 在这种情况下，Store应该以只读方式打开。

我们通过创建一个负责打开和关闭我们的Store和Environment的类来执行这些活动。 这个类是我们的应用程序共享的。 要使用它，调用者只需要提供环境主目录的路径，并指出对象是否是只读的。 这个类的实现如下：

<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class MyDbEnv {
	
	    private Environment myEnv;
	    private EntityStore store;
	    
	    //空的构造器
	    public MyDbEnv() {}
	    
	 // The setup() method opens the environment and store
	    // for us.
	    public void setup(File envHome, boolean readOnly)
	        throws DatabaseException {

	        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
	        StoreConfig storeConfig = new StoreConfig();

	        myEnvConfig.setReadOnly(readOnly);
	        storeConfig.setReadOnly(readOnly);

	        // If the environment is opened for write, then we want to be 
	        // able to create the environment and entity store if 
	        // they do not exist.
	        myEnvConfig.setAllowCreate(!readOnly);
	        storeConfig.setAllowCreate(!readOnly);

	        // Open the environment and entity store
	        myEnv = new Environment(envHome, myEnvConfig);
	        store = new EntityStore(myEnv, "EntityStore", storeConfig);

	    }
	    
	    // Return a handle to the entity store
	    public EntityStore getEntityStore() {
	        return store;
	    }

	    // Return a handle to the environment
	    public Environment getEnv() {
	        return myEnv;
	    }
	    
	    // Close the store and environment.
	    public void close() {
	        if (store != null) {
	            try {
	                store.close();
	            } catch(DatabaseException dbe) {
	                System.err.println("Error closing store: " +
	                                    dbe.toString());
	               System.exit(-1);
	            }
	        }
	        if (myEnv != null) {
	            try {
	                // Finally, close the environment.
	                myEnv.close();
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

## 6.4 DataAccessor.java

现在我们已经实现了我们的数据类，我们可以编写一个类，它可以方便地访问我们的主索引和二级索引。 请注意，就像我们的数据类一样，这个类由我们的示例程序共享。

如果您将此类与我们的供应商和库存类实施进行比较，您将看到在此处声明的主要和次要索引均由此类引用。

这些实现参见Vendor.java和Inventory.java。

<pre>
<code>
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.xingyun.dpl.example.model.Inventory;
import com.xingyun.dpl.example.model.Vendor;

/**
 * 我们使用专门的数据访问器类来组织我们的主要和次要索引。 
 * 这个类存在的主要原因是为我们的实体类提供对所有使用的索引的方便访问
 * */
public class DataAccessor {
	
    // Inventory Accessors
    PrimaryIndex<String,Inventory> inventoryBySku;
    SecondaryIndex<String,String,Inventory> inventoryByName;

    // Vendor Accessors
    PrimaryIndex<String,Vendor> vendorByName;
    
	// Open the indices
    public DataAccessor(EntityStore store)
        throws DatabaseException {

        // Primary key for Inventory classes
        inventoryBySku = store.getPrimaryIndex(
            String.class, Inventory.class);

        // Secondary key for Inventory classes
        // Last field in the getSecondaryIndex() method must be
        // the name of a class member; in this case, an Inventory.class
        // data member.
        inventoryByName = store.getSecondaryIndex(
            inventoryBySku, String.class, "itemName");

        // Primary key for Vendor class
        vendorByName = store.getPrimaryIndex(
            String.class, Vendor.class);
    }
}
</code>
</pre>

## 6.5 ExampleDatabasePut.java

<pre>
<code>

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.xingyun.dpl.example.model.Inventory;
import com.xingyun.dpl.example.model.Vendor;
public class ExampleDatabasePut {
	
	   /**
	    我们的main（）方法也是不言自明的。 我们只是在那里实例化一个ExampleDatabasePut对象，然后调用它的run（）方法。
	    我们还在那里为运行时可能引发的任何异常提供顶级try块。
       请注意，顶级try块中的finally语句调用MyDbEnv.close（）。 这个方法关闭我们的EntityStore和Environment对象。 
	    通过将它放置在finally语句中，我们可以确保我们的商店和环境总是完全关闭。
	    * */
	   public static void main(String args[]) {
		   
	        ExampleDatabasePut edp = new ExampleDatabasePut();
	        try {
	            edp.run(args);
	        } catch (DatabaseException dbe) {
	            System.err.println("ExampleDatabasePut: " + dbe.toString());
	            dbe.printStackTrace();
	        } catch (Exception e) {
	            System.out.println("Exception: " + e.toString());
	            e.printStackTrace();
	        } finally {
	            myDbEnv.close();
	        }
	        System.out.println("All done.");
	    } 
	
	    private static File myDbEnvPath = new File("resources"+File.separator+"db");
	    private static File inventoryFile = new File("resources"+File.separator+"inventory.txt");
	    private static File vendorsFile = new File("resources"+File.separator+"vendors.txt");

	    private DataAccessor da;

	    // 封装环境和数据存储
	    private static MyDbEnv myDbEnv = new MyDbEnv();
	    
	    
	    //接下来，我们提供我们的usage（）方法。 仅当磁盘资源的默认值不足时，才需要使用此处提供的命令行选项。
	    private static void usage() {
	        System.out.println("ExampleDatabasePut [-h <env directory>]");
	        System.out.println("      [-i <inventory file>]");
	        System.out.println("      [-v <vendors file>]");
	        System.exit(-1);
	    } 
	    
	    /**
	     * 我们的run（）方法做了四件事。
	     * 1.  它调用MyDbEnv.setup（），它打开我们的Environment和EntityStore。
	     *  然后它实例化一个DataAccessor对象，我们将使用它来将数据写入商店。 
	     *  它调用加载所有供应商信息的loadVendorsDb（）。
	     *   然后它调用加载所有库存信息的loadInventoryDb（）。
          请注意MyDbEnv对象正在设置为读写。 这会导致打开EntityStore以获得事务支持。 
          （有关实现细节，请参阅MyDbEnv。）*/
	    private void run(String args[])throws DatabaseException {
	           
	    	    // 解析参数列表
	            parseArgs(args);
	            
	            if(!myDbEnvPath.exists()) {
	            	myDbEnvPath.mkdirs();
	            }
	            
	            //// 指向数据库环境目录,环境是否是只读
	            myDbEnv.setup(myDbEnvPath,  false); 

	            // 打开数据访问器. This is used to store
	            // persistent objects.
	            da = new DataAccessor(myDbEnv.getEntityStore());

	            //加载数据
	            System.out.println("loading vendors db....");
	            loadVendorsDb();

	            //加载数据
	            System.out.println("loading inventory db....");
	            loadInventoryDb();
	        } 
	    
	    /**
	     * 我们现在可以实现loadVendorsDb（）方法。 
	     * 此方法负责从相应的平面文本文件中读取供应商联系信息，用数据填充Vendor类对象，然后将其写入EntityStore。
	     *  如上所述，每个单独的对象都是用事务支持来编写的。 但是，由于事务句柄没有明确使用，所以使用自动提交来执行写操作。 
	     *  发生这种情况是因为EntityStore已打开以支持事务。
          要将每个类实际写入EntityStore，我们只需为Vendor实体实例调用PrimaryIndex.put（）方法。 我们从我们的DataAccessor类中获得这个方法。*/
	    private void loadVendorsDb()
	            throws DatabaseException {

	        // loadFile opens a flat-text file that contains our data
	        // and loads it into a list for us to work with. The integer
	        // parameter represents the number of fields expected in the
	        // file.
	        List vendors = loadFile(vendorsFile, 8);

	        // Now load the data into the store.
	        for (int i = 0; i < vendors.size(); i++) {
	            String[] sArray = (String[])vendors.get(i);
	            Vendor theVendor = new Vendor();
	            theVendor.setVendorName(sArray[0]);
	            theVendor.setAddress(sArray[1]);
	            theVendor.setCity(sArray[2]);
	            theVendor.setState(sArray[3]);
	            theVendor.setZipcode(sArray[4]);
	            theVendor.setBizPhoneNumber(sArray[5]);
	            theVendor.setRepName(sArray[6]);
	            theVendor.setRepPhoneNumber(sArray[7]);

	            // Put it in the store.
	            da.vendorByName.put(theVendor);
	        }
	    } 
	    
	    //现在我们可以实现我们的loadInventoryDb（）方法。 这与loadVendorsDb（）方法完全相同。
	    private void loadInventoryDb()
	            throws DatabaseException {

	            // loadFile opens a flat-text file that contains our data
	            // and loads it into a list for us to work with. The integer
	            // parameter represents the number of fields expected in the
	            // file.
	            List inventoryArray = loadFile(inventoryFile, 6);

	            // Now load the data into the store. The item's sku is the
	            // key, and the data is an Inventory class object.

	            for (int i = 0; i < inventoryArray.size(); i++) {
	                String[] sArray = (String[])inventoryArray.get(i);
	                String sku = sArray[1];

	                Inventory theInventory = new Inventory();
	                theInventory.setItemName(sArray[0]);
	                theInventory.setSku(sArray[1]);
	                theInventory.setVendorPrice(
	                    (new Float(sArray[2])).floatValue());
	                theInventory.setVendorInventory(
	                    (new Integer(sArray[3])).intValue());
	                theInventory.setCategory(sArray[4]);
	                theInventory.setVendor(sArray[5]);

	                // Put it in the store. Note that this causes our secondary key
	                // to be automatically updated for us.
	                da.inventoryBySku.put(theInventory);
	            }
	        } 
	    
	    /**
	     * 此示例的其余部分简单解析命令行并从平面文本文件加载数据。 
	     * 这里没有什么特别感兴趣的DPL，但为了完整起见，我们无论如何都展示了这部分示例。
	     * */
	    private static void parseArgs(String args[]) {
	        for(int i = 0; i < args.length; ++i) {
	            if (args[i].startsWith("-")) {
	                switch(args[i].charAt(1)) {
	                  case 'h':
	                    myDbEnvPath = new File(args[++i]);
	                    break;
	                  case 'i':
	                    inventoryFile = new File(args[++i]);
	                    break;
	                  case 'v':
	                    vendorsFile = new File(args[++i]);
	                    break;
	                  default:
	                    usage();
	                }
	            }
	        }
	    }

	    private List loadFile(File theFile, int numFields) {
	        List<String[]> records = new ArrayList<String[]>();
	        try {
	            String theLine = null;
	            FileInputStream fis = new FileInputStream(theFile);
	            BufferedReader br = 
	                new BufferedReader(new InputStreamReader(fis));
	            while((theLine=br.readLine()) != null) {
	                String[] theLineArray = theLine.split("#");
	                if (theLineArray.length != numFields) {
	                    System.out.println("Malformed line found in " + 
	                        theFile.getPath());
	                    System.out.println("Line was: '" + theLine);
	                    System.out.println("length found was: " + 
	                        theLineArray.length);
	                    System.exit(-1);
	                }
	                records.add(theLineArray);
	            }
	            // Close the input stream handle
	            fis.close();
	        } catch (FileNotFoundException e) {
	            System.err.println(theFile.getPath() + " does not exist.");
	            e.printStackTrace();
	            usage();
	        } catch (IOException e)  {
	            System.err.println("IO Exception: " + e.toString());
	            e.printStackTrace();
	            System.exit(-1);
	        }
	        return records;
	    }

	    protected ExampleDatabasePut() {}
}
</code>
</pre>

## 6.6  ExampleInventoryRead.java

ExampleInventoryRead从我们的实体库中检索库存信息并显示它。 当它显示每个库存项目时，它也会显示相关的供应商联系信息。

ExampleInventoryRead可以做两件事之一。 如果您不提供搜索条件，则会显示商店中的所有库存商品。 如果您提供项目名称（使用-s命令行开关），则只显示使用该名称的清单项目。

我们的示例的开始与我们的ExampleDatabasePut示例程序几乎完全相同。 为了完整起见，我们在此重复该示例代码。 有关完整的介绍，请参阅上一节（ExampleDatabasePut.java）。

<pre>
<code>
import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.xingyun.dpl.example.model.Inventory;
import com.xingyun.dpl.example.model.Vendor;

public class ExampleInventoryRead {
	
    private static File myDbEnvPath = new File("resources"+File.separator+"db");
/*    private static File inventoryFile = new File("resources"+File.separator+"inventory.txt");
    private static File vendorsFile = new File("resources"+File.separator+"vendors.txt");*/

	    private DataAccessor da;

	    // Encapsulates the database environment.
	    private static MyDbEnv myDbEnv = new MyDbEnv();

	    // The item to locate if the -s switch is used
	    private static String locateItem;

	    private static void usage() {
	        System.out.println("ExampleInventoryRead [-h <env directory>]" +
	                           "[-s <item to locate>]");
	        System.exit(-1);
	    }

	    public static void main(String args[]) {
	        ExampleInventoryRead eir = new ExampleInventoryRead();
	        try {
	            eir.run(args);
	        } catch (DatabaseException dbe) {
	            System.err.println("ExampleInventoryRead: " + dbe.toString());
	            dbe.printStackTrace();
	        } finally {
	            myDbEnv.close();
	        }
	        System.out.println("All done.");
	    }

	    private void run(String args[])
	        throws DatabaseException {
	        // Parse the arguments list
	        parseArgs(args);

	        myDbEnv.setup(myDbEnvPath, // path to the environment home
	                      true);       // is this environment read-only?

	        // Open the data accessor. This is used to retrieve
	        // persistent objects.
	        da = new DataAccessor(myDbEnv.getEntityStore());

	        // If a item to locate is provided on the command line,
	        // show just the inventory items using the provided name.
	        // Otherwise, show everything in the inventory.
	        if (locateItem != null) {
	            showItem();
	        } else {
	            showAllInventory();
	        }
	    } 

	    // Shows all the inventory items that exist for a given
	    // inventory name.
	    private void showItem() throws DatabaseException {

	        // Use the inventory name secondary key to retrieve
	        // these objects.
	        EntityCursor<Inventory> items =
	            da.inventoryByName.subIndex(locateItem).entities();
	        try {
	            for (Inventory item : items) {
	                displayInventoryRecord(item);
	            }
	        } finally {
	            items.close();
	        }
	    } 
	    
	 // Displays all the inventory items in the store
	    private void showAllInventory()
	        throws DatabaseException {

	        // Get a cursor that will walk every
	        // inventory object in the store.
	        EntityCursor<Inventory> items =
	            da.inventoryBySku.entities();

	        try {
	            for (Inventory item : items) {
	                displayInventoryRecord(item);
	            }
	        } finally {
	            items.close();
	        }
	    } 
	    
	    private void displayInventoryRecord(Inventory theInventory)
	            throws DatabaseException {

	            System.out.println(theInventory.getSku() + ":");
	            System.out.println("\t " + theInventory.getItemName());
	            System.out.println("\t " + theInventory.getCategory());
	            System.out.println("\t " + theInventory.getVendor());
	            System.out.println("\t\tNumber in stock: " +
	                theInventory.getVendorInventory());
	            System.out.println("\t\tPrice per unit:  " +
	                theInventory.getVendorPrice());
	            System.out.println("\t\tContact: ");

	            Vendor theVendor =
	                    da.vendorByName.get(theInventory.getVendor());
	            assert theVendor != null;

	            System.out.println("\t\t " + theVendor.getAddress());
	            System.out.println("\t\t " + theVendor.getCity() + ", " +
	                theVendor.getState() + " " + theVendor.getZipcode());
	            System.out.println("\t\t Business Phone: " +
	                theVendor.getBizPhoneNumber());
	            System.out.println("\t\t Sales Rep: " +
	                                theVendor.getRepName());
	            System.out.println("\t\t            " +
	                theVendor.getRepPhoneNumber());
	    } 
	    
	    protected ExampleInventoryRead() {}

	    private static void parseArgs(String args[]) {
	        for(int i = 0; i < args.length; ++i) {
	            if (args[i].startsWith("-")) {
	                switch(args[i].charAt(1)) {
	                    case 'h':
	                        myDbEnvPath = new File(args[++i]);
	                    break;
	                    case 's':
	                        locateItem = args[++i];
	                    break;
	                    default:
	                        usage();
	                }
	            }
	        }
	    }
	} 
</code>
</pre>

输出结果：

<pre>
<code>
MonofruiNu2uGH:
	 Monos Plum
	 fruits
	 Off the Vine
		Number in stock: 978
		Price per unit:  1.13
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Moosfrui8805mB:
	 Moosewood
	 fruits
	 Simply Fresh
		Number in stock: 963
		Price per unit:  0.86
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
MoosfruiMXEGex:
	 Moosewood
	 fruits
	 TriCounty Produce
		Number in stock: 969
		Price per unit:  0.86
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
MoosfruiOsnDFL:
	 Moosewood
	 fruits
	 Off the Vine
		Number in stock: 594
		Price per unit:  0.88
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Mousdess0ujevx:
	 Mousse, Cherry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  12.43
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Mousdess1De9oL:
	 Mousse, Chocolate Banana
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.08
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Mousdess8FyFT8:
	 Mousse, Chocolate
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.96
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
MousdessDpN4sQ:
	 Mousse, Chocolate
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  6.25
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
MousdessHCDlBK:
	 Mousse, Strawberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  5.58
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
MousdessIeW4qz:
	 Mousse, Chocolate Banana
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  5.13
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
MousdessSZ4PyW:
	 Mousse, Strawberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.36
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
MousdessZ38hXj:
	 Mousse, Eggnog
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  9.07
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
MousdessacwrkO:
	 Mousse, Blueberry Maple
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  7.28
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
MousdessbiCMFg:
	 Mousse, Blueberry Maple
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  7.21
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Mousdesshs05ST:
	 Mousse, Eggnog
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  8.81
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Mousdesss1bF8H:
	 Mousse, Cherry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  13.05
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
MungvegeGxNxQC:
	 Munggo
	 vegetables
	 Off the Vine
		Number in stock: 555
		Price per unit:  0.25
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
MungvegeNhqWvL:
	 Munggo
	 vegetables
	 TriCounty Produce
		Number in stock: 360
		Price per unit:  0.26
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
MungvegeqeuwGw:
	 Munggo
	 vegetables
	 The Pantry
		Number in stock: 362
		Price per unit:  0.25
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Mushvege8o27D2:
	 Mushroom
	 vegetables
	 Off the Vine
		Number in stock: 467
		Price per unit:  0.55
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
MushvegeSq53h8:
	 Mushroom
	 vegetables
	 The Pantry
		Number in stock: 365
		Price per unit:  0.59
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Mushvegedq6lYP:
	 Mushroom
	 vegetables
	 TriCounty Produce
		Number in stock: 444
		Price per unit:  0.59
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
NatafruiOhqRrd:
	 Natal Orange
	 fruits
	 Simply Fresh
		Number in stock: 982
		Price per unit:  0.42
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
NatafruiRObMf6:
	 Natal Orange
	 fruits
	 Off the Vine
		Number in stock: 268
		Price per unit:  0.41
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
NatafruitB8Kh2:
	 Natal Orange
	 fruits
	 TriCounty Produce
		Number in stock: 332
		Price per unit:  0.42
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Nectfrui5U7U96:
	 Nectarine
	 fruits
	 Off the Vine
		Number in stock: 930
		Price per unit:  0.37
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
NectfruiQfjt6b:
	 Nectarine
	 fruits
	 Simply Fresh
		Number in stock: 818
		Price per unit:  0.35
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
NectfruilNfeD8:
	 Nectarine
	 fruits
	 TriCounty Produce
		Number in stock: 601
		Price per unit:  0.36
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
NeemfruiCruEMF:
	 Neem Tree
	 fruits
	 TriCounty Produce
		Number in stock: 222
		Price per unit:  0.24
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
NeemfruiGv0pv5:
	 Neem Tree
	 fruits
	 Simply Fresh
		Number in stock: 645
		Price per unit:  0.24
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
NeemfruiUFPVfk:
	 Neem Tree
	 fruits
	 Off the Vine
		Number in stock: 601
		Price per unit:  0.25
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
New fruiaoR9TP:
	 New Zealand Spinach
	 fruits
	 Simply Fresh
		Number in stock: 630
		Price per unit:  0.87
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
New fruihDIgec:
	 New Zealand Spinach
	 fruits
	 TriCounty Produce
		Number in stock: 428
		Price per unit:  0.87
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
New fruiy8LBul:
	 New Zealand Spinach
	 fruits
	 Off the Vine
		Number in stock: 570
		Price per unit:  0.94
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OkravegeD6tF9n:
	 Okra
	 vegetables
	 Off the Vine
		Number in stock: 77
		Price per unit:  0.55
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OkravegeJBWmfh:
	 Okra
	 vegetables
	 TriCounty Produce
		Number in stock: 165
		Price per unit:  0.58
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
OkravegeTszQSL:
	 Okra
	 vegetables
	 The Pantry
		Number in stock: 62
		Price per unit:  0.55
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
OlosfruiESlpB3:
	 Olosapo
	 fruits
	 Simply Fresh
		Number in stock: 560
		Price per unit:  0.76
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
OlosfruiFNEkER:
	 Olosapo
	 fruits
	 Off the Vine
		Number in stock: 962
		Price per unit:  0.76
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OlosfruiGXvaMm:
	 Olosapo
	 fruits
	 TriCounty Produce
		Number in stock: 388
		Price per unit:  0.76
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
OniovegeUOwwks:
	 Onion
	 vegetables
	 TriCounty Produce
		Number in stock: 417
		Price per unit:  0.8
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
OniovegejwimQo:
	 Onion
	 vegetables
	 The Pantry
		Number in stock: 186
		Price per unit:  0.8
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
OniovegezcRDrc:
	 Onion
	 vegetables
	 Off the Vine
		Number in stock: 435
		Price per unit:  0.8
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OranfruiLEuzQj:
	 Oranges
	 fruits
	 Off the Vine
		Number in stock: 261
		Price per unit:  0.69
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OranfruiRu6Ghr:
	 Oranges
	 fruits
	 TriCounty Produce
		Number in stock: 451
		Price per unit:  0.71
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
OranfruiXRPFn1:
	 Oranges
	 fruits
	 Simply Fresh
		Number in stock: 263
		Price per unit:  0.73
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
OregfruiC5UCxX:
	 Oregon Grape
	 fruits
	 Off the Vine
		Number in stock: 419
		Price per unit:  1.17
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OregfruiMgjHUn:
	 Oregon Grape
	 fruits
	 Simply Fresh
		Number in stock: 959
		Price per unit:  1.2
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
OregfruiWxhzrf:
	 Oregon Grape
	 fruits
	 TriCounty Produce
		Number in stock: 892
		Price per unit:  1.14
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Oregvege9h9ZKy:
	 Oregano
	 vegetables
	 TriCounty Produce
		Number in stock: 173
		Price per unit:  0.7
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
OregvegebXr0PJ:
	 Oregano
	 vegetables
	 Off the Vine
		Number in stock: 773
		Price per unit:  0.7
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OregvegetlU7Ez:
	 Oregano
	 vegetables
	 The Pantry
		Number in stock: 119
		Price per unit:  0.71
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Otahfrui92PyMY:
	 Otaheite Apple
	 fruits
	 Simply Fresh
		Number in stock: 857
		Price per unit:  0.22
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
OtahfruiLGD1EH:
	 Otaheite Apple
	 fruits
	 Off the Vine
		Number in stock: 807
		Price per unit:  0.2
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OtahfruilT0iFj:
	 Otaheite Apple
	 fruits
	 TriCounty Produce
		Number in stock: 579
		Price per unit:  0.21
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Oystfrui1kudBX:
	 Oyster Plant
	 fruits
	 Simply Fresh
		Number in stock: 989
		Price per unit:  0.81
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
OystfruiaX3uO2:
	 Oyster Plant
	 fruits
	 Off the Vine
		Number in stock: 505
		Price per unit:  0.8
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
OystfruimGxOsj:
	 Oyster Plant
	 fruits
	 TriCounty Produce
		Number in stock: 835
		Price per unit:  0.77
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PanafruiZG0Vp4:
	 Panama Berry
	 fruits
	 TriCounty Produce
		Number in stock: 288
		Price per unit:  1.19
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PanafruiobvXPE:
	 Panama Berry
	 fruits
	 Simply Fresh
		Number in stock: 541
		Price per unit:  1.21
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
PanafruipaW8F3:
	 Panama Berry
	 fruits
	 Off the Vine
		Number in stock: 471
		Price per unit:  1.16
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
ParsvegeSxXHSA:
	 Parsnip
	 vegetables
	 TriCounty Produce
		Number in stock: 411
		Price per unit:  0.47
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
ParsvegeXFEjjN:
	 Parsley
	 vegetables
	 The Pantry
		Number in stock: 502
		Price per unit:  0.83
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Parsvegea0stPf:
	 Parsnip
	 vegetables
	 Off the Vine
		Number in stock: 403
		Price per unit:  0.44
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Parsvegee9Lp6D:
	 Parsnip
	 vegetables
	 The Pantry
		Number in stock: 626
		Price per unit:  0.46
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
ParsvegehAtH2H:
	 Parsley
	 vegetables
	 Off the Vine
		Number in stock: 523
		Price per unit:  0.84
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
ParsvegejAg5C4:
	 Parsley
	 vegetables
	 TriCounty Produce
		Number in stock: 454
		Price per unit:  0.8
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PeacfruiILDYAp:
	 Peach Tomato
	 fruits
	 Off the Vine
		Number in stock: 876
		Price per unit:  1.23
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
PeacfruiQpovYH:
	 Peach Tomato
	 fruits
	 TriCounty Produce
		Number in stock: 475
		Price per unit:  1.2
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PeacfruixYXLTN:
	 Peach Tomato
	 fruits
	 Simply Fresh
		Number in stock: 655
		Price per unit:  1.18
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Peanfrui7jeRN2:
	 Peanut Butter Fruit
	 fruits
	 Off the Vine
		Number in stock: 938
		Price per unit:  0.27
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
PeanfruiEimbED:
	 Peanut
	 fruits
	 Simply Fresh
		Number in stock: 307
		Price per unit:  0.65
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
PeanfruiST0T0R:
	 Peanut Butter Fruit
	 fruits
	 Simply Fresh
		Number in stock: 910
		Price per unit:  0.27
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Peanfruic452Vc:
	 Peanut
	 fruits
	 Off the Vine
		Number in stock: 937
		Price per unit:  0.68
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
PeanfruixEDt9Y:
	 Peanut Butter Fruit
	 fruits
	 TriCounty Produce
		Number in stock: 628
		Price per unit:  0.27
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Peanfruiy8M7pt:
	 Peanut
	 fruits
	 TriCounty Produce
		Number in stock: 275
		Price per unit:  0.69
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PearfruiA93XZx:
	 Pear
	 fruits
	 Simply Fresh
		Number in stock: 333
		Price per unit:  0.21
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
PearfruiB5YmSJ:
	 Pear
	 fruits
	 TriCounty Produce
		Number in stock: 945
		Price per unit:  0.2
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PearfruioNKiIf:
	 Pear
	 fruits
	 Off the Vine
		Number in stock: 715
		Price per unit:  0.21
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Peavege46Gdp9:
	 Pea
	 vegetables
	 TriCounty Produce
		Number in stock: 255
		Price per unit:  0.18
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Peavegecq4SxR:
	 Pea
	 vegetables
	 The Pantry
		Number in stock: 342
		Price per unit:  0.18
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Peavegeov1gc5:
	 Pea
	 vegetables
	 Off the Vine
		Number in stock: 251
		Price per unit:  0.18
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Pecafrui1szYz2:
	 Pecan
	 fruits
	 Off the Vine
		Number in stock: 929
		Price per unit:  0.25
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
PecafruiMGkqla:
	 Pecan
	 fruits
	 Simply Fresh
		Number in stock: 889
		Price per unit:  0.26
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
PecafruiiTIv1Z:
	 Pecan
	 fruits
	 TriCounty Produce
		Number in stock: 471
		Price per unit:  0.26
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Pechvege8Pq8Eo:
	 Pechay
	 vegetables
	 Off the Vine
		Number in stock: 141
		Price per unit:  0.36
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Pechvegehi4Fcx:
	 Pechay
	 vegetables
	 TriCounty Produce
		Number in stock: 723
		Price per unit:  0.35
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PechvegezDeHFZ:
	 Pechay
	 vegetables
	 The Pantry
		Number in stock: 401
		Price per unit:  0.36
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
PeppvegeB60btP:
	 Pepper
	 vegetables
	 TriCounty Produce
		Number in stock: 107
		Price per unit:  0.35
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
PeppvegeG4tP3e:
	 Pepper
	 vegetables
	 Off the Vine
		Number in stock: 481
		Price per unit:  0.34
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
PeppvegeUcBYRp:
	 Pepper
	 vegetables
	 The Pantry
		Number in stock: 52
		Price per unit:  0.33
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Pie,dess1mL7IS:
	 Pie, Cranberry Apple
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  10.16
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dess2NqhNR:
	 Pie, Pecan
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  12.7
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dess9naVkX:
	 Pie, Lemon Meringue
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  3.74
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dessB1LfcE:
	 Pie, Pecan
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  12.33
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessDg3NWl:
	 Pie, Pumpkin
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  6.24
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessH80DuG:
	 Pie, Banana Cream
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  7.35
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dessJflbf5:
	 Pie, Raspberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  2.36
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessKYcNML:
	 Pie, Lemon Meringue
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  3.67
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessMwQkKm:
	 Pie, Key Lime
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.13
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessSUuiIU:
	 Pie, Caramel
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  2.27
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dessTbiwDp:
	 Pie, Apple
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  7.88
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessUHhMlS:
	 Pie, Raspberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  2.36
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dessf1YvFb:
	 Pie, Banana Cream
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  7.08
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,desshcSHhT:
	 Pie, Apple
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  7.88
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,desshtli5N:
	 Pie, Key Lime
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.85
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,dessiSjZKD:
	 Pie, Blueberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  2.12
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessmDhkUA:
	 Pie, Cranberry Apple
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  10.16
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessvo8uHh:
	 Pie, Caramel
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  2.33
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Pie,dessw9VdgD:
	 Pie, Blueberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  2.14
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pie,desswhPBPB:
	 Pie, Pumpkin
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  6.0
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Pigevegeb93eLi:
	 Pigeon Pea
	 vegetables
	 TriCounty Produce
		Number in stock: 447
		Price per unit:  0.91
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Pigevegec5bAtm:
	 Pigeon Pea
	 vegetables
	 The Pantry
		Number in stock: 391
		Price per unit:  0.94
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
PigevegejEBDRa:
	 Pigeon Pea
	 vegetables
	 Off the Vine
		Number in stock: 259
		Price per unit:  0.89
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Pumpvegeb3nQU5:
	 Pumpkin
	 vegetables
	 Off the Vine
		Number in stock: 207
		Price per unit:  0.26
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
PumpvegeiYsPR8:
	 Pumpkin
	 vegetables
	 The Pantry
		Number in stock: 776
		Price per unit:  0.25
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
PumpvegelqP1Kh:
	 Pumpkin
	 vegetables
	 TriCounty Produce
		Number in stock: 189
		Price per unit:  0.25
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Purpfrui4mMGkD:
	 Purple Passion Fruit
	 fruits
	 TriCounty Produce
		Number in stock: 914
		Price per unit:  1.04
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Purpfrui5XOW3K:
	 Purple Passion Fruit
	 fruits
	 Simply Fresh
		Number in stock: 423
		Price per unit:  1.06
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
PurpfruifDTAgW:
	 Purple Passion Fruit
	 fruits
	 Off the Vine
		Number in stock: 549
		Price per unit:  1.05
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Radivege0tIBnL:
	 Radish
	 vegetables
	 TriCounty Produce
		Number in stock: 779
		Price per unit:  0.16
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
RadivegeNLqJCf:
	 Radish
	 vegetables
	 Off the Vine
		Number in stock: 731
		Price per unit:  0.16
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
RadivegeNwwSBJ:
	 Radish
	 vegetables
	 The Pantry
		Number in stock: 613
		Price per unit:  0.16
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Red frui0jl9mg:
	 Red Princess
	 fruits
	 Off the Vine
		Number in stock: 252
		Price per unit:  0.24
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Red fruiUseWLG:
	 Red Mulberry
	 fruits
	 Off the Vine
		Number in stock: 795
		Price per unit:  1.21
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Red fruiVLOXIW:
	 Red Mulberry
	 fruits
	 TriCounty Produce
		Number in stock: 270
		Price per unit:  1.24
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Red fruiXNXt4a:
	 Red Mulberry
	 fruits
	 Simply Fresh
		Number in stock: 836
		Price per unit:  1.21
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Red fruigJLR4V:
	 Red Princess
	 fruits
	 TriCounty Produce
		Number in stock: 829
		Price per unit:  0.23
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Red fruinVKps5:
	 Red Princess
	 fruits
	 Simply Fresh
		Number in stock: 558
		Price per unit:  0.23
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Rhubvege4Jc3b7:
	 Rhubarb
	 vegetables
	 TriCounty Produce
		Number in stock: 557
		Price per unit:  0.12
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
RhubvegeREfOti:
	 Rhubarb
	 vegetables
	 The Pantry
		Number in stock: 301
		Price per unit:  0.12
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
RhubvegeaXqF7H:
	 Rhubarb
	 vegetables
	 Off the Vine
		Number in stock: 378
		Price per unit:  0.12
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Rosevege16QStc:
	 Rosemary
	 vegetables
	 The Pantry
		Number in stock: 380
		Price per unit:  0.73
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
RosevegeFgsOyN:
	 Rosemary
	 vegetables
	 Off the Vine
		Number in stock: 631
		Price per unit:  0.74
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
RosevegeNf6Oem:
	 Rosemary
	 vegetables
	 TriCounty Produce
		Number in stock: 622
		Price per unit:  0.75
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
RutavegecUYfQ3:
	 Rutabaga
	 vegetables
	 The Pantry
		Number in stock: 676
		Price per unit:  0.55
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
RutavegejOG5DF:
	 Rutabaga
	 vegetables
	 TriCounty Produce
		Number in stock: 273
		Price per unit:  0.55
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
RutavegewEVjzV:
	 Rutabaga
	 vegetables
	 Off the Vine
		Number in stock: 452
		Price per unit:  0.53
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SalsvegeViS9HF:
	 Salsify
	 vegetables
	 The Pantry
		Number in stock: 537
		Price per unit:  0.11
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Salsvegemd3HAL:
	 Salsify
	 vegetables
	 TriCounty Produce
		Number in stock: 753
		Price per unit:  0.11
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SalsvegeuRCnmq:
	 Salsify
	 vegetables
	 Off the Vine
		Number in stock: 787
		Price per unit:  0.1
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Savovegee4DRWl:
	 Savory
	 vegetables
	 The Pantry
		Number in stock: 456
		Price per unit:  0.21
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Savovegeje7yy7:
	 Savory
	 vegetables
	 Off the Vine
		Number in stock: 328
		Price per unit:  0.22
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SavovegerZ90Xm:
	 Savory
	 vegetables
	 TriCounty Produce
		Number in stock: 642
		Price per unit:  0.21
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Sesavege4NAWZE:
	 Sesame
	 vegetables
	 The Pantry
		Number in stock: 54
		Price per unit:  0.84
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SesavegeMTc9IN:
	 Sesame
	 vegetables
	 TriCounty Produce
		Number in stock: 458
		Price per unit:  0.84
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SesavegegOwAjo:
	 Sesame
	 vegetables
	 Off the Vine
		Number in stock: 125
		Price per unit:  0.83
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
ShalvegeSDC8VY:
	 Shallots
	 vegetables
	 Off the Vine
		Number in stock: 369
		Price per unit:  0.27
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
ShalvegeUO2pDO:
	 Shallots
	 vegetables
	 The Pantry
		Number in stock: 599
		Price per unit:  0.26
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
ShalvegeY1sekb:
	 Shallots
	 vegetables
	 TriCounty Produce
		Number in stock: 647
		Price per unit:  0.27
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Sherdess1JVFOS:
	 Sherbet, Lemon Milk
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  7.57
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Sherdess3DCxUg:
	 Sherbet, Cantaloupe
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  3.11
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Sherdess8W8Mb9:
	 Sherbet, Orange Crush
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.32
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
SherdessC865vu:
	 Sherbet, Lemon Milk
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  7.57
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
SherdessFAgxqp:
	 Sherbet, Blueberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  3.46
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
SherdessFwv09m:
	 Sherbet, Strawberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.63
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
SherdessKB0H7q:
	 Sherbet, Strawberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  4.81
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
SherdessMPL87u:
	 Sherbet, Blueberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  3.6
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Sherdesscp2VIz:
	 Sherbet, Cantaloupe
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  2.99
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Sherdesse86ugA:
	 Sherbet, Raspberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  6.08
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Sherdesslc1etR:
	 Sherbet, Raspberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.85
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
SherdessxmVJBF:
	 Sherbet, Orange Crush
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  4.16
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
SigavegeLhsoOB:
	 Sigarilyas
	 vegetables
	 TriCounty Produce
		Number in stock: 768
		Price per unit:  0.87
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SigavegeMJrtlV:
	 Sigarilyas
	 vegetables
	 The Pantry
		Number in stock: 335
		Price per unit:  0.88
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SigavegeS6RJcA:
	 Sigarilyas
	 vegetables
	 Off the Vine
		Number in stock: 356
		Price per unit:  0.93
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Sitavege0BCNeF:
	 Sitaw
	 vegetables
	 Off the Vine
		Number in stock: 674
		Price per unit:  0.66
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Sitavege0hMi9z:
	 Sitaw
	 vegetables
	 The Pantry
		Number in stock: 153
		Price per unit:  0.65
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Sitavegeez1g6N:
	 Sitaw
	 vegetables
	 TriCounty Produce
		Number in stock: 561
		Price per unit:  0.67
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Snakvegec5n1UM:
	 Snake gourd
	 vegetables
	 Off the Vine
		Number in stock: 143
		Price per unit:  0.92
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SnakvegedlNiBk:
	 Snake gourd
	 vegetables
	 TriCounty Produce
		Number in stock: 669
		Price per unit:  0.92
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SnakvegesfHGvt:
	 Snake gourd
	 vegetables
	 The Pantry
		Number in stock: 626
		Price per unit:  0.92
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SorbdessQoa0CE:
	 Sorbet, Blackberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  9.88
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
SorbdessqoOYzv:
	 Sorbet, Blackberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  9.78
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
SoybvegebanSFq:
	 Soybean
	 vegetables
	 Off the Vine
		Number in stock: 268
		Price per unit:  0.67
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SoybvegeqxSVRL:
	 Soybean
	 vegetables
	 The Pantry
		Number in stock: 639
		Price per unit:  0.7
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SoybvegezEMjOG:
	 Soybean
	 vegetables
	 TriCounty Produce
		Number in stock: 423
		Price per unit:  0.68
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SpagvegeAOoZNX:
	 Spaghetti Squash
	 vegetables
	 Off the Vine
		Number in stock: 431
		Price per unit:  0.13
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SpagvegeMNO1yC:
	 Spaghetti Squash
	 vegetables
	 The Pantry
		Number in stock: 753
		Price per unit:  0.12
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SpagvegeilpUaD:
	 Spaghetti Squash
	 vegetables
	 TriCounty Produce
		Number in stock: 604
		Price per unit:  0.13
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SpinvegeVcqXL6:
	 Spinach
	 vegetables
	 TriCounty Produce
		Number in stock: 708
		Price per unit:  0.11
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SpinvegeegXXou:
	 Spinach
	 vegetables
	 The Pantry
		Number in stock: 742
		Price per unit:  0.1
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SpinvegetZ26DN:
	 Spinach
	 vegetables
	 Off the Vine
		Number in stock: 625
		Price per unit:  0.11
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
StrifruiUKzjoU:
	 Striped Screw Pine
	 fruits
	 TriCounty Produce
		Number in stock: 226
		Price per unit:  0.6
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
StrifruiiF7CGH:
	 Striped Screw Pine
	 fruits
	 Off the Vine
		Number in stock: 983
		Price per unit:  0.6
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
StrifruivWLDzH:
	 Striped Screw Pine
	 fruits
	 Simply Fresh
		Number in stock: 685
		Price per unit:  0.64
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Sugavege1XyzNH:
	 Sugar Snap Peas
	 vegetables
	 TriCounty Produce
		Number in stock: 205
		Price per unit:  0.48
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SugavegeJuaG7f:
	 Sugar Snap Peas
	 vegetables
	 Off the Vine
		Number in stock: 348
		Price per unit:  0.46
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SugavegepUZDTl:
	 Sugar Snap Peas
	 vegetables
	 The Pantry
		Number in stock: 308
		Price per unit:  0.47
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
SweevegepNDQWb:
	 Sweet Potato
	 vegetables
	 The Pantry
		Number in stock: 720
		Price per unit:  0.94
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Sweevegepnw7Tm:
	 Sweet Potato
	 vegetables
	 TriCounty Produce
		Number in stock: 377
		Price per unit:  0.9
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Sweevegeyk0C82:
	 Sweet Potato
	 vegetables
	 Off the Vine
		Number in stock: 242
		Price per unit:  0.89
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SwisvegeKm2Kze:
	 Swiss Chard
	 vegetables
	 TriCounty Produce
		Number in stock: 472
		Price per unit:  0.54
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
SwisvegehteuMk:
	 Swiss Chard
	 vegetables
	 Off the Vine
		Number in stock: 142
		Price per unit:  0.56
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
SwisvegeksalTA:
	 Swiss Chard
	 vegetables
	 The Pantry
		Number in stock: 545
		Price per unit:  0.54
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
TalovegeO3U2ze:
	 Talong
	 vegetables
	 Off the Vine
		Number in stock: 126
		Price per unit:  0.1
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
TalovegevZjVK6:
	 Talong
	 vegetables
	 The Pantry
		Number in stock: 530
		Price per unit:  0.1
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
TalovegexX4MRw:
	 Talong
	 vegetables
	 TriCounty Produce
		Number in stock: 305
		Price per unit:  0.09
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
TapifruiZ6Igg3:
	 Tapioca
	 fruits
	 Off the Vine
		Number in stock: 655
		Price per unit:  0.41
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Tapifruib4LCqt:
	 Tapioca
	 fruits
	 TriCounty Produce
		Number in stock: 955
		Price per unit:  0.4
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
TapifruiwgQLj9:
	 Tapioca
	 fruits
	 Simply Fresh
		Number in stock: 889
		Price per unit:  0.41
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Tarovege3fpGV6:
	 Taro
	 vegetables
	 The Pantry
		Number in stock: 155
		Price per unit:  0.87
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
TarovegeXKPuzc:
	 Taro
	 vegetables
	 Off the Vine
		Number in stock: 443
		Price per unit:  0.89
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
TarovegerZkmof:
	 Taro
	 vegetables
	 TriCounty Produce
		Number in stock: 371
		Price per unit:  0.86
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
TarrvegeCzVC6U:
	 Tarragon
	 vegetables
	 The Pantry
		Number in stock: 491
		Price per unit:  0.18
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
TarrvegerZsKFP:
	 Tarragon
	 vegetables
	 Off the Vine
		Number in stock: 180
		Price per unit:  0.18
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
TarrvegesIkEfS:
	 Tarragon
	 vegetables
	 TriCounty Produce
		Number in stock: 65
		Price per unit:  0.17
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Tartdess1V1A1c:
	 Tart, Almond
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  6.68
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Tartdess2BeEDb:
	 Tart, Strawberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  4.61
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Tartdess2pdOE4:
	 Tart, Chocolate-Pear
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  5.67
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Tartdess4IUcZW:
	 Tart, Strawberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.75
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Tartdess4a1BUc:
	 Tart, Pear
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  10.09
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Tartdess5fqxgy:
	 Tart, Raspberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  1.94
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Tartdess6YXJec:
	 Tart, Pecan
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  11.04
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Tartdess9DhZUT:
	 Tart, Lemon Fudge
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  3.88
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdessA2Wftr:
	 Tart, Pineapple
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  8.44
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TartdessAVnpP6:
	 Tart, Raspberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  6.18
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdessC7FARL:
	 Tart, Almond
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  6.62
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdessL3aEDd:
	 Tart, Chocolate-Pear
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.51
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TartdessNw8YPG:
	 Tart, Pear
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  10.68
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TartdessUSJSuc:
	 Tart, Blueberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  10.28
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TartdesseMfJFe:
	 Tart, Pineapple
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  9.01
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdessfVxZFf:
	 Tart, Raspberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.95
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TartdesshyBd24:
	 Tart, Raspberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  1.85
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdesshzLOWt:
	 Tart, Lemon Fudge
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  3.96
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Tartdessp7pyiy:
	 Tart, Apple
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  3.13
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TartdessrsTyXA:
	 Tart, Apple
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  3.35
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdesssQZRXX:
	 Tart, Blueberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  10.28
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TartdessvSbXzd:
	 Tart, Pecan
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  11.8
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Tavofrui0k9XOt:
	 Tavola
	 fruits
	 TriCounty Produce
		Number in stock: 938
		Price per unit:  1.16
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Tavofrui8DuRxL:
	 Tavola
	 fruits
	 Simply Fresh
		Number in stock: 979
		Price per unit:  1.08
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
TavofruiNZEuJZ:
	 Tavola
	 fruits
	 Off the Vine
		Number in stock: 215
		Price per unit:  1.16
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
TeafruiD5soTf:
	 Tea
	 fruits
	 Simply Fresh
		Number in stock: 970
		Price per unit:  1.13
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
TeafruiL0357s:
	 Tea
	 fruits
	 TriCounty Produce
		Number in stock: 516
		Price per unit:  1.11
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
TeafruiOWq4oO:
	 Tea
	 fruits
	 Off the Vine
		Number in stock: 357
		Price per unit:  1.19
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Thymvege8Rv72c:
	 Thyme
	 vegetables
	 The Pantry
		Number in stock: 442
		Price per unit:  0.41
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
ThymvegeJoUdQS:
	 Thyme
	 vegetables
	 TriCounty Produce
		Number in stock: 237
		Price per unit:  0.42
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
ThymvegeRck5uO:
	 Thyme
	 vegetables
	 Off the Vine
		Number in stock: 491
		Price per unit:  0.43
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
TogevegeYelJUw:
	 Toge
	 vegetables
	 The Pantry
		Number in stock: 449
		Price per unit:  0.54
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Togevegeilr1xK:
	 Toge
	 vegetables
	 TriCounty Produce
		Number in stock: 274
		Price per unit:  0.54
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Togevegesvjnyn:
	 Toge
	 vegetables
	 Off the Vine
		Number in stock: 316
		Price per unit:  0.51
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
TomavegeKAjRUn:
	 Tomato
	 vegetables
	 TriCounty Produce
		Number in stock: 630
		Price per unit:  0.3
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
TomavegePZOHlH:
	 Tomato
	 vegetables
	 Off the Vine
		Number in stock: 70
		Price per unit:  0.3
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Tomavegey0NHGK:
	 Tomato
	 vegetables
	 The Pantry
		Number in stock: 60
		Price per unit:  0.31
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Trifdess1rtW0A:
	 Trifle, Strawberry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  3.58
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
Trifdess2zJsGi:
	 Trifle, Scotch Whiskey
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  5.44
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
Trifdess52l955:
	 Trifle, English
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  8.12
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessAAUQCN:
	 Trifle, Scottish
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  14.03
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessAV9Ix8:
	 Trifle, Berry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  12.6
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessAd5TpV:
	 Trifle, Strawberry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  3.58
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdessFa0JdK:
	 Trifle, Scottish
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  13.63
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdessFrfCHP:
	 Trifle, Orange
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  10.22
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessJKFN96:
	 Trifle, Pumpkin
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.72
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdessL8nuI6:
	 Trifle, Scotch Whiskey
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  5.18
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessMNw4EV:
	 Trifle, Pumpkin
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  4.95
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessTArskm:
	 Trifle, American
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  4.35
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TrifdessX87q8T:
	 Trifle, English
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  8.2
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdesscsdSCd:
	 Trifle, American
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.7
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdesscuttJg:
	 Trifle, Sherry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  4.42
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdesslUwxwe:
	 Trifle, Orange
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  9.74
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdessmEkbU2:
	 Trifle, Berry
	 desserts
	 Mom's Kitchen
		Number in stock: 0
		Price per unit:  12.48
		Contact: 
		 53 Yerman Ct.
		 Middle Town, MN 55432
		 Business Phone: 763 554 9200
		 Sales Rep: Maggie Kultgen
		            763 554 9200 x12
TrifdesspRGpfP:
	 Trifle, Sherry
	 desserts
	 The Baking Pan
		Number in stock: 0
		Price per unit:  4.21
		Contact: 
		 1415 53rd Ave.
		 Dutchin, MN 56304
		 Business Phone: 320 442 2277
		 Sales Rep: Mike Roan
		            320 442 6879
TurnvegeRVQiV5:
	 Turnip
	 vegetables
	 The Pantry
		Number in stock: 580
		Price per unit:  0.44
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
TurnvegeVjIX9D:
	 Turnip
	 vegetables
	 TriCounty Produce
		Number in stock: 743
		Price per unit:  0.45
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
TurnvegelFhvuJ:
	 Turnip
	 vegetables
	 Off the Vine
		Number in stock: 219
		Price per unit:  0.44
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Ubevege2CNyve:
	 Ube
	 vegetables
	 TriCounty Produce
		Number in stock: 450
		Price per unit:  0.55
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
UbevegeC43sVj:
	 Ube
	 vegetables
	 Off the Vine
		Number in stock: 263
		Price per unit:  0.55
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
UbevegeoPnxvb:
	 Ube
	 vegetables
	 The Pantry
		Number in stock: 397
		Price per unit:  0.56
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
UglifruifbDrzc:
	 Ugli Fruit
	 fruits
	 Simply Fresh
		Number in stock: 642
		Price per unit:  0.24
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
UglifruipKNCpf:
	 Ugli Fruit
	 fruits
	 TriCounty Produce
		Number in stock: 501
		Price per unit:  0.24
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Uglifruiwx8or4:
	 Ugli Fruit
	 fruits
	 Off the Vine
		Number in stock: 280
		Price per unit:  0.24
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
UpovegecOGRqC:
	 Upo
	 vegetables
	 The Pantry
		Number in stock: 404
		Price per unit:  0.22
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Upovegekjl2wl:
	 Upo
	 vegetables
	 TriCounty Produce
		Number in stock: 541
		Price per unit:  0.22
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
UpovegemTTTwI:
	 Upo
	 vegetables
	 Off the Vine
		Number in stock: 459
		Price per unit:  0.23
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
VegefruiKBfzN0:
	 Vegetable Brain
	 fruits
	 Off the Vine
		Number in stock: 453
		Price per unit:  0.72
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
VegefruieXLBoc:
	 Vegetable Brain
	 fruits
	 TriCounty Produce
		Number in stock: 355
		Price per unit:  0.73
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Vegefruik5FSdl:
	 Vegetable Brain
	 fruits
	 Simply Fresh
		Number in stock: 498
		Price per unit:  0.71
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Wasavege1ve7TY:
	 Wasabi
	 vegetables
	 Off the Vine
		Number in stock: 61
		Price per unit:  0.65
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Wasavege5P5pZp:
	 Wasabi
	 vegetables
	 The Pantry
		Number in stock: 751
		Price per unit:  0.67
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
Wasavege6EEE9r:
	 Wasabi
	 vegetables
	 TriCounty Produce
		Number in stock: 559
		Price per unit:  0.68
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Watevege8oeDCT:
	 Watercress
	 vegetables
	 TriCounty Produce
		Number in stock: 774
		Price per unit:  0.54
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
WatevegeL83MRH:
	 Watermelon
	 vegetables
	 The Pantry
		Number in stock: 698
		Price per unit:  0.19
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
WatevegeR2S4Dq:
	 Watermelon
	 vegetables
	 TriCounty Produce
		Number in stock: 488
		Price per unit:  0.21
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
WatevegelwzPLQ:
	 Watercress
	 vegetables
	 The Pantry
		Number in stock: 230
		Price per unit:  0.54
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
WatevegepFPXQu:
	 Watermelon
	 vegetables
	 Off the Vine
		Number in stock: 439
		Price per unit:  0.21
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Watevegexr8L1t:
	 Watercress
	 vegetables
	 Off the Vine
		Number in stock: 185
		Price per unit:  0.55
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
WhitfruiHygydw:
	 White Walnut
	 fruits
	 Simply Fresh
		Number in stock: 913
		Price per unit:  0.3
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
WhitfruieNtplo:
	 White Walnut
	 fruits
	 Off the Vine
		Number in stock: 401
		Price per unit:  0.3
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Whitfruit3oVHL:
	 White Walnut
	 fruits
	 TriCounty Produce
		Number in stock: 501
		Price per unit:  0.3
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Woodfrui4Zk69T:
	 Wood Apple
	 fruits
	 Simply Fresh
		Number in stock: 616
		Price per unit:  0.68
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
WoodfruijVPRqA:
	 Wood Apple
	 fruits
	 TriCounty Produce
		Number in stock: 501
		Price per unit:  0.68
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
WoodfruiuSLHZK:
	 Wood Apple
	 fruits
	 Off the Vine
		Number in stock: 474
		Price per unit:  0.7
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
YamvegeI1AnyI:
	 Yam
	 vegetables
	 Off the Vine
		Number in stock: 456
		Price per unit:  0.56
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
YamvegeRN9ONH:
	 Yam
	 vegetables
	 The Pantry
		Number in stock: 438
		Price per unit:  0.57
		Contact: 
		 1206 N. Creek Way
		 Middle Town, MN 55432
		 Business Phone: 763 555 3391
		 Sales Rep: Sully Beckstrom
		            763 555 3391
YamvegeWjdzeA:
	 Yam
	 vegetables
	 TriCounty Produce
		Number in stock: 564
		Price per unit:  0.56
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Yellfrui0DiPqa:
	 Yellow Horn
	 fruits
	 Simply Fresh
		Number in stock: 517
		Price per unit:  1.13
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Yellfrui0ljvqC:
	 Yellow Horn
	 fruits
	 Off the Vine
		Number in stock: 853
		Price per unit:  1.14
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Yellfrui4J2mke:
	 Yellow Sapote
	 fruits
	 Simply Fresh
		Number in stock: 269
		Price per unit:  0.88
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
Yellfrui5igjjf:
	 Yellow Horn
	 fruits
	 TriCounty Produce
		Number in stock: 729
		Price per unit:  1.18
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Yellfrui6PuXaL:
	 Yellow Sapote
	 fruits
	 Off the Vine
		Number in stock: 575
		Price per unit:  0.86
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
YellfruilGmCfq:
	 Yellow Sapote
	 fruits
	 TriCounty Produce
		Number in stock: 204
		Price per unit:  0.93
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Ylanfrui3rmByO:
	 Ylang-ylang
	 fruits
	 TriCounty Produce
		Number in stock: 429
		Price per unit:  0.76
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
YlanfruiA80Nkq:
	 Ylang-ylang
	 fruits
	 Simply Fresh
		Number in stock: 886
		Price per unit:  0.76
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
YlanfruinUEm5d:
	 Ylang-ylang
	 fruits
	 Off the Vine
		Number in stock: 747
		Price per unit:  0.72
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
ZapofruiAe6Eu1:
	 Zapote Blanco
	 fruits
	 Off the Vine
		Number in stock: 255
		Price per unit:  0.68
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
ZapofruilKxl7N:
	 Zapote Blanco
	 fruits
	 Simply Fresh
		Number in stock: 924
		Price per unit:  0.65
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
ZapofruisZ5sMA:
	 Zapote Blanco
	 fruits
	 TriCounty Produce
		Number in stock: 428
		Price per unit:  0.67
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
Zulufrui0LJnWK:
	 Zulu Nut
	 fruits
	 Off the Vine
		Number in stock: 858
		Price per unit:  0.71
		Contact: 
		 133 American Ct.
		 Centennial, IA 52002
		 Business Phone: 563 121 3800
		 Sales Rep: Bob King
		            563 121 3800 x54
Zulufrui469K4k:
	 Zulu Nut
	 fruits
	 TriCounty Produce
		Number in stock: 445
		Price per unit:  0.71
		Contact: 
		 309 S. Main Street
		 Middle Town, MN 55432
		 Business Phone: 763 555 5761
		 Sales Rep: Mort Dufresne
		            763 555 5765
ZulufruiWbz6vU:
	 Zulu Nut
	 fruits
	 Simply Fresh
		Number in stock: 653
		Price per unit:  0.71
		Contact: 
		 15612 Bogart Lane
		 Harrigan, WI 53704
		 Business Phone: 420 333 3912
		 Sales Rep: Cheryl Swedberg
		            420 333 3952
All done.
</code>
</pre>