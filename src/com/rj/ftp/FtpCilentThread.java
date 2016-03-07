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
	private List ftpservers; // FTP��������Ϣ
	public volatile static boolean stop = true;

	public FtpCilentThread() {

	}

	
	public FtpCilentThread(FtpclientBean ftpclientbean, List ftpservers) {

		this.ftpclientbean = ftpclientbean;
		this.ftpservers = ftpservers;
	}

	/**
	 * ���Ӹ���FTP������
	 * 
	 * @return HashMap<String, FTPClient> key������·��,FTP������ip
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
	 *            �ļ�ԴĿ¼
	 * @param despath
	 *            �ļ�Ŀ��Ŀ¼
	 * @param isDel
	 *            ������ɺ��Ƿ�ɾ�� true��ɾ����false����ɾ��
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
		// 1.�ȿ����ļ�����ʱĿ¼�ĸ���������ip��Ŀ¼��
		Set<String> ss = hashmap.keySet();
		Iterator<String> ii = ss.iterator();
		while (ii.hasNext()) {
			String key = ii.next();
			String[] keys = key.split(",");
			movefile(ftpclientbean.getFilepath(), keys[0], false);

		}
		// 2.����������Ŀ¼�£�������ɺ�ɾ��������������FTP�������ṩ���أ�������װFTP�������ṩ���ع��ܣ�
		movefile(ftpclientbean.getFilepath(), ftpclientbean.getFilebakpath(),
				true);//����״̬ ��ʱ�趨Ϊfalse����ʽʹ���޸�Ϊtrueɾ���ѿ������ļ�

		// 3.�ϴ��ļ���ftp������

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
		        //�������Ĭ�϶˿ڣ�����ʹ��ftp.connect(url)�ķ�ʽֱ������FTP������  
		        ftp.login("test1", "test1");//��¼  
		        reply = ftp.getReplyCode();  
		        if (!FTPReply.isPositiveCompletion(reply)) {  
		            ftp.disconnect();  
		            System.out.println("success"); 
		        }  
		        ftp.changeWorkingDirectory("1");//ת�Ƶ�FTP������Ŀ¼  
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
			
			// ˯�߷���
			try {
//				upload();
				download();
				Thread.sleep(1000 * 60 * ftpclientbean.getInterruptime());
			}catch (InterruptedException e) {
				log.error("����˯��ʧ�ܣ�",e);
			} 
			catch (Exception e) {
				log.error("�ϴ�ʧ�ܣ�",e);
			}
		}
	}

}
