package me.spacekiller.loginsystem.encryption;

import me.spacekiller.loginsystem.util.EncryptionUtil;


public class MD5 implements Encryptor {

	@Override
	public boolean check(String check, String real) {
		check = EncryptionUtil.getMD5(check);
		return check.equals(real);
	}

	@Override
	public String hash(String value) {
		return EncryptionUtil.getMD5(value);
	}
}
