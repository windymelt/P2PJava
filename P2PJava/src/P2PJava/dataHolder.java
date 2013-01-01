package P2PJava;

import java.util.HashMap;

import org.apache.ws.commons.util.Base64;

public class dataHolder {
	HashMap<byte[], byte[]> data = new HashMap<byte[], byte[]>();
	byte[] get(byte[] key) {
		return data.get(key);
	}
	void put(byte[] key, byte[] value) {
		System.out.println(Base64.encode(key) + " inserted");
		data.put(key, value);
	}
	boolean containsKey(byte[] key) {
		return data.containsKey(key);
	}
}