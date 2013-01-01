package P2PJava;

import java.math.BigInteger;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ws.commons.util.Base64;

/**
 * @author qwilas
 * 
 */
// ホスト名とポート番号のペア
class hostPair implements Serializable {
	private static final long serialVersionUID = 451602865065879348L;
	String hostval = "";
	int portval = 8090;

	hostPair(String host, int port) {
		hostval = host;
		portval = port;
	}

	String getHost() {
		return hostval;
	}

	int getPort() {
		return portval;
	}
}

// ノードのID。0から2^160 -1までの空間であるBigIntegerをIDとして保持する。
class nodeID implements Serializable {
	private static final long serialVersionUID = 1L;
	static BigInteger CHORDSIZE = (new BigInteger("2")).pow(160);
	byte[] idVal;

	nodeID(byte[] id) {
		idVal = id;
	}

	byte[] getArray() {
		return idVal;
	}

	// 円形空間における距離を測定する。
	static BigInteger distance(nodeID id1, nodeID id2) {
		BigInteger x = new BigInteger(1, id1.getArray());
		BigInteger y = new BigInteger(1, id2.getArray());
		if (x.subtract(y).abs().compareTo(new BigInteger("2").pow(159)) <= 0) {
			return x.subtract(y).mod(CHORDSIZE).abs()/* .mod(CHORDSIZE) */;
		} else {
			return new BigInteger("2").pow(160).subtract(x.subtract(y).mod(CHORDSIZE).abs())/*
																			 * .mod
																			 * (
																			 * CHORDSIZE
																			 * )
																			 */;
		}

		// final double smaller = 4.299129844735844 * (10^-48); // magic number;
		// (2Pi)/2^160.

	}

	String getBase64() {
		return Base64.encode(idVal);
	}

	byte at(int i) {
		return idVal[i];
	}

	int length() {
		return idVal.length;
	}

	BigInteger toBigInt() {
		return new BigInteger(idVal);
	}

	BigInteger abs() {
		return new BigInteger(idVal).abs();
	}

}

class idList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	LinkedHashMap<nodeID, hostPair> idListVal = new LinkedHashMap<nodeID, hostPair>();

	idAddress first() {
		//System.out.println(idListVal.keySet().toArray()[0]);
		//System.out.println(idListVal.entrySet().toArray()[0]);
		/*nodeID firstID = idListVal.keySet().iterator().next();
		hostPair firstPair = idListVal.values().iterator().next();*/
		nodeID firstID = (nodeID)(idListVal.keySet().toArray()[0]);
		hostPair firstPair = ((Map.Entry<nodeID, hostPair>)idListVal.entrySet().toArray()[0]).getValue();

		return new idAddress(firstID, firstPair.getHost(), firstPair.getPort());
	}
	idAddress second() {
		if (idListVal.size() > 1) {
		/*idListVal.keySet().iterator().next();
		idListVal.values().iterator().next();
		nodeID secondID = idListVal.keySet().iterator().next();
		hostPair secondPair = idListVal.values().iterator().next();*/
		nodeID secondID = (nodeID)(idListVal.keySet().toArray()[1]);
		hostPair secondPair = ((Map.Entry<nodeID, hostPair>)idListVal.entrySet().toArray()[1]).getValue();
		return new idAddress(secondID, secondPair.getHost(), secondPair.getPort());
		} else {
			return first();
		}
	}
	idAddress third() {
		if (idListVal.size() > 2) {
		/*idListVal.keySet().iterator().next();
		idListVal.values().iterator().next();
		idListVal.keySet().iterator().next();
		idListVal.values().iterator().next();
		nodeID thirdID = idListVal.keySet().iterator().next();
		hostPair thirdPair = idListVal.values().iterator().next();*/
		nodeID thirdID = (nodeID)(idListVal.keySet().toArray()[2]);
		hostPair thirdPair = ((Map.Entry<nodeID, hostPair>)idListVal.entrySet().toArray()[2]).getValue();
		return new idAddress(thirdID, thirdPair.getHost(), thirdPair.getPort());
		} else {
			return second();
		}
	}
	void add(nodeID id, String hostname, int port) {
		idListVal.put(id, new hostPair(hostname, port));
	}

	void add(nodeID id, hostPair pair) {
		idListVal.put(id, pair);
	}

	void add(idAddress id) {
		idListVal.put(id.getID(), new hostPair(id.hostval, id.portval));
	}

	boolean remove(nodeID id) {
		if (idListVal.containsKey(id)) {
			idListVal.remove(id);
			return true;
		} else {
			return false;
		}
	}

	boolean remove1st() {
		return remove(first().getID());
	}

	void clear() {
		idListVal.clear();
	}
	boolean isEmpty() {
		return idListVal.isEmpty();
	}
	int count () {
		return idListVal.size();
	}
}

class idAddress implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	nodeID IDval;
	String hostval;
	int portval = 8090;

	idAddress(nodeID ID, String hostname, int port) {
		IDval = ID;
		hostval = hostname;
		portval = port;
	}

	nodeID getID() {
		return IDval;
	}

	String getHostname() {
		return hostval;
	}

	int getPort() {
		return portval;
	}

	hostPair getHostPair() {
		return new hostPair(hostval, portval);
	}
}
