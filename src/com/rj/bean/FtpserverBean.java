package com.rj.bean;

public class FtpserverBean {
	private String name;
	private String ip;
	private int port;
	private String  username;
	private String password;
	private String workingdirectory;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getWorkingdirectory() {
		return workingdirectory;
	}
	public void setWorkingdirectory(String workingdirectory) {
		this.workingdirectory = workingdirectory;
	}
}
