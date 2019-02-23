package com.xingyun.util;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.dbxml.*;
import com.xingyun.exception.MyErrorHandler;

import java.io.File;
import java.io.FileNotFoundException;

public final class BDBXmlDBUtils {

    private static Environment myEnv = null;//DB 环境
    private static EnvironmentConfig envConf=new EnvironmentConfig();//DB环境配置
    private static MyErrorHandler myErrorHandler=new MyErrorHandler();//DB 自定义错误异常

    private static XmlManager xmlManager = null;//DB管理器用于增删改查容器
    private static XmlManagerConfig xmlManagerConfig=new XmlManagerConfig();//DB管理器配置

    private static XmlContainer xmlContainer = null;//容器对象
    private static XmlContainerConfig xmlContainerConfig=new XmlContainerConfig();//容器配置

    public static XmlManager getXmlManager(){
        return xmlManager;
    }

    /**
     * 初始化BDB XML DB
     * **/
    public static XmlContainer initBDBXMLDB(File envHome,String theContainerName){
        try {
            //BDB XML DB 数据库环境配置
            //如果为true，那么在打开时如果环境不存在，则创建它。
            envConf.setAllowCreate(true);
            //如果为true，则初始化共享内存池子系统。该子系统是多线程BDB XML应用程序所必需的，它提供了一个内存缓存，可以由参与此环境的所有线程和进程共享。
            envConf.setInitializeCache(true);
            //如果为true，则初始化锁定子系统,如果希望容器可以被多个线程和/或多个进程访问，那么您应该启用此子系统。
            envConf.setInitializeLocking(true);
            //如果为true，则初始化日志记录子系统 此子系统用于从应用程序或系统故障中恢复数据库。
            envConf.setInitializeLogging(true);
            //如果为true，则导致对基础数据库运行正常恢复,只有在打开日志记录子系统时才能运行恢复。
            // 此外，恢复只能由单个控制线程运行; 通常，在执行任何其他数据库操作之前，它由应用程序的主线程运行。
            envConf.setRunRecovery(false);
            //启用事务系统
            envConf.setTransactional(true);
            //启用自定义异常处理
            envConf.setErrorHandler(myErrorHandler);

            //如果数据库环境路径不存在则创建它
            if(!envHome.exists()) {
                envHome.mkdirs();
            }
            //给DB环境设置数据库路径和配置
            myEnv = new Environment(envHome,envConf);

            //XML管理器配置
            //禁止XML管理器自动打开容器 如果设置为true，则XQuery查询引用已创建但未打开的容器将自动导致在查询期间打开容器。
            xmlManagerConfig.setAllowAutoOpen(false);
            //如果设置为true，XmlManager将关闭并销毁在XmlManager关闭时实例化的Environment对象。
            xmlManagerConfig.setAdoptEnvironment(true);
            //如果设置为true，则从BDB XML内部执行的XQuery查询可以访问外部源（URL，文件等）。
            xmlManagerConfig.setAllowExternalAccess(true);

            //初始化XML管理器
            xmlManager = new XmlManager(myEnv,xmlManagerConfig);
            //配置XML管理器日志级别  允许您指示要查看的日志记录级别 debug, info, warning, error或者all
            XmlManager.setLogLevel(XmlManager.LEVEL_ALL,true);
            //配置XML管理器日志范围 允许您指示要为其发出日志消息的DB XML子系统的部分 indexer, query processor, optimizer, dictionary, container, or all
            XmlManager.setLogCategory(XmlManager.CATEGORY_ALL, true);
            //设置默认的容器类型   XmlContainer.NodeContainer
            xmlManager.setDefaultContainerType(XmlContainer.WholedocContainer);

            //如果指定的文件不是BDB XML容器，则返回0。 否则，它返回基础数据库格式编号。
            if(xmlManager.existsContainer(theContainerName)==0){
                //创建并打开一个BDB XML 容器
                xmlContainer=xmlManager.createContainer(theContainerName);
            }else{
                //导致创建容器和所有基础数据库
                xmlContainerConfig.setAllowCreate(false);
                //如果容器已存在，则导致容器创建失败
                xmlContainerConfig.setExclusiveCreate(true);
                //打开容器仅供读取访问
                xmlContainerConfig.setReadOnly(false);
                //导致文档在加载到容器中时进行验证。 默认行为是不验证文档。
                xmlContainerConfig.setAllowValidation(true);
                //默认索引类型
                //旧版本:默认NodeContainer:true  WholedocContainer:false
                //新版本 0:默认 1 节点存储 2 整个文档存储
                //如果我们的文档大小超过兆字节，则应避免使用WholedocContainer
                //NodeContainer的查询速度通常比WholedocContainer快。
                xmlContainerConfig.setIndexNodes(2);
                //容器支持事务
                xmlContainerConfig.setTransactional(true);
                //打开一个BDB XML 容器
                xmlContainer = xmlManager.openContainer(theContainerName);

//                String currentName = "/export/xml/myContainer.bdbxml";
//                String newName = "/export2/xml/myContainer.bdbxml";
//                //重命名容器
//                xmlManager.renameContainer(currentName, newName);
                //删除容器
//                  xmlManager.removeContainer(newName);
            }
        } catch (XmlException e) {
            // Error handling goes here
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return xmlContainer;
    }

    /**
     * 重命名容器
     * **/
    public static Boolean reNameContainer(String currentContainerName,String newContainerName){
        try {
            xmlManager.renameContainer(currentContainerName,newContainerName);
            return true;
        } catch (XmlException e) {
            return false;
        }
    }

    /**
     * 删除容器
     * */
    public static Boolean removedContainer(String currentContainerName){
        try {
            xmlManager.removeContainer(currentContainerName);
            return true;
        } catch (XmlException e) {
            return false;
        }
    }

    /**
     * 关闭BDB XML DB
     * **/
    public static Boolean closeBDBXMLDB(){
        //如果容器不为空
        if(xmlContainer!=null) {
            try {
                //尝试关闭容器
                xmlContainer.close();
                xmlManager.close();
            } catch (XmlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
