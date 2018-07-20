package me.spacekiller.loginsystem.encryption;

import me.spacekiller.loginsystem.LoginSystem;
import me.spacekiller.loginsystem.data.DataManager;


public class PasswordManager {
	public static boolean checkPass(String uuid, String password) {
		LoginSystem plugin = LoginSystem.instance;
		DataManager data = plugin.data;
		String realPass = data.getPassword(uuid);
		int type = data.getEncryptionTypeId(uuid);
		EncryptionType etype = EncryptionType.fromInt(type);
		return etype.checkPass(password, realPass);
	}
}
