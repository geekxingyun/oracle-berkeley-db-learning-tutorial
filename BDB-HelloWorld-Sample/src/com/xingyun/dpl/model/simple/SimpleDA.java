package com.xingyun.dpl.model.simple;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

/**
 * 我们使用专门的数据访问器类来组织我们的主要和次要索引。 
 * 这个类存在的主要原因是为我们的实体类提供对所有使用的索引的方便访问
 * */
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
