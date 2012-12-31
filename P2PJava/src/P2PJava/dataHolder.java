package P2PJava;

import java.util.HashMap;

public class dataHolder {
	HashMap<byte[], byte[]> data = new HashMap<byte[], byte[]>();
	byte[] get(byte[] key) {
		return data.get(key);
	}
	void put(byte[] key, byte[] value) {
		data.put(key, value);
	}
	boolean containsKey(byte[] key) {
		return data.containsKey(key);
	}
}