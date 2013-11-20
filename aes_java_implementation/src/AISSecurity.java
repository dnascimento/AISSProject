import java.util.Random;

import machines.AESAlgorithm;
import machines.BlockCBC;
import machines.BlockCTR;
import machines.BlockECB;
import machines.CipherBlock;

public class AISSecurity {

	/********************** Cipher Methods ******************************/

	private static final int DECRYPT = 0;
	private static final int ENCRYPT = 1;

	private static final int ECB = 0;
	private static final int CBC = 1;
	private static final int CTR = 2;

	int cipher;
	int CMode;
	byte[] buffer;
	byte[] ivBuffer = new byte[16];
	int ivBufferSize = 0;
	static int bufferSize = 0;
	public CipherBlock cipherMachine = null;
	boolean firstTime = true;

	/**
	 * This method performs the required initializations, which include s the
	 * key expansion.
	 * 
	 * @param Cipher
	 *            : decipher = 0 cipher = 1;
	 * @param CMode
	 *            = ecb = 0 cbc = 1 ctr = 2
	 * @throws Exception
	 */
	void init(int Cipher, int CMode, byte[] key) throws Exception {
		int keyLen = key.length;
		// Key Expansion
		byte[][] expandedKey = AESAlgorithm.generateSubkeys(key);
		firstTime = true;

		cipher = Cipher;
		this.CMode = CMode;
		resetBuffer();

		switch (CMode) {
		case ECB:
			cipherMachine = new BlockECB(expandedKey, keyLen);
			break;
		case CBC:
			byte[] iv = keyGenerator(128);
			cipherMachine = new BlockCBC(iv, expandedKey, keyLen);
			break;
		case CTR:
			byte[] nonce = keyGenerator(128);
			cipherMachine = new BlockCTR(nonce, expandedKey, keyLen);
			break;
		default:
			throw new Exception("Invalid cipher mode");
		}
	}

	/**
	 * This method performs the ciphering of the input data, using the
	 * parameters specified in the init method. Note: If the last bytes of the
	 * input data do not form a full block, the bytes are not ciphered at that
	 * instance .
	 * 
	 * @param plaintext
	 * @return
	 * @throws Exception
	 */
	byte[] update(byte[] plaintext) throws Exception {
		return update(plaintext, plaintext.length);
	}

	byte[] update(byte[] plaintext, int inputlen) throws Exception {
		return cipherData(plaintext, inputlen, false);

	}

	/**
	 * This method performs the ciphering of the input data, using the
	 * parameters specified in the init method. This method also performs the
	 * necessary procedures to conclude the ciphering of the data stream,
	 * including the addition of necessary padding.
	 * 
	 * @param plaintext
	 * @return
	 * @throws Exception
	 */
	byte[] doFinal(byte[] plaintext) throws Exception {
		return doFinal(plaintext, plaintext.length);
	}

	byte[] doFinal(byte[] plaintext, int inputlen) throws Exception {
		byte[] out;
		if (CMode == CTR) {
			out = cipherData(plaintext, inputlen, false);
		} else {
			out = cipherData(plaintext, inputlen, true);
		}
		firstTime = false;
		return out;
	}

