# 第4章Working with Indices使用索引
使用DPL存储在JE中的所有实体类都必须具有为其标识的主索引或密钥。 所有这些类也可能有一个或多个为它们声明的辅助键。 本章详细介绍主要和次要索引，并说明如何访问为给定实体类创建的索引。

组织访问主要和次要索引的一种方法是创建一个数据访问器类。

## 4.1 Accessing Indexes 访问索引

为了从实体存储中检索任何对象，您必须至少访问该对象的主索引。 存储在实体存储中的不同实体类可以具有不同的主索引，但是所有实体类都必须为其声明主索引。 主索引只是该类使用的默认索引。 （也就是说，它是底层数据库的数据主键。）

实体类可以有选择地为它们声明二级索引。 为了访问这些二级索引，您必须首先访问主索引。

### 4.1.1 Accessing Primary Indices 访问主索引

您使用EntityStore.getPrimaryIndex（）方法检索主索引。 为此，您需要指明索引键类型（即它是否是String，Integer等）以及存储在索引中的实体的类。

例如，以下检索Inventory类的主索引（我们在Inventory.java中提供了此类的实现）。 这些索引键是String类型的。

<pre>
<code>
PrimaryIndex<String,Inventory> inventoryBySku = store.getPrimaryIndex(String.class, Inventory.class);
</code>
</pre>


### 4.1.2 Accessing Secondary Indices 访问二级索引

使用EntityStore.getSecondaryIndex（）方法检索辅助索引。 由于辅助索引实际上是指数据存储中的某个主索引，因此要访问辅助索引，请执行以下操作：

提供由EntityStore.getPrimaryIndex（）返回的主索引。

识别二级索引使用的关键数据类型（字符串，长整数等）。

标识辅助键字段的名称。 在声明SecondaryIndex对象时，可以标识辅助索引必须引用的实体类。

例如，以下内容首先检索主索引，然后使用它检索辅助索引。 辅助键由Inventory类的itemName字段保存。

<pre>
<code>
PrimaryIndex<String,Inventory> inventoryBySku = store.getPrimaryIndex(String.class, Inventory.class); 

SecondaryIndex<String,String,Inventory> inventoryByName = store.getSecondaryIndex(inventoryBySku, String.class, "itemName"); 
</code>
</pre>
## 4.2 创建索引

要使用DPL创建索引，可以使用Java注解声明该类的哪些功能用于主索引，以及哪些功能（如果有）将用作辅助索引。

存储在DPL中的所有实体类都必须为其声明主索引。

实体类可以为其声明零个或多个二级索引。 您可以声明的二级索引的数量没有限制。

### 4.2.1 Declaring Primary Indexes 声明主索引

您通过使用@PrimaryKey注释来声明实体类的主键。 此注释必须紧靠表示类主键的数据成员之前出现。 例如：

