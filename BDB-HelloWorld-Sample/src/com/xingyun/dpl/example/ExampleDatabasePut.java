package com.xingyun.dpl.example;

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
