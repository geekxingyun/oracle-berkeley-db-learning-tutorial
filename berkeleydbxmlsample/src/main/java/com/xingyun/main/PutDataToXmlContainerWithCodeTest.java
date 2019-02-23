package com.xingyun.main;

import com.sleepycat.dbxml.*;
import com.xingyun.util.BDBXmlDBUtils;

import java.io.File;

/**
 * 将现有的
 * <a>
 * <b a1="one" b2="two">b node text</b>
 * <c>c node text</c>
 * </a>
 * 写入到BDB XML DB 中
 * **/
public class PutDataToXmlContainerWithCodeTest {
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
    //写入流
    private static XmlEventWriter writer=null;

    // The document's name.
    private static String docName = "testDoc1";
    // The document's content is a file

    public static void main(String[] args){

        try {
            //初始化容器
            xmlContainer= BDBXmlDBUtils.initBDBXMLDB(envHome,theContainer);
            xmlManager=BDBXmlDBUtils.getXmlManager();
            // create a new document
            xmlDocument = xmlManager.createDocument();
            //set document name
            xmlDocument.setName(docName);

            // 将文档放入容器中。请注意，此时您实际上没有将任何文档数据写入容器,因为您的文档当前是空的。
           writer = xmlContainer.putDocumentAsEventWriter(xmlDocument);
            writer.writeStartDocument(null, null, null); // no XML decl

            // Write the document's root node. It has no prefixes or
            // attributes. This node is not empty.
            writer.writeStartElement("a", null, null, 0, false);

            // Write a new start element. This time for the "b" node.
            // It has two attributes and its content is also not empty.
            writer.writeStartElement("b", null, null, 2, false);
            // Write the "a1" and "b2" attributes on the "b" node
            writer.writeAttribute("a1", null, null, "one", true);
            writer.writeAttribute("b2", null, null, "two", true);
            // Write the "b" node's content. Note that there are 11
            // characters in this text, and we provide that information
            // to the method.
            // writer.writeText(XmlManager.Characters, "b node text", 11);
            writer.writeText(65001, "b node text", 11);
            // End the "b" node
            writer.writeEndElement("b", null, null);
            // Start the "c" node. There are no attributes on this node.
            writer.writeStartElement("c", null, null, 0, false);
            // Write the "c" node's content
//            writer.writeText(XmlManager.Characters, "c node text", 11);
            writer.writeText(65001, "c node text", 11);
            // End the "c" node and then the "a" (the root) node
            writer.writeEndElement("c", null, null);
            writer.writeEndElement("a", null, null);

            // End the document
            writer.writeEndDocument();

        } catch (XmlException e) {
            e.printStackTrace();
        }finally {
            if(writer!=null){
                try {
                    // Close the document
                    writer.close();
                } catch (XmlException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
