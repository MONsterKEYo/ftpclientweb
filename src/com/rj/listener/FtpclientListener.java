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
			//得到线程的状态
			String state = ftpthread.getState().name();
			// interrupt()该方法的功能是中断一个线程的执行。但是，这个方法不中断一个正在运行的线程
			// 但是它可以使一个被阻塞的线程抛出一个中断异常，从而使线程提前结束阻塞状态，退出堵塞代码
			ftpthread.interrupt();
		}
		try {
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	
	}

}