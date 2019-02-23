package com.xingyun.main;

import com.sleepycat.dbxml.*;
import com.xingyun.util.BDBXmlDBUtils;

import java.io.File;

/**
 * 写入到BDB XML DB 中
 * **/
public class PutDataToXmlContainerWithCodeDetailTest {
    //数据库文件夹
    static File envHome = new File("C:\\export1\\testEnv");
    //数据库文件
    private static String theContainer = "container.dbxml";
    //容器对象
    private static XmlContainer xmlContainer=null;
    //容器管理器
    private static XmlManager xmlManager=null;
    //文档对象
    private static XmlDocument xmlDocument=null;


    // The document's name.
    private static String docName = "testDoc1";
    // The document file
    private static String fileName = "c:/export/testdoc1.xml";
    private static String URI = "http://dbxmlExamples/metadata";
    private static String attrName = "createdOn";
    private static  XmlValue attrValue=null;

    public static void main(String[] args){
        try {
            //初始化容器
            xmlContainer = BDBXmlDBUtils.initBDBXMLDB(envHome, theContainer);
            xmlManager = BDBXmlDBUtils.getXmlManager();
            // Get the input stream.
            XmlInputStream theStream = xmlManager.createLocalFileInputStream(fileName);

            // create a new document
            xmlDocument = xmlManager.createDocument();
            //set document name
            xmlDocument.setName(docName);
            // Set the content
            xmlDocument.setContentAsXmlInputStream(theStream);
            attrValue = new XmlValue(XmlValue.DATE_TIME, "2005-10-5T04:18:36");
            xmlDocument.setMetaData(URI, attrName, attrValue);
            xmlContainer.putDocument(xmlDocument,        // The actual document.
                    new XmlDocumentConfig());       // XmlDocumentConfig object

        } catch (XmlException e) {
            e.printStackTrace();
        }finally {
            BDBXmlDBUtils.closeBDBXMLDB();
        }

    }
}
