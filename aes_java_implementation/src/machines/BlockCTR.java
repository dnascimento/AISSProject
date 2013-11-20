package machines;

public class BlockCTR extends CipherBlock {

	byte[] lastNonce = null;
	int usedBytes = 0;

	public BlockCTR(byte[] nonce, byte[][] key, int klen) {
		super(key, klen);
		super.iv = nonce;
		super.lastIV = null;
		lastNonce = null;
	}

	/**
	 * Counter Mode Turns: gerar um nouce incremental de 128bits que Ž
	 * inicializado com o nouce e incrementado a cada round. Este nouce Ž
	 * cifrado com a chave e XORed com o plaintext para dar a cifra
	 * 
	 * @param data
	 * @param key
	 * @param nouce
	 * @return
	 */
	@Override
	public byte[] encryption(byte[] data, int len) {
		if (lastNonce == null) {
			lastNonce = new byte[iv.length];
			System.arraycopy(iv, 0, lastNonce, 0, iv.length);
		}
		byte[] cipheredData = new byte[len];

		int bytesCipher = 0;
		byte[] cipher = AESAlgorithm.encryptBloc(lastNonce, 0, key, keyLen);

		while (bytesCipher < len) {
			if (usedBytes >= 16) {
				lastNonce = incrementCounter(lastNonce);
				usedBytes = 0;
				cipher = AESAlgorithm.encryptBloc(lastNonce, 0, key, keyLen);
			}

			// cipher block
			cipheredData[bytesCipher] = xor_func(data[bytesCipher],
					cipher[usedBytes]);
			bytesCipher++;
			usedBytes++;
		}
		return cipheredData;
	}

	private byte xor_func(byte a, byte b) {
		return (byte) (a ^ b);
	}

	private byte[] incrementCounter(byte[] nonce) {
		incrementAtIndex(nonce, nonce.length - 1);
		return nonce;
	}

	private void incrementAtIndex(byte[] array, int index) {
		if (array[index] == Byte.MAX_VALUE) {
			array[index] = 0;
			if (index > 0) {
				incrementAtIndex(array, index - 1);
			}
		} else {
			array[index]++;
		}
	}

	@Override
	public byte[] decryption(byte[] data, int len) {
		return encryption(data, len);
	}

}
