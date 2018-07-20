package me.spacekiller.loginsystem.data;

import java.sql.Connection;
import java.sql.ResultSet;

public interface DataManager {

	public void openConnection();
	
	public void closeConnection();

	public boolean isRegistered(String user);

	public void register(String user, String password, int encryption, String ip);

	public void updatePassword(String user, String password, int encryption);

	public void updateIp(String user, String ip);

	public String getPassword(String user);

	public int getEncryptionTypeId(String user);

	public String getIp(String user);

	public void removeUser(String user);
	
	public ResultSet getAllUsers();

	public Connection getConnection();

	public void session(String uuid, String password);
	
	public String getSessionId(String uuid);
	
	public String getSessionIp(String uuid);

	public void deleteSession(String uuid);

}
