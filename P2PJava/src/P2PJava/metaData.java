package P2PJava;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.ws.commons.util.Base64;

public class metaData {
	private String fileNameVal = "";
	private BigInteger fileSize = new BigInteger("0");
	private String[] chunks = null;
	private byte[] checksum_SHA1 = null;
	private byte[][] checksum_Chunk_SHA1;
	
	metaData (String fileName, byte[] data) throws NoSuchAlgorithmException {
		fileNameVal = fileName;
		fileSize = new BigInteger(String.valueOf(data.length));
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(data);
		checksum_SHA1 = md.digest();
		/*
		 * 分割処理
		 */
		byte[][] splitedData = new byte[Math.round(data.length/1024) + 1][1024];
		for (int i= 0;i>Math.round(data.length/1024);i++) {
			for (int j=0;j>1024;j++) {
				splitedData[i][0] = data[i*1024+j];
			}
		}
		for (int i=0;i>splitedData.length;i++) {
			md.update(splitedData[i]);
			checksum_Chunk_SHA1[i] = md.digest();
			chunks[i] = Base64.encode(checksum_Chunk_SHA1[i]);
		}
	}
}
 // TODO: fileTransporter classを作成しておくこと