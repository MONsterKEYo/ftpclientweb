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
	 * connectServer ����ftp������
	 * 
	 * @throws java.io.IOException
	 * 
	 * @param password
	 *            ����
	 * @param loginname
	 *            ��½�û�
	 * @param ftpServerIP
	 *            ��������ַ
	 * @param workingdirectory
	 *            ftp��������Ŀ¼
	 * 
	 * @param changedir
	 *            ftp��������Ŀ¼�������Ŀ¼
	 * 
	 */

	public static FTPClient connectServer(String ftpServerIP, String loginname,
			String password, int port, String workingdirectory, String changedir) {
		FTPClient ftpClient = new FTPClient();
		;
		// ftpServerIP��FTP��������IP��ַ��loginname:��¼FTP���������û���
		// password����¼FTP���������û����Ŀ��path��FTP�������ϵ�·��
		// port:FTP�������Ķ˿ں�
		log.info("��ʼ����" + ftpServerIP + "ftp�������� ");
		try {

			ftpClient.connect(ftpServerIP, port);
			// FTP���������ӻش�
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				log.error("����" + ftpServerIP + "FTP������ʧ�ܣ�");
				return null;
			}

			log.info("����" + ftpServerIP + "ftp�������ɹ��� ");

			boolean issucess = ftpClient.login(loginname, password);

			if (issucess) {
				log.info("ʹ���û�����" + loginname + "�����룺" + password + " ��¼"
						+ ftpServerIP + "FTP�������ɹ���");
			} else
				log.info("ʹ���û�����" + loginname + "�����룺" + password + " ��¼"
						+ ftpServerIP + "FTP������ʧ�ܣ�");

			// ���ñ���ģʽ
			ftpClient.enterLocalPassiveMode();
			ftpClient.setDataTimeout(300 * 1000);
			ftpClient.setSoTimeout(1000 * 600);

			ftpClient.setBufferSize(1024 * 2);
			ftpClient.setControlEncoding("GBK");

			// �����ļ����ͣ������ƣ�
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			// path��ftp��������Ŀ¼����Ŀ¼,//�����ϴ�Ŀ¼
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
			log.error("����" + ftpServerIP + "ftp�����������쳣�� ", e);
		} catch (IOException e) {
			log.error("����" + ftpServerIP + "ftp�����������쳣�� ", e);
		}
		return ftpClient;

	}

	/**
	 * changeWorkingDirectory ����ftp��������Ŀ¼��ĳ����Ŀ¼Ϊ�ϴ�Ŀ¼
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            ��������ַ
	 * @param workingdirectory
	 *            ftp��������Ŀ¼
	 * 
	 * @param path
	 *            ftp��������Ŀ¼�������Ŀ¼
	 */

	public static boolean changeWorkingDirectory(FTPClient ftpClient,
			String ftpServerIP, String workingdirectory, String path) {
		// path��ftp��������Ŀ¼����Ŀ¼,//�����ϴ�Ŀ¼
		boolean flag = false;
		if (path != null && path.length() != 0)
			try {

				flag = ftpClient.changeWorkingDirectory(gbkToIso(path));
				if (flag)
					log.info(ftpServerIP + "ftp����" + workingdirectory
							+ "·���¸ı�·����" + path + " �ɹ���");
				else
					log.info(ftpServerIP + "ftp����" + workingdirectory
							+ "·���¸ı�·����" + path + " ·��ʧ�ܣ�");
				return flag;

			} catch (IOException e) {
				log.error(ftpServerIP + "ftp����" + workingdirectory + "·���¸ı�·����"
						+ path + " ·�������쳣��", e);
				return false;
			}
		else
			return false;

	}

	/**
	 * upload �ϴ��ļ�
	 * 
	 * @return true �ϴ��ɹ���false�ϴ�ʧ��
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            ��������ַ
	 * @param filename
	 *            �ϴ����ļ�
	 * @param newname
	 *            �ϴ�������ļ���
	 */
	public static boolean upload(FTPClient ftpClient, String ftpServerIP,
			String filename, String newname) {
		FileInputStream fis = null;
		boolean flag = false;

		try {
			fis = new FileInputStream(filename);

			if (ftpClient == null) {
				// ftp����������ʧ��
				flag = false;

			}

			flag = ftpClient.storeFile(gbkToIso(newname), fis);
		} catch (FileNotFoundException e) {
			log.error(filename + " �ϴ�" + ftpServerIP + "FTP�����������쳣��", e);
		} catch (IOException e) {
			log.error(filename + " �ϴ�" + ftpServerIP + "FTP�����������쳣��", e);
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					log.error("�ر��ļ���ʧ�ܣ�",e);
				}
		}

		return flag;
	}

	/**
	 * �ϴ�����ļ�
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param localFile
	 *            --�����ļ���·��
	 * @param filebakpath
	 *            --�����ļ��б���·��
	 * @param ftpServerIP
	 *            --FTP������ip
	 */
	public static void uploadManyFile(FTPClient ftpClient, String ftpServerIP,
			String localFile, String filebakpath) {
		boolean flag = true;
		try {
			File file = new File(localFile); // �ڴ�Ŀ¼�����ļ�
			File fileList[] = file.listFiles();
			if (fileList == null || fileList.length == 0)
				log.info(localFile + "Ŀ¼��û����Ҫ�ϴ����ļ���");
			for (File f : fileList) {
				if (f.isDirectory()) { // �ļ����л����ļ���
					uploadManyFile(ftpClient, ftpServerIP, f.getAbsolutePath(),
							filebakpath);
				} else {
					flag = upload(ftpClient, ftpServerIP, localFile + "/"
							+ f.getName(), f.getName());

					if (flag) {
						log.info(f.getName() + " �ϴ���" + ftpServerIP
								+ "FTP�������ɹ���");
						// ���ͳɹ��󣬄h���ļ�
						deleteFile(localFile, f.getName());
					} else {
						log.info(f.getName() + " �ϴ���" + ftpServerIP
								+ "FTP������ʧ�ܣ�");
					}
				}

			}

		}  catch (Exception e) {
			log.error("�ϴ���" + ftpServerIP + "FTP�����������쳣��", e);
		}
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param path
	 *            Ŀ¼
	 * @param fileName
	 *            �ļ���
	 */
	public static void deleteFile(String path, String fileName) {
		File file = new File(path + "/" + fileName);
		if (file.exists() && file.isFile()) {
			file.delete();
		}
	}

	/**
	 * ���ļ����ķ�ʽ�����ļ�,����ļ����ڻᱻ����
	 * 
	 * @param fileName
	 *            �����Ƶ��ļ���
	 * @param src
	 *            �ļ�ԴĿ¼
	 * @param dest
	 *            �ļ�Ŀ��Ŀ¼
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
			
			log.info( "�����ļ�"+fileName+"(�ļ�����޸�ʱ�䣺"+sdf.format(cal.getTime())+") �� "+src+" �� "+dest+" �ɹ���");
		} catch (FileNotFoundException e) {
			log.error("�����ļ�"+fileName+"�� "+src+" �� "+dest+" ʧ�ܣ�",e);
			throw e;
		} catch (IOException e) {
			log.error("�����ļ�"+fileName+"�� "+src+" �� "+dest+" ʧ�ܣ�",e);
			throw e;
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				log.error("�ر��ļ���ʧ�ܣ�",e);
				throw e;
			}
		}

	}

	/**
	 * ת���ļ�Ŀ¼
	 * 
	 * @param fileName
	 *            �ļ���
	 * @param oldPath
	 *            ��Ŀ¼
	 * @param newPath
	 *            ��Ŀ¼
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
	 * �г�Ftp�������ϵ������ļ���Ŀ¼
	 */
	public static FTPFile[] listRemoteAllFiles(FTPClient ftpClient,
			String workingdirectory) {
		try {
			FTPFile[] files = ftpClient.listFiles(gbkToIso(workingdirectory));
			return files;
		} catch (Exception e) {
			log.error("�г�Ftp���������ļ���Ŀ¼ʧ�ܣ�", e);
		}
		return null;
	}

	/**
	 * �ж�Ftp�������ϵ��ļ���Ŀ¼�Ƿ����
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            ��������ַ
	 * @param workingdirectory
	 *            ftp��������Ŀ¼
	 * 
	 * @param strFileName
	 *            ftp��������Ŀ¼�������Ŀ¼
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
							log.info(ftpServerIP + "FTP������" + workingdirectory
									+ strFileName + " �Ѵ��ڣ�");

						} else {

							log.info(ftpServerIP + "FTP������" + workingdirectory
									+ strFileName + " �����ڣ�");
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
	 * closeServer �Ͽ���ftp������������
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            ��������ַ
	 * @throws java.io.IOException
	 */
	public static void closeServer(FTPClient ftpClient, String ftpServerIP) {
		try {
			if (ftpClient != null) {
				ftpClient.disconnect();
				log.info("�ѶϿ���" + ftpServerIP + "FTP�����������ӣ�");
			}
		} catch (IOException e) {
			log.error("�Ͽ���" + ftpServerIP + "FTP������������ʧ�ܣ�", e);
		}
	}

	/**
	 * gbkToIso ת��GBKΪISO
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
	 * �ڷ������ϴ���һ���ļ���
	 * 
	 * @param FTPClient
	 *            ftpClient
	 * @param ftpServerIP
	 *            ��������ַ
	 * @param workingdirectory
	 *            ftp��������Ŀ¼
	 * 
	 * @param dir
	 *            �ļ������ƣ����ܺ��������ַ����� \ ��/ ��: ��* ��?�� "�� <��>...
	 */
	public static boolean makeDirectory(FTPClient ftpClient,
			String ftpServerIP, String workingdirectory, String dir) {
		boolean flag = false;
		try {
			flag = ftpClient.makeDirectory(gbkToIso(dir));
			if (flag) {
				log.info(ftpServerIP + "FTP��������" + workingdirectory + "�����ļ��У� "
						+ dir + " �ɹ���");

			} else {

				log.info(ftpServerIP + "FTP��������" + workingdirectory + "�����ļ��У� "
						+ dir + " ʧ�ܣ�");
			}
		} catch (Exception e) {
			log.error(ftpServerIP + "FTP��������" + workingdirectory + "�����ļ��У� "
					+ dir + " �����쳣��", e);
		}
		return flag;
	}

	public static void main(String[] args) {
		FTPClient ftpClient = connectServer("192.168.1.100", "user", "user",
				21, "/", "ÿ�ճ��������AQIֵ����ҳ��Ⱦ������");

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
