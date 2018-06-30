package com.xingyun.dpl.model;

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
