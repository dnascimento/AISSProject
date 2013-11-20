import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FileAccess {

	private static final int BLOCK_READ_SIZE_BYTES = 10000;

	public static void writeToFile(byte[] data, String outputFile,
			boolean append) throws IOException {
		File file = new File(outputFile);
		BufferedOutputStream output = null;
		try {
			output = new BufferedOutputStream(
					new FileOutputStream(file, append));
			output.write(data);
		} finally {
			output.close();
		}
	}

	/**
	 * Read All file
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] readAllFileAtOnce(String filePath) throws IOException {
		File file = new File(filePath);
		int len = (int) file.length();
		byte[] data = new byte[len];
		InputStream input = null;
		input = new BufferedInputStream(new FileInputStream(file));
		int totalBytesRead = 0;
		while (totalBytesRead < len) {
			int bytesRemaining = len - totalBytesRead;
			int bytesRead = input.read(data, totalBytesRead, bytesRemaining);
			totalBytesRead += bytesRead;
		}
		input.close();
		return data;
	}

	public static boolean fileEquals(String filePathA, String filePathB)
			throws IOException {
		File fileA = new File(filePathA);
		File fileB = new File(filePathB);
		long len = fileA.length();
		if (len != fileB.length()) {
			System.out.println("Different file length");
			return false;
		}
		InputStream fA = new BufferedInputStream(new FileInputStream(fileA));
		InputStream fB = new BufferedInputStream(new FileInputStream(fileB));
		byte[] dataA = new byte[BLOCK_READ_SIZE_BYTES];
		byte[] dataB = new byte[BLOCK_READ_SIZE_BYTES];

		while (fA.read(dataA, 0, BLOCK_READ_SIZE_BYTES) != -1) {
			fB.read(dataB, 0, BLOCK_READ_SIZE_BYTES);
			if (!Arrays.equals(dataA, dataB)) {
				fA.close();
				fB.close();
				return false;
			}
		}
		fA.close();
		fB.close();
		return true;
	}
}
