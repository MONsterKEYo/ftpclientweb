package com.rj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 * <p>
 * Title: ftp upload
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * <p>
 * Company:rongji
 * </p>
 * 
 * @author wjl
 * @version 1.0
 */

public class FtpClientUtil {
	private static Logger log = Logger.getLogger(FtpClientUtil.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");          
	/**
	 * connectServer 连接ftp服务器
	 * 
	 * @throws java.io.IOException
	 * 
	 * @param password
	 *            密码
	 * @param loginname
	 *            登陆用户
	 * @param ftpServerIP
	 *            服务器地址
	 * @param workingdirectory
	 *            ftp服务器主目录
	 * 
	 * @param changedir
	 *            ftp服务器主目录下面的子目录
	 * 
	 */

	public static FTPClient connectServer(String ftpServerIP, String loginname,
			String password, int port, String workingdirectory, String changedir) {
		FTPClient ftpClient = new FTPClient();
		;
		// ftpServerIP：FTP服务器的IP地址；loginname:登录FTP服务器的用户名
		// password：登录FTP服务器的用户名的口令；path：FTP服务器上的路径
		// port:FTP服务器的端口号
		log.info("开始连接" + ftpServerIP + "ftp服务器！ ");
		try {

			ftpClient.connect(ftpServerIP, port);
			// FTP服务器连接回答
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				log.error("连接" + ftpServerIP + "FTP服务器失败！");
				return null;
			}

			log.info("连接" + ftpServerIP + "ftp服务器成功！ ");

			boolean issucess = ftpClient.login(loginname, password);

			if (issucess) {
				log.info("使用用户名：" + loginname + "，密码：" + password + " 登录"
						+ ftpServerIP + "FTP服务器成功！");
			} else
				log.info("使用用户名：" + loginname + "，密码：" + password + " 登录"
						+ ftpServerIP + "FTP服务器失败！");

			// 设置被动模式
			ftpClient.enterLocalPassiveMode();
			ftpClient.setDataTimeout(300 * 1000);
			ftpClient.setSoTimeout(1000 * 600);

			ftpClient.setBufferSize(1024 * 2);
			ftpClient.setControlEncoding("GBK");

			// 设置文件类型（二进制）
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			// path是ftp服务下主目录的子目录,//设置上传目录
			if (workingdirectory != null && workingdirectory.length() != 0) {
				String chworkdir = workingdirectory + changedir;
				if (changedir != null && changedir.length() != 0) {
					boolean b = isFileExists(ftpClient, ftpServerIP,
							workingdirectory, changedir);
					if (!b) {
						makeDirectory(ftpClient, ftpServerIP, workingdirectory,
								chworkdir);
					}

					changeWorkingDirectory(ftpClient, ftpServerIP,
							workingdirectory, chworkdir);
				}
			}

		} catch (SocketException e) {
			log.error("连接" + ftpServerIP + "ftp服务器出现异常！ ", e);
		} catch (IOException e) {
			log.error("连接" + ftpServerIP + "ftp服务器出现异常！ ", e);
		}
		return ftpClient;

	}

	/**
	 * changeWorkingDirectory 设置ftp服务下主目录的某个子目录为上传目录
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            服务器地址
	 * @param workingdirectory
	 *            ftp服务器主目录
	 * 
	 * @param path
	 *            ftp服务器主目录下面的子目录
	 */

	public static boolean changeWorkingDirectory(FTPClient ftpClient,
			String ftpServerIP, String workingdirectory, String path) {
		// path是ftp服务下主目录的子目录,//设置上传目录
		boolean flag = false;
		if (path != null && path.length() != 0)
			try {

				flag = ftpClient.changeWorkingDirectory(gbkToIso(path));
				if (flag)
					log.info(ftpServerIP + "ftp服务" + workingdirectory
							+ "路径下改变路径到" + path + " 成功！");
				else
					log.info(ftpServerIP + "ftp服务" + workingdirectory
							+ "路径下改变路径到" + path + " 路径失败！");
				return flag;

			} catch (IOException e) {
				log.error(ftpServerIP + "ftp服务" + workingdirectory + "路径下改变路径到"
						+ path + " 路径出现异常！", e);
				return false;
			}
		else
			return false;

	}

