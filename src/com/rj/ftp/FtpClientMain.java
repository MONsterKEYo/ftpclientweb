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
	private List ftpservers; // FTP��������Ϣ
	private List ftpclients; // FTP�ͻ�����Ϣ
	private  ServletContext m_context;
	public static ArrayList<Thread> al_ftpthread= new ArrayList<Thread>() ;
	public List getFtpservers() {
		return ftpservers;
	}

	public List getFtpclients() {
		return ftpclients;
	}

	/**
	 * ��ʼ������ȡ�����ļ�
	 * 
	 */
	private void init() {

		String strFilePath = System.getProperty("user.dir")
				+ "/resource/ftpconfig.xml";
		Document doc = XmlUtil.loadXML(strFilePath);
		// �õ�ftp�����������Ϣ
		ftpservers = XmlUtil.getEle2List(doc, "ftpserver", FtpserverBean.class);
		// �õ�Ҫ�ϴ����ļ��������Ϣ
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
		// �õ�ftp�����������Ϣ
		ftpservers = XmlUtil.getEle2List(doc, "ftpserver", FtpserverBean.class);
		// �õ�Ҫ�ϴ����ļ��������Ϣ
		ftpclients = XmlUtil.getEle2List(doc, "ftpclient", FtpclientBean.class);

	}

	/**
	 * ��ÿ���ļ�������һ�����̣��Ѹ��ļ���������ļ����͵��������úõ�FTP��������
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
