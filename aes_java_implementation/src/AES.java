import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * AES [Ðe/d] [Ðecb/cbc/ctr] [Ðf Keyfile] [Ðk Key] [Ði inputfile] [Ðo outputfile]
 Ðe/d : e (encryption) or d (decryption) mode. By omission the encryption mode is used.
 Ðecb/cbc/ctr : block cipher mode selection. By omission the CBC mode is used.
 Ðf Keyfile : specification of the ciphering key via a file. Keyfile specifies the path of the raw file with the ciphering key bits (in binary format).
 Ðk Key : specification of the ciphering key via command shell input. Key specifies the ciphering key bits (in hexadecimal format).
 Ði inputfile : specification of the file with the input data. inputfile specifies the path of the file with the data to be ciphered (in binary format).
 Ðo outputfile : specification of the file with the output data. outputfile specifies the path of the destination file with the ciphered data (in binary format).
 */
public class AES {
	private static final int DECRYPT = 0;
	private static final int ENCRYPT = 1;

	private static final int ECB = 0;
	private static final int CBC = 1;
	private static final int CTR = 2;

	private static final int BLOCK_READ_SIZE_BYTES = 10000000;
	private static final boolean USING_JAVA = true;

	int way = DECRYPT;
	int mode = ECB;
	byte[] key = null;
	String keyFile = null;
	String inputFile = null;
	String outputFile = null;
	AISSecurity algorithm = null;
	String outputFileJava = null;

	public static void main(String[] args) throws Exception {
		AES app = new AES();
		app = AES.readInterfaceInput(args, app);
		/*
		 * app.mode = ECB; app.way = ENCRYPT; app.keyFile = "key.txt";
		 * 
		 * if (app.way == ENCRYPT) { app.inputFile = "data.txt"; app.outputFile
		 * = "cipher.txt"; app.outputFileJava = "javax.txt"; } else {
		 * app.inputFile = "cipher.txt"; app.outputFile = "decrypt.txt";
		 * app.outputFileJava = "javalho.txt"; }
		 */
		app.algorithm = new AISSecurity();
		app.readKeyFile();

		app.cipherFile();

		if (USING_JAVA) {
			app.useJava();
			if (FileAccess.fileEquals(app.outputFile, app.outputFileJava)) {
				System.out.println("Files Are Equal");
			} else {
				System.out.println("ERROR");
			}
		}
	}

	private void useJava() throws Exception {
		String instance;
		switch (mode) {
		case CBC:
			instance = "AES/CBC/PKCS5Padding";
			break;
		case CTR:
			instance = "AES/CTR/PKCS5Padding";
			break;
		case ECB:
			instance = "AES/ECB/PKCS5Padding";
			break;
		default:
			throw new Exception("Invalid mode");
		}
		int opmode;
		if (way == DECRYPT) {
			opmode = Cipher.DECRYPT_MODE;
		} else {
			opmode = Cipher.ENCRYPT_MODE;
		}
		Cipher c = Cipher.getInstance(instance);
		Key keyObj = new SecretKeySpec(key, "AES");

		System.out.println("Init Java: " + new Date().getTime());

		// File Control parameters
		File file = new File(inputFile);
		byte[] data = new byte[BLOCK_READ_SIZE_BYTES];
		InputStream input = new BufferedInputStream(new FileInputStream(file));

		File fileOut = new File(outputFileJava);
		fileOut.delete();
		BufferedOutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(fileOut, true));

		int bytesRead = 0;
		int bytesReaded = 0;
		int fileLength = (int) file.length();
		byte[] ciphered;
		boolean ivIgnore = false;
		byte[] iv = null;

		System.out.println("Init Start: " + new Date().getTime());
		if (mode == CBC || mode == CTR) {
			if ((opmode == Cipher.DECRYPT_MODE) && (mode == CTR)) {
				byte[] ivData = new byte[16];
				bytesRead = input.read(ivData, 0, 16);
			}
			c.init(opmode, keyObj, new IvParameterSpec(
					algorithm.cipherMachine.iv));
			if (opmode == Cipher.ENCRYPT_MODE) {
				iv = c.getIV();
				outputStream.write(iv);
			}
		} else {
			c.init(opmode, keyObj);
		}

