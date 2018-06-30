package com.xingyun.dpl.example.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * 库存类
 */
@Entity
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
