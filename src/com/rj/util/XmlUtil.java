package com.rj.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlUtil {
	private static Logger log = Logger.getLogger(XmlUtil.class);
	
	public	static	Document	loadXML( String strUrl )
	{
			try {
				SAXBuilder	builder	=	new SAXBuilder();
				
				return builder.build(new File( strUrl ));
			} catch (MalformedURLException e) {
				log.error(strUrl+"读取xml文件失败！",e);
			} catch (JDOMException e) {
				log.error(strUrl+"读取xml文件失败！",e);
			} catch (IOException e) {
				log.error(strUrl+"读取xml文件失败！",e);
			}
		
		return	null;
	}
	
	public	static	Document	loadXML( URL strUrl )
	{
			try {
				SAXBuilder	builder	=	new SAXBuilder();
				
				return builder.build(strUrl);
			} catch (MalformedURLException e) {
				log.error(strUrl+"读取xml文件失败！",e);
			} catch (JDOMException e) {
				log.error(strUrl+"读取xml文件失败！",e);
			} catch (IOException e) {
				log.error(strUrl+"读取xml文件失败！",e);
			}
		
		return	null;
	}
	
	public static List getEle2List(Document doc,String elename, Class clsObj ) {
		ArrayList al = new ArrayList();
		HashMap properties = new HashMap();
		Object obj = null;
		Element ele = doc.getRootElement();
		
		List list = ele.getChildren(elename);
		for(int i=0;i<list.size();i++){
			Element e = (Element)list.get(i);
			List atts = e.getAttributes();
			for (int j = 0; j < atts.size(); j++) {
				Attribute	attr	=	(Attribute)atts.get(j);
				properties.put( attr.getName(), attr.getValue() );
			}
			
			try {
				obj = clsObj.newInstance();
				BeanUtils.populate( obj, properties );
				al.add(obj);
			} catch (InstantiationException e1) {
				log.error("把xml转换成对象失败！",e1);
			} catch (IllegalAccessException e1) {
				log.error("把xml转换成对象失败！",e1);
			} catch (InvocationTargetException e1) {
				log.error("把xml转换成对象失败！",e1);
			}
			
		}
		return al;
	}
	
	/*public static void main(String[] args) {
		 String strFilePath = System.getProperty("user.dir") + "/resource/ftpconfig.xml";
		 Document doc = XmlUtil.loadXML(strFilePath);
		 XmlUtil.getEle2List(doc, "ftpserver", FtpserverBean.class);
	}*/

}
