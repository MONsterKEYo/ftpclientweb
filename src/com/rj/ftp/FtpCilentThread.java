package com.rj.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.rj.bean.FtpclientBean;
import com.rj.bean.FtpserverBean;
import com.rj.util.FtpClientUtil;

public class FtpCilentThread implements Runnable  {
	private static Logger log = Logger.getLogger(FtpCilentThread.class);
	private FtpclientBean ftpclientbean;
	private List ftpservers; // FTP服务器信息
	public volatile static boolean stop = true;

	public FtpCilentThread() {

	}

	
	public FtpCilentThread(FtpclientBean ftpclientbean, List ftpservers) {

		this.ftpclientbean = ftpclientbean;
		this.ftpservers = ftpservers;
	}

	/**
	 * 连接各个FTP服务器
	 * 
	 * @return HashMap<String, FTPClient> key：备份路径,FTP服务器ip
	 */
	private HashMap<String, FTPClient> getFTPClients() {
		HashMap<String, FTPClient> hashmap = new HashMap<String, FTPClient>();
		Iterator i = ftpservers.iterator();
		while (i.hasNext()) {
			FtpserverBean ftpserver = (FtpserverBean) i.next();
			FTPClient ftpclient = FtpClientUtil.connectServer(
					ftpserver.getIp(), ftpserver.getUsername(), ftpserver
							.getPassword(), ftpserver.getPort(), ftpserver
							.getWorkingdirectory(), ftpclientbean
							.getFtpserverpath());

			String filetemp = ftpclientbean.getFiletemp();
			int w = filetemp.lastIndexOf("/");
			StringBuffer sb = new StringBuffer();
			sb.append(filetemp.substring(0, w + 1));
			sb.append(ftpserver.getIp());
			sb.append(filetemp.substring(w, filetemp.length()));
			sb.append(",").append(ftpserver.getIp());
			hashmap.put(sb.toString(), ftpclient);
		}
		return hashmap;
	}

	/**
	 * @param srcpath
	 *            文件源目录
	 * @param despath
	 *            文件目的目录
	 * @param isDel
	 *            拷贝完成后是否删除 true：删除，false：不删除
	 */

	public void movefile(String srcpath, String despath, boolean isDel) throws Exception{

		File file = new File(srcpath);
		File fileList[] = file.listFiles();
		File filed = new File(despath);
		if (!filed.exists())
			filed.mkdirs();

		for (File f : fileList) {
			if (f.isDirectory()) {
				continue;
			}


				FtpClientUtil.copyFile(f.getName(), srcpath, despath);
				if (isDel)
					FtpClientUtil.deleteFile(srcpath, f.getName());

		}
	}

	public void upload() throws Exception{
		HashMap<String, FTPClient> hashmap = getFTPClients();
		// 1.先拷贝文件到临时目录的各个服务器ip的目录下
		Set<String> ss = hashmap.keySet();
		Iterator<String> ii = ss.iterator();
		while (ii.hasNext()) {
			String key = ii.next();
			String[] keys = key.split(",");
			movefile(ftpclientbean.getFilepath(), keys[0], false);

		}
		// 2.拷贝到备份目录下（拷贝完成后删除），供本机的FTP服务器提供下载（本机安装FTP服务器提供下载功能）
		movefile(ftpclientbean.getFilepath(), ftpclientbean.getFilebakpath(),
				true);//测试状态 暂时设定为false，正式使用修改为true删除已拷贝的文件

		// 3.上传文件到ftp服务器

		Set<String> s = hashmap.keySet();
		Iterator<String> i = s.iterator();
		while (i.hasNext()) {
			String key = i.next();
			String[] keys = key.split(",");
			FTPClient ftpClient = hashmap.get(key);

			FtpClientUtil.uploadManyFile(ftpClient, keys[1], keys[0],
					ftpclientbean.getFilebakpath());
			FtpClientUtil.closeServer(ftpClient,keys[1]);
		}
	}
	
	public void download() throws Exception{
		HashMap<String, FTPClient> hashmap = getFTPClients();
		 boolean success = false;  
		    FTPClient ftp = new FTPClient();  
		    try {  
		        int reply;  
		        ftp.connect("127.0.0.1", 21);  
		        //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器  
		        ftp.login("test1", "test1");//登录  
		        reply = ftp.getReplyCode();  
		        if (!FTPReply.isPositiveCompletion(reply)) {  
		            ftp.disconnect();  
		            System.out.println("success"); 
		        }  
		        ftp.changeWorkingDirectory("1");//转移到FTP服务器目录  
		        FTPFile[] fs = ftp.listFiles();  
		        for(FTPFile ff:fs){  
		        	if(ff.getName().indexOf("txt")>-1){
		                File localFile = new File("E:/ftpget"+"/"+ff.getName());  
		                  
		                OutputStream is = new FileOutputStream(localFile);   
		                ftp.retrieveFile(ff.getName(), is);  
		                is.close();  
		                
		             // Delete file
		                boolean exist = ftp.deleteFile(ff.getName());

		                // Notify user for deletion 
		                if (exist) {
		                    System.out.println("[INFO]File '"+ ff.getName() + "' deleted...");
		                }
		                // Notify user that file doesn't exist
		                else 
		                    System.out.println("[INFO]File '"+ ff.getName() + "' doesn't exist...");
		        	}  
		        }  
		          
		        ftp.logout();  
		        success = true;  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    } finally {  
		        if (ftp.isConnected()) {  
		            try {  
		                ftp.disconnect();  
		            } catch (IOException ioe) {  
		            }  
		        }  
		    }  
		    System.out.println("success");
	}

	public void run() {

		while (stop) {
			
			// 睡眠分钟
			try {
//				upload();
				download();
				Thread.sleep(1000 * 60 * ftpclientbean.getInterruptime());
			}catch (InterruptedException e) {
				log.error("设置睡眠失败！",e);
			} 
			catch (Exception e) {
				log.error("上传失败！",e);
			}
		}
	}

}
