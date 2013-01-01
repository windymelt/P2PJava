package P2PJava;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.ws.commons.util.Base64;

public class metaData {
	String fileNameVal = "";
	BigInteger fileSize = new BigInteger("0");
	String[] chunks = null;
	byte[] checksum_SHA1 = null;
	byte[][] checksum_Chunk_SHA1;

	metaData(String fileName, byte[] data) throws NoSuchAlgorithmException {
		fileNameVal = fileName;
		fileSize = new BigInteger(String.valueOf(data.length));
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(data);
		checksum_SHA1 = md.digest();
		if (fileSize.divideAndRemainder(new BigInteger("1024"))[1].intValue() == 0) {
			chunks = new String[fileSize.divide(new BigInteger("1024"))
					.intValue()];
		} else {
			chunks = new String[fileSize.divide(new BigInteger("1024"))
					.intValue() + 1];
		}

		/*
		 * 分割処理
		 */
		byte[][] splitedData = new byte[Math.round(data.length / 1024) + 1][1024];
		for (int i = 0; i < Math.round(data.length / 1024); i++) {
			for (int j = 0; j < 1024; j++) {
				System.arraycopy(data, i * 1024 + j, splitedData[i], 0, 1024);
			}
		}

		checksum_Chunk_SHA1 = new byte[chunks.length][];
		for (int i = 0; i < splitedData.length; i++) {
			md.update(splitedData[i]);
			checksum_Chunk_SHA1[i] = md.digest();
			chunks[i] = Base64.encode(checksum_Chunk_SHA1[i]);
		}
	}

	public String toString() {
		String answer = fileNameVal + "\n" + checksum_SHA1 + "\n"
				+ fileSize.toString() + "\n" + String.valueOf(chunks.length)
				+ "\n";
		for (byte[] hash : checksum_Chunk_SHA1) {
			answer += Base64.encode(hash);
		}
		return answer;
	}
}
// TODO: fileTransporter classを作成しておくこと