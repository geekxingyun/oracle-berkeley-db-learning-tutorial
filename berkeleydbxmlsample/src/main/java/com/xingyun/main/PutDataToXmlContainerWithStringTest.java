package com.xingyun.main;

import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlDocumentConfig;
import com.sleepycat.dbxml.XmlException;
import com.xingyun.util.BDBXmlDBUtils;

import java.io.File;

/***
 * 将XML字符串格式的内容保存到BDB XML DB 中
 * ****/
public class PutDataToXmlContainerWithStringTest {
    //数据库文件夹
    static File envHome = new File("C:\\export1\\testEnv");
    //容器名称
    private static String theContainer = "container.dbxml";
    //容器实例对象
    private static XmlContainer xmlContainer=null;
    //文档配置
    private static XmlDocumentConfig xmlDocumentConfig=null;

    // The document's name.
    private static String docName = "testDoc1";
    // The document's Content is a string
    private static String docString = "<a_node><b_node>Some text</b_node></a_node>";

    public static void main(String[] args){

        try {
            xmlContainer= BDBXmlDBUtils.initBDBXMLDB(envHome,theContainer);
            xmlDocumentConfig=new XmlDocumentConfig();
            xmlContainer.putDocument(docName,docString,xmlDocumentConfig);
        } catch (XmlException e) {
            e.printStackTrace();
        }finally {
            BDBXmlDBUtils.closeBDBXMLDB();
        }
    }
}