		System.out.println("Cicle Start: " + new Date().getTime());
		while ((bytesRead = input.read(data, 0, BLOCK_READ_SIZE_BYTES)) != -1) {
			double remain = (fileLength - bytesReaded)
					/ (double) BLOCK_READ_SIZE_BYTES;
			if (remain <= 1) {
				System.out.println("Final: " + new Date().getTime());
				ciphered = c.doFinal(data, 0, bytesRead);
			} else {
				System.out.println("Update: " + new Date().getTime());
				ciphered = c.update(data, 0, bytesRead);

			}
			bytesReaded += bytesRead;

			if (mode == CBC && opmode == Cipher.DECRYPT_MODE && !ivIgnore) {
				ivIgnore = true;
				outputStream.write(ciphered, 16, ciphered.length - 16);
			} else {
				outputStream.write(ciphered);
			}
		}
		System.out.println("Finish, go close: " + new Date().getTime());
		input.close();
		outputStream.close();
		System.out
				.println("Finish Our Implementation: " + new Date().getTime());
	}

	private void cipherFile() throws Exception {
		System.out.println("Init Our Implementation: " + new Date().getTime());

		File file = new File(inputFile);
		byte[] data = new byte[BLOCK_READ_SIZE_BYTES];
		InputStream input = new BufferedInputStream(new FileInputStream(file));

		File fileOut = new File(outputFile);
		fileOut.delete();
		BufferedOutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(fileOut, true));

		int bytesRead = 0;
		int bytesReaded = 0;
		int fileLength = (int) file.length();
		byte[] ciphered;

		// INIT Cipher mode
		System.out.println("Init Start: " + new Date().getTime());
		algorithm.init(way, mode, key);
		System.out.println("Cicle Start: " + new Date().getTime());
		while ((bytesRead = input.read(data, 0, BLOCK_READ_SIZE_BYTES)) != -1) {
			double remain = (fileLength - bytesReaded)
					/ (double) BLOCK_READ_SIZE_BYTES;
			if (remain <= 1) {
				System.out.println("Final: " + new Date().getTime());
				ciphered = algorithm.doFinal(data, bytesRead);
			} else {
				System.out.println("Update: " + new Date().getTime());
				ciphered = algorithm.update(data, bytesRead);

			}
			bytesReaded += bytesRead;
			outputStream.write(ciphered);
		}
		System.out.println("Finish, go close: " + new Date().getTime());
		input.close();
		outputStream.close();
		System.out
				.println("Finish Our Implementation: " + new Date().getTime());

	}

	public static AES readInterfaceInput(String[] args, AES app)
			throws Exception {
		// Read args:
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			try {
				arg = arg.replaceFirst("-", "");

				switch (Options.valueOf(arg)) {
				// encryption
				case e:
					app.way = ENCRYPT;
					break;
				case d:
					app.way = DECRYPT;
					break;
				case ecb:
					app.mode = ECB;
					break;
				case cbc:
					app.mode = CBC;
					break;
				case ctr:
					app.mode = CTR;
					break;
				case f:
					app.keyFile = args[++i];
					break;
				case k:
					// read key from Hexa to byte
					app.key = AES.readHexKey(args[++i]);
					break;
				case i:
					app.inputFile = args[++i];
					break;
				case o:
					app.outputFile = args[++i];
					break;
				default:
					throw new Exception("Invalid option");
				}
			} catch (IllegalArgumentException e) {
				throw new Exception("Invalid Argument");
			}
		}

		if (app.keyFile != null) {
			app.readKeyFile();
		}
		if (app.key == null || app.inputFile == null || app.outputFile == null) {
			System.out.println(app.key);
			System.out.println(app.inputFile);
			System.out.println(app.outputFile);

			throw new Exception("Missing arguments: key, input, output");
		}

		return app;

	}

	private void readKeyFile() throws Exception {
		key = FileAccess.readAllFileAtOnce(keyFile);
	}

	private static byte[] readHexKey(String key) {
		byte[] out = new byte[key.length() / 2];
		for (int i = 0; i < key.length(); i += 2) {
			out[i / 2] = (byte) Integer.parseInt(key.substring(i, i + 1), 16);
		}
		return out;
	}

}
