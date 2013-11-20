package machines;

public class BlockCBC extends CipherBlock {

	public BlockCBC(byte[] iv, byte[][] key, int klen) {
		super(key, klen);
		super.iv = iv;
		super.lastIV = iv;
	}

	@Override
	public byte[] encryption(byte[] data, int len) {
		if (len == 0) {
			return new byte[0];
		}
		// fazer xor com o resultado anterior no plaintext antes de cifrar
		byte[][] matrix = convertToBlockMatrix(data, len);

		for (int i = 0; i < matrix.length; i++) {
			// Start XOR
			matrix[i] = xor_func(lastIV, matrix[i]);
			// encrypt block
			matrix[i] = AESAlgorithm.encryptBloc(matrix[i], 0, key, keyLen);

			super.lastIV = matrix[i];
		}
		byte[] out = convertFromBlockMatrix(matrix, len);
		return out;
	}

	@Override
	public byte[] decryption(byte[] data, int len) {
		if (len == 0) {
			return new byte[0];
		}
		// fazer xor com o resultado anterior no plaintext antes de cifrar
		byte[][] matrix = convertToBlockMatrix(data, len);

		super.lastIV = super.iv;
		for (int i = 0; i < matrix.length; i++) {
			// Save current IV to be the next
			lastIV = matrix[i];

			// decrypt block
			matrix[i] = AESAlgorithm.decryptBloc(matrix[i], 0, key, keyLen);
			// XOR LAST
			matrix[i] = xor_func(matrix[i], super.iv);
			// Update the next IV to use
			super.iv = super.lastIV;
		}
		byte[] out = convertFromBlockMatrix(matrix, len);
		return out;
	}
}