	byte[] cipherData(byte[] plaintext, int inputlen, boolean padding)
			throws Exception {

		// Se decifra, retirar o IV e colocar na decifra
		if (CMode != ECB && firstTime && cipher == DECRYPT) {
			// Data enough (buffer+plaintext) to extract prefix?
			if (ivBufferSize + inputlen > 16) {
				if (ivBufferSize != 0) {
					byte[] temp = new byte[ivBufferSize + inputlen];
					System.arraycopy(ivBuffer, 0, temp, 0, ivBufferSize);
					System.arraycopy(plaintext, 0, temp, ivBufferSize, inputlen);
					plaintext = temp;
				}
				plaintext = cipherMachine.exctractPrefix(plaintext);
				firstTime = false;
				ivBufferSize = 0;
				inputlen = plaintext.length;
			} else {
				System.arraycopy(plaintext, 0, ivBuffer, ivBufferSize, inputlen);
				ivBufferSize += inputlen;
				return new byte[0];
			}
		}

		// If buffer is not empty, append
		if (bufferSize != 0) {
			byte[] data = new byte[plaintext.length + bufferSize];
			System.arraycopy(buffer, 0, data, 0, bufferSize);
			int bufferSizeOld = bufferSize;
			resetBuffer();
			System.arraycopy(plaintext, 0, data, bufferSizeOld, inputlen);
			inputlen += bufferSizeOld;
			plaintext = data;
		}
		if (padding && cipher == ENCRYPT) {
			plaintext = paddingData(plaintext, inputlen);
			inputlen = plaintext.length;
		}

		// If not multiple size, buffer it if not using stream cipher
		if ((inputlen % 16) != 0 && CMode != CTR) {
			int fullsize = (inputlen + 16 - 1) / 16;
			fullsize = fullsize * 16;
			resetBuffer();
			bufferSize = 16 - (fullsize - inputlen);
			System.arraycopy(plaintext, fullsize - 16, buffer, 0, bufferSize);
			inputlen = inputlen - bufferSize;
		}

		if (cipher == ENCRYPT) {
			byte[] out = cipherMachine.encryption(plaintext, inputlen);
			// Se cifra, adicionar o prefixo antes de devolver cifrado.
			if (firstTime) {
				firstTime = false;
				out = cipherMachine.prefixAppend(out);
			}
			return out;
		} else {
			byte[] out = cipherMachine.decryption(plaintext, inputlen);
			// if its the last, unpadding it
			if (padding) {
				out = unpadding(out, inputlen);
			}
			return out;
		}
	}

	// //////////////////////// AUX ///////////////////////////

	/**
	 * Padding data using PKCS#5. Vemos quantos bytes estao por cifrar,
	 * escrevemos o numero de bytes em hexa ate ao fim. Se o pacote encher ate
	 * ao fim, temos sempre de colocar o padding por isso criamos um novo pacote
	 * 
	 * @param lenght
	 * @param data
	 * @param inputlen
	 * @return
	 */
	public static byte[] paddingData(byte[] data, int inputlen) {
		int padding = 0;
		// Determinar quanto padding tem de fazer para ficar multiplo de bloco
		if (inputlen % 16 == 0) {
			// Full package
			padding = 16;
		} else {
			int sizeTotal = (inputlen + 16 - 1) / 16;
			sizeTotal = sizeTotal * 16;
			padding = sizeTotal - inputlen;

		}
		// Fazer padding
		byte[] padded = new byte[inputlen + padding];

		int i = 0;
		while (i < inputlen) {
			padded[i] = data[i];
			i++;
		}
		while (i < (inputlen + padding)) {
			padded[i] = (byte) padding;
			i++;
		}
		return padded;
	}

	/**
	 * unpadding
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static byte[] unpadding(byte[] data, int inputlen) throws Exception {
		int padding = data[inputlen - 1];
		System.out.println(padding);

		for (int i = inputlen - 1; i > inputlen - 1 - padding; i--) {
			if (data[i] != padding) {
				throw new Exception("Wrong padding");
			}
		}
		byte[] result = new byte[inputlen - padding];

		System.arraycopy(data, 0, result, 0, inputlen - padding);
		return result;

	}

	private void resetBuffer() {
		buffer = new byte[16];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = 0x00;
		}
		bufferSize = 0;
	}

	/**
	 * Generate random key
	 * 
	 * @param lenght
	 *            bits
	 * @return key
	 */
	public static byte[] keyGenerator(int lenght) {
		int byteLenght = lenght / 8;
		byte[] key = new byte[byteLenght];
		new Random().nextBytes(key);
		return key;
	}

}
