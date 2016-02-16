package com.rj.listener;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.rj.ftp.FtpCilentThread;
import com.rj.ftp.FtpClientMain;

public class FtpclientListener implements ServletContextListener {
	private final String WEB_APP_ROOT_DEFAULT = "webapp.root";

	public void contextInitialized(ServletContextEvent event) {
		
		
		ServletContext scontext = event.getServletContext();
		
		String prefix = scontext.getRealPath("/");
		System.setProperty(WEB_APP_ROOT_DEFAULT, prefix);

		FtpClientMain.start(scontext);

	}

	public void contextDestroyed(ServletContextEvent event) {
		
		FtpCilentThread.stop = false;
		ArrayList<Thread> al = FtpClientMain.al_ftpthread;
		Iterator<Thread> i = al.iterator();
		while (i.hasNext()) {
			Thread ftpthread = i.next();
			//�õ��̵߳�״̬
			String state = ftpthread.getState().name();
			// interrupt()�÷����Ĺ������ж�һ���̵߳�ִ�С����ǣ�����������ж�һ���������е��߳�
			// ����������ʹһ�����������߳��׳�һ���ж��쳣���Ӷ�ʹ�߳���ǰ��������״̬���˳���������
			ftpthread.interrupt();
		}
		try {
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	
	}

}