package com.xingyun.dpl.model;

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
