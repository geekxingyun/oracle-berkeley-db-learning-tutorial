package com.xingyun.main;

import com.sleepycat.dbxml.*;
import com.xingyun.util.BDBXmlDBUtils;

import java.io.File;

/**
 * 将现有的XML文件保存到BDB XML DB中
 * **/
public class PutDataToXmlContainerWithFileTest {
    //数据库文件夹
    static File envHome = new File("C:\\export1\\testEnv");
    //数据库文件
    private static String theContainer = "container.dbxml";
    //容器对象
    private static XmlContainer xmlContainer=null;
    //容器管理器
    private static XmlManager xmlManager=null;
    //文档配置
    private static XmlDocumentConfig xmlDocumentConfig=null;

    //XML读取流
    private static XmlInputStream xmlInputStream;
    // The document's name.
    private static String docName = "testDoc1";
    // The document's content is a file
    private static String fileName= "classpath:/resource/send.xml";

    public static void main(String[] args){

        try {
            xmlContainer= BDBXmlDBUtils.initBDBXMLDB(envHome,theContainer);
            xmlManager=BDBXmlDBUtils.getXmlManager();
            xmlInputStream = xmlManager.createLocalFileInputStream(fileName);
           xmlDocumentConfig=new XmlDocumentConfig();
            xmlContainer.putDocument(docName,xmlInputStream,xmlDocumentConfig);
        } catch (XmlException e) {
            e.printStackTrace();
        }finally {
            BDBXmlDBUtils.closeBDBXMLDB();
        }
    }
}
