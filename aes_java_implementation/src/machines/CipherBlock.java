package machines;

public abstract class CipherBlock {

	public byte[][] key;
	public int keyLen;
	public byte[] iv;
	public byte[] lastIV;

	public CipherBlock(byte[][] k, int kLen) {
		key = k;
		keyLen = kLen;
	}

	public abstract byte[] encryption(byte[] data, int len);

	public abstract byte[] decryption(byte[] data, int len);

	static byte[] xor_func(byte[] a, byte[] b) {
		byte[] out = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			out[i] = (byte) (a[i] ^ b[i]);
		}
		return out;
	}

	static byte[][] convertToBlockMatrix(byte[] data, int len) {
		int nBlocks = (len + 16 - 1) / 16;

		byte blockMatrix[][] = new byte[nBlocks][16];

		for (int i = 0; i < len; i++) {
			int linha = i / 16;
			int coluna = i % 16;
			blockMatrix[linha][coluna] = data[i];
		}
		return blockMatrix;
	}

	static byte[] convertFromBlockMatrix(byte[][] matrix, int len) {
		byte[] data = new byte[len];

		for (int i = 0; i < len; i++) {
			int linha = i / 16;
			int coluna = i % 16;
			data[i] = matrix[linha][coluna];
		}
		return data;
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];

		}
		return new String(hexChars);
	}

	public byte[] exctractPrefix(byte[] plaintext) throws Exception {
		if (plaintext.length < 16) {
			throw new Exception("Data is not enought to extract iv");
		}
		byte out[] = new byte[plaintext.length - 16];
		// set Iv
		System.arraycopy(plaintext, 0, iv, 0, 16);
		lastIV = iv;
		// Exctract IV
		System.arraycopy(plaintext, 16, out, 0, plaintext.length - 16);
		return out;
	}

	public byte[] prefixAppend(byte[] cipher) {
		byte[] outWithIV = new byte[iv.length + cipher.length];
		System.arraycopy(iv, 0, outWithIV, 0, iv.length);
		System.arraycopy(cipher, 0, outWithIV, iv.length, cipher.length);
		return outWithIV;
	}
}
