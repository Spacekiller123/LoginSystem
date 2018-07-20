package me.spacekiller.loginsystem.xAuth;

import me.spacekiller.loginsystem.encryption.Encryptor;
import me.spacekiller.loginsystem.util.EncryptionUtil;

public class SaltedWhirlpool implements Encryptor {
	@Override
	public boolean check(String check, String real) {
		check = EncryptionUtil.getSaltedWhirlpool(real, check);
		return check.equals(real);
	}

	@Override
	public String hash(String value) {
		return null;
	}
}
