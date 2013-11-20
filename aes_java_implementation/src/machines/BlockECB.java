package machines;

public class BlockECB extends CipherBlock {

	public BlockECB(byte[][] key, int klen) {
		super(key, klen);
	}

	/**
	 * Electronic Code Bock - one block once
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	@Override
	public byte[] encryption(byte[] data, int len) {
		if (len == 0) {
			return new byte[0];
		}
		return doIt(data, len, true);
	}

	@Override
	public byte[] decryption(byte[] data, int len) {
		if (len == 0) {
			return new byte[0];
		}
		return doIt(data, len, false);
	}

	private byte[] doIt(byte[] data, int len, boolean encrypt) {
		for (int pos = 0; pos < data.length; pos += 16) {
			// cipher block
			byte[] result;
			if (encrypt) {
				result = AESAlgorithm.encryptBloc(data, pos, key, keyLen);
			} else {
				result = AESAlgorithm.decryptBloc(data, pos, key, keyLen);
			}
			System.arraycopy(result, 0, data, pos, 16);
		}
		return data;
	}

	@Override
	public byte[] exctractPrefix(byte[] plaintext) {
		return plaintext;
	}

	@Override
	public byte[] prefixAppend(byte[] out) {
		return out;
	}
}
