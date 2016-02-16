package com.rj.ftp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.jdom.Document;

import com.rj.bean.FtpclientBean;
import com.rj.bean.FtpserverBean;
import com.rj.util.XmlUtil;

public class FtpClientMain {
	private static Logger log = Logger.getLogger(FtpClientMain.class);
	private List ftpservers; // FTP服务器信息
	private List ftpclients; // FTP客户端信息
	private  ServletContext m_context;
	public static ArrayList<Thread> al_ftpthread= new ArrayList<Thread>() ;
	public List getFtpservers() {
		return ftpservers;
	}

	public List getFtpclients() {
		return ftpclients;
	}

	/**
	 * 初始化，读取配置文件
	 * 
	 */
	private void init() {

		String strFilePath = System.getProperty("user.dir")
				+ "/resource/ftpconfig.xml";
		Document doc = XmlUtil.loadXML(strFilePath);
		// 得到ftp服务器相关信息
		ftpservers = XmlUtil.getEle2List(doc, "ftpserver", FtpserverBean.class);
		// 得到要上传的文件夹相关信息
		ftpclients = XmlUtil.getEle2List(doc, "ftpclient", FtpclientBean.class);

	}
	private void initweb() {

		String strFilePath =  "/WEB-INF/ftpconfig.xml";
		URL url = null;
		try {
			 url = m_context.getResource(strFilePath);
		} catch (MalformedURLException e) {
			log.error(e);
		}
		Document doc = XmlUtil.loadXML(url);
		// 得到ftp服务器相关信息
		ftpservers = XmlUtil.getEle2List(doc, "ftpserver", FtpserverBean.class);
		// 得到要上传的文件夹相关信息
		ftpclients = XmlUtil.getEle2List(doc, "ftpclient", FtpclientBean.class);

	}

	/**
	 * 按每个文件夹启动一个进程，把该文件夹下面的文件发送到所有配置好的FTP服务器上
	 */

	private void upload() {

		Iterator i = ftpclients.iterator();
		while (i.hasNext()) {
			FtpclientBean ftpclientbean = (FtpclientBean) i.next();
			FtpCilentThread ftpCilentThread = new FtpCilentThread(
					ftpclientbean, ftpservers);
			
			
			
			Thread daemon = new Thread(ftpCilentThread,"ftpClientThread"+ftpclientbean.getInterruptime());
			daemon.setPriority( Thread.NORM_PRIORITY );
			daemon.setDaemon(true);
			daemon.start();
			al_ftpthread.add(daemon);

		}

	}

	public static void main(String[] args) {

		FtpClientMain clientMain = new FtpClientMain();
		clientMain.init();
		clientMain.upload();

	}
	
	public static void start(ServletContext servletContext){
		FtpClientMain clientMain = new FtpClientMain();
		clientMain.setServletContext(servletContext);
		clientMain.initweb();
		clientMain.upload();
		
	}
	public  void setServletContext(ServletContext servletContext) {
		m_context = servletContext;

	}
}