<pre>
<code>
import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Vendor implements Serializable {

    private static final long serialVersionUID = 6649604904367797437L;
    
    private String repName;
    private String address;
    private String city;
    private String state;
    private String zipcode;
    private String bizPhoneNumber;
    private String repPhoneNumber;
    
    @PrimaryKey
    private String vendor;

	public String getRepName() {
		return repName;
	}

	public void setRepName(String repName) {
		this.repName = repName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getBizPhoneNumber() {
		return bizPhoneNumber;
	}

	public void setBizPhoneNumber(String bizPhoneNumber) {
		this.bizPhoneNumber = bizPhoneNumber;
	}

	public String getRepPhoneNumber() {
		return repPhoneNumber;
	}

	public void setRepPhoneNumber(String repPhoneNumber) {
		this.repPhoneNumber = repPhoneNumber;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
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

您可以通过指定要用于主键的序列来避免需要为类的主索引显式设置值。 这会导致一个唯一的整数值被用作每个存储对象的主键。

你声明一个序列是通过给@PrimaryKey注释指定sequence关键字来使用的。 您还必须提供序列的名称。 例如:

    @PrimaryKey(sequence="Sequence_Namespace")
    long myPrimaryKey; 

### 4.2.2 Declaring Secondary Indexes 声明二级索引

要声明二级索引，我们使用@SecondaryKey注释。请注意，当我们这样做时，我们必须声明它是一个什么样的索引;也就是说，它与数据存储中的其他数据有什么关系。

我们可以声明的那种指数是：

- ONE_TO_ONE

这种关系表明辅助键对于该对象是唯一的。如果使用数据存储中已存在的辅助键存储对象，则会引发运行时错误。

例如，可以使用社会安全号码（在美国）的主键和人员的雇员号码的辅助键来存储人员对象。预计这两个值在数据存储中都是唯一的。

- MANY_TO_ONE

表示辅助键可用于数据存储中的多个对象。也就是说，密钥不止一次出现，但对于每个存储的对象，只能使用一次。

考虑将经理与员工关联起来的数据存储。一个给定的经理将有多个员工，但每个员工被假定只有一个经理。在这种情况下，经理的员工编号可能是辅助键，这样您可以快速找到与该经理员工相关的所有对象。

- ONE_TO_MANY

表示对于给定的对象，辅助键可能会多次使用。索引键本身被认为是唯一的，但索引的多个实例可以用于每个对象。

例如，员工可能有多个唯一的电子邮件地址。在这种情况下，任何给定的对象都可以被一个或多个电子邮件地址访问。每个这样的地址在数据存储中都是唯一的，但是每个这样的地址将与单个员工对象相关。

- MANY_TO_MANY

对于任何给定的对象可以有多个键，并且对于任何给定的键可以有许多相关的对象。

例如，假设您的组织拥有共享资源，例如打印机。您可能想要跟踪给定员工可以使用哪些打印机（可能有多个打印机）。您可能还想跟踪哪些员工可以使用特定的打印机。这代表了多对多的关系。

请注意，对于ONE_TO_ONE和MANY_TO_ONE关系，您需要一个简单的数据成员（而不是数组或集合）来保存密钥。 对于ONE_TO_MANY和MANY_TO_MANY关系，您需要一个数组或集合来保存键值：

<pre>
<code>
@SecondaryKey(relate=Relationship.ONE_TO_ONE)
private String primaryEmailAddress = new String();

@SecondaryKey(relate=Relationship.ONE_TO_MANY)
private Set<String> emailAddresses = new HashSet<String>(); 
</code>
</pre>

### 4.2.3 Foreign Key Constraints 外键约束

有时候，辅助索引在某种程度上与另一个也包含在数据存储中的实体类有关。也就是说，辅助键可能是另一个实体类的主键。如果是这种情况，您可以声明外键约束，以使数据完整性更容易完成。

例如，您可能有一个用于表示雇员的类。你可能有另一个用来代表公司分部。当您添加或修改员工记录时，您可能希望确保该员工所属的部门对数据存储是已知的。你可以通过指定一个外键约束来做到这一点。

当声明外键约束时：

- 当存储对象的新的辅助键时，将检查它是否存在为相关实体对象的主键。如果没有，则会发生运行时错误。

- 在删除相关实体（即从数据存储中删除公司部门）时，会自动为引用此对象的实体（即员工对象）执行一些操作。这个动作究竟是什么，可以由你来定义。见下文。

从数据存储中删除相关实体时，将采取以下操作之一：

ABORT 中止

删除操作不被允许。操作导致运行时错误。这是默认行为。

CASCADE 级联

与此相关的所有实体也将被删除。例如，如果您删除了一个Division对象，那么属于该部门的所有Employee对象也将被删除。

NULLIFY 废止

与被删除实体有关的所有实体都会被更新，以便相关数据成员无效。也就是说，如果您删除了一个部门，那么与该部门相关的所有员工对象都会将其部门关键字自动设置为空。

您通过使用relatedEntity关键字来声明外键约束。 您使用onRelatedEntityDelete关键字声明外键约束删除策略。 例如，以下内容向Division类对象声明了一个外键约束，并且如果删除了Division类，则会导致相关对象被删除

<pre>
<code>
@SecondaryKey(relate=ONE_TO_ONE, relatedEntity=Division.class,
onRelatedEntityDelete=CASCADE)
private String division = new String(); 
</code>
</pre>