	/**
	 * upload 上传文件
	 * 
	 * @return true 上传成功，false上传失败
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            服务器地址
	 * @param filename
	 *            上传的文件
	 * @param newname
	 *            上传后的新文件名
	 */
	public static boolean upload(FTPClient ftpClient, String ftpServerIP,
			String filename, String newname) {
		FileInputStream fis = null;
		boolean flag = false;

		try {
			fis = new FileInputStream(filename);

			if (ftpClient == null) {
				// ftp服务器连接失败
				flag = false;

			}

			flag = ftpClient.storeFile(gbkToIso(newname), fis);
		} catch (FileNotFoundException e) {
			log.error(filename + " 上传" + ftpServerIP + "FTP服务器出现异常！", e);
		} catch (IOException e) {
			log.error(filename + " 上传" + ftpServerIP + "FTP服务器出现异常！", e);
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					log.error("关闭文件流失败！",e);
				}
		}

		return flag;
	}

	/**
	 * 上传多个文件
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param localFile
	 *            --本地文件夹路径
	 * @param filebakpath
	 *            --本地文件夹备份路径
	 * @param ftpServerIP
	 *            --FTP服务器ip
	 */
	public static void uploadManyFile(FTPClient ftpClient, String ftpServerIP,
			String localFile, String filebakpath) {
		boolean flag = true;
		try {
			File file = new File(localFile); // 在此目录中找文件
			File fileList[] = file.listFiles();
			if (fileList == null || fileList.length == 0)
				log.info(localFile + "目录下没有需要上传的文件！");
			for (File f : fileList) {
				if (f.isDirectory()) { // 文件夹中还有文件夹
					uploadManyFile(ftpClient, ftpServerIP, f.getAbsolutePath(),
							filebakpath);
				} else {
					flag = upload(ftpClient, ftpServerIP, localFile + "/"
							+ f.getName(), f.getName());

					if (flag) {
						log.info(f.getName() + " 上传到" + ftpServerIP
								+ "FTP服务器成功！");
						// 发送成功后，刪除文件
						deleteFile(localFile, f.getName());
					} else {
						log.info(f.getName() + " 上传到" + ftpServerIP
								+ "FTP服务器失败！");
					}
				}

			}

		}  catch (Exception e) {
			log.error("上传到" + ftpServerIP + "FTP服务器出现异常！", e);
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param path
	 *            目录
	 * @param fileName
	 *            文件名
	 */
	public static void deleteFile(String path, String fileName) {
		File file = new File(path + "/" + fileName);
		if (file.exists() && file.isFile()) {
			file.delete();
		}
	}

	/**
	 * 以文件流的方式复制文件,如果文件存在会被覆盖
	 * 
	 * @param fileName
	 *            被复制的文件名
	 * @param src
	 *            文件源目录
	 * @param dest
	 *            文件目的目录
	 * @throws IOException
	 */
	public static void copyFile(String fileName, String src, String dest) throws Exception {

		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(src + "/" + fileName);
			File file = new File(dest);
			file.toString();
			if (!file.exists()) {
				file.createNewFile();
			}

			out = new FileOutputStream(dest + "/" + fileName);
			int c;
			byte buffer[] = new byte[1024];
			while ((c = in.read(buffer)) != -1) {
				for (int i = 0; i < c; i++) {
					out.write(buffer[i]);
				}
			}
			Calendar cal = Calendar.getInstance(); 
			File filesrc = new File(src);
			cal.setTimeInMillis(filesrc.lastModified());
			
			log.info( "拷贝文件"+fileName+"(文件最后修改时间："+sdf.format(cal.getTime())+") 从 "+src+" 到 "+dest+" 成功！");
		} catch (FileNotFoundException e) {
			log.error("拷贝文件"+fileName+"从 "+src+" 到 "+dest+" 失败！",e);
			throw e;
		} catch (IOException e) {
			log.error("拷贝文件"+fileName+"从 "+src+" 到 "+dest+" 失败！",e);
			throw e;
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				log.error("关闭文件流失败！",e);
				throw e;
			}
		}

	}

	/**
	 * 转移文件目录
	 * 
	 * @param fileName
	 *            文件名
	 * @param oldPath
	 *            旧目录
	 * @param newPath
	 *            新目录
	 */
	public static boolean changeDirectory(String fileName, String oldPath,
			String newPath) {
		boolean flag = false;
		File path = new File(newPath);
		if (!path.exists())
			path.mkdirs();

		if (!oldPath.equals(newPath)) {
			File oldFile = new File(oldPath + "/" + fileName);
			File newFile = new File(newPath + "/" + fileName);
			if (newFile.exists())
				oldFile.delete();

			flag = oldFile.renameTo(newFile);
		}

		return flag;
	}

	/**
	 * 列出Ftp服务器上的所有文件和目录
	 */
	public static FTPFile[] listRemoteAllFiles(FTPClient ftpClient,
			String workingdirectory) {
		try {
			FTPFile[] files = ftpClient.listFiles(gbkToIso(workingdirectory));
			return files;
		} catch (Exception e) {
			log.error("列出Ftp服务器上文件和目录失败！", e);
		}
		return null;
	}

	/**
	 * 判断Ftp服务器上的文件和目录是否存在
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            服务器地址
	 * @param workingdirectory
	 *            ftp服务器主目录
	 * 
	 * @param strFileName
	 *            ftp服务器主目录下面的子目录
	 * 
	 */
	public static boolean isFileExists(FTPClient ftpClient, String ftpServerIP,
			String workingdirectory, String strFileName) {
		boolean flag = false;
		try {
			FTPFile[] files = listRemoteAllFiles(ftpClient, workingdirectory);
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					if (strFileName.equals(files[i].getName())) {
						flag = true;
						if (flag) {
							log.info(ftpServerIP + "FTP服务器" + workingdirectory
									+ strFileName + " 已存在！");

						} else {

							log.info(ftpServerIP + "FTP服务器" + workingdirectory
									+ strFileName + " 不存在！");
						}
						break;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * closeServer 断开与ftp服务器的链接
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            服务器地址
	 * @throws java.io.IOException
	 */
	public static void closeServer(FTPClient ftpClient, String ftpServerIP) {
		try {
			if (ftpClient != null) {
				ftpClient.disconnect();
				log.info("已断开与" + ftpServerIP + "FTP服务器的连接！");
			}
		} catch (IOException e) {
			log.error("断开与" + ftpServerIP + "FTP服务器的连接失败！", e);
		}
	}

	/**
	 * gbkToIso 转换GBK为ISO
	 * 
	 * 
	 */
	public static String gbkToIso(String para) {
		try {
			return new String(para.getBytes("GBK"), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 在服务器上创建一个文件夹
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            服务器地址
	 * @param workingdirectory
	 *            ftp服务器主目录
	 * 
	 * @param dir
	 *            文件夹名称，不能含有特殊字符，如 \ 、/ 、: 、* 、?、 "、 <、>...
	 */
	public static boolean makeDirectory(FTPClient ftpClient,
			String ftpServerIP, String workingdirectory, String dir) {
		boolean flag = false;
		try {
			flag = ftpClient.makeDirectory(gbkToIso(dir));
			if (flag) {
				log.info(ftpServerIP + "FTP服务器上" + workingdirectory + "创建文件夹： "
						+ dir + " 成功！");

			} else {

				log.info(ftpServerIP + "FTP服务器上" + workingdirectory + "创建文件夹： "
						+ dir + " 失败！");
			}
		} catch (Exception e) {
			log.error(ftpServerIP + "FTP服务器上" + workingdirectory + "创建文件夹： "
					+ dir + " 出现异常！", e);
		}
		return flag;
	}

	public static void main(String[] args) {
		FTPClient ftpClient = connectServer("192.168.1.100", "user", "user",
				21, "/", "每日城市最大日AQI值和首页污染物名称");

		try {
			FTPFile[] file = ftpClient.listFiles("/");
			for (int i = 0; i < file.length; i++) {
				System.out.println("name:" + file[i].getName());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
