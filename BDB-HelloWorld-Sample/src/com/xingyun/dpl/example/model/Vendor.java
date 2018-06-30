package com.xingyun.dpl.example.model;

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
    private String vendorName;//供应商名称

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

	

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
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
