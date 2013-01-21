package P2PJava;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;

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

	P2PClient getClient() throws MalformedURLException {
		return new P2PClient(hostval, portval);
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

	nodeID(String id) throws DecodingException {
		idVal = Base64.decode(id);
	}

	byte[] getArray() {
		return idVal;
	}

	// 円形空間における距離を測定する。
	static BigInteger distance(nodeID id1, nodeID id2) {
		final BigInteger x = new BigInteger(1, id1.getArray());
		final BigInteger y = new BigInteger(1, id2.getArray());
		final BigInteger HALF = new BigInteger("2").pow(159);
		final BigInteger final_x_y_subtract;

		final BigInteger x_y_subtract = x.subtract(y).abs(); // abs(x-y)
		if (x_y_subtract.compareTo(HALF) >= 0) {
			final_x_y_subtract = x_y_subtract.subtract(HALF);
		} else {
			final_x_y_subtract = x_y_subtract;
		}
		return final_x_y_subtract;
	}

	String getBase64() {
		return Base64.encode(idVal);
	}
	
	public static boolean belongs(nodeID X, nodeID alpha, nodeID omega) {
		BigInteger big_X = X.toBigInt();
		BigInteger big_alpha = alpha.toBigInt();
		BigInteger big_omega = omega.toBigInt();
		
		if (big_alpha.compareTo(big_omega) == 0) {return true;}
		if (big_omega.compareTo(big_alpha) < 0) { // Z-A < 0 <=> A -> 0 -> Z
			boolean caseLeft = big_alpha.compareTo(big_X) < 0 && big_X.compareTo(CHORDSIZE) < 0;
			boolean caseRight = BigInteger.ZERO.compareTo(big_X) < 0 && big_omega.compareTo(big_X) >= 0;
			return caseLeft || caseRight; // A<X<END OR 0<X<Z
		} else {
			return big_alpha.compareTo(big_X) < 0 && big_X.compareTo(big_omega) <= 0; // A<X<Z
		}
	}

	@Override
	public int hashCode() {
		// TODO 自動生成されたメソッド・スタブ
		return idVal.hashCode();
	}


	@Override
	public String toString() {
		return getBase64();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof nodeID) {
			return getBase64().equals(((nodeID) obj).getBase64());
		} else {
			assert false:"equals must be use with nodeID: " + obj.toString();
			return false;
		}
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

class IdManager implements Serializable {
	private static final long serialVersionUID = 1L;
	LinkedHashMap<nodeID, hostPair> idListVal = new LinkedHashMap<nodeID, hostPair>();

	idAddress first() {
		// System.out.println(idListVal.keySet().toArray()[0]);
		// System.out.println(idListVal.entrySet().toArray()[0]);
		/*
		 * nodeID firstID = idListVal.keySet().iterator().next(); hostPair
		 * firstPair = idListVal.values().iterator().next();
		 */
		nodeID firstID = (nodeID) (idListVal.keySet().toArray()[0]);
		hostPair firstPair = ((Map.Entry<nodeID, hostPair>) idListVal
				.entrySet().toArray()[0]).getValue();

		return new idAddress(firstID, firstPair.getHost(), firstPair.getPort());
	}

	idAddress second() {
		if (idListVal.size() > 1) {
			/*
			 * idListVal.keySet().iterator().next();
			 * idListVal.values().iterator().next(); nodeID secondID =
			 * idListVal.keySet().iterator().next(); hostPair secondPair =
			 * idListVal.values().iterator().next();
			 */
			nodeID secondID = (nodeID) (idListVal.keySet().toArray()[1]);
			hostPair secondPair = ((Map.Entry<nodeID, hostPair>) idListVal
					.entrySet().toArray()[1]).getValue();
			return new idAddress(secondID, secondPair.getHost(),
					secondPair.getPort());
		} else {
			return first();
		}
	}

	idAddress third() {
		if (idListVal.size() > 2) {
			/*
			 * idListVal.keySet().iterator().next();
			 * idListVal.values().iterator().next();
			 * idListVal.keySet().iterator().next();
			 * idListVal.values().iterator().next(); nodeID thirdID =
			 * idListVal.keySet().iterator().next(); hostPair thirdPair =
			 * idListVal.values().iterator().next();
			 */
			nodeID thirdID = (nodeID) (idListVal.keySet().toArray()[2]);
			hostPair thirdPair = ((Map.Entry<nodeID, hostPair>) idListVal
					.entrySet().toArray()[2]).getValue();
			return new idAddress(thirdID, thirdPair.getHost(),
					thirdPair.getPort());
		} else {
			return second();
		}
	}

	idAddress nearestNeighbor(nodeID id_query) {
		nodeID nearest = null;
		
		for(Iterator<nodeID> keyIter = idListVal.keySet().iterator(); keyIter.hasNext();) {
			nodeID next = keyIter.next();
			if (nearest == null){
				nearest = next;
			} else {
				//  self -> next -> query, d(nearest,query)>d(next,query).
				if(nodeID.belongs(next, chord.getInstance().self.getID(), id_query) && nodeID.distance(next, id_query).compareTo(nodeID.distance(nearest, id_query)) < 0) {
					nearest = next;
				}
			}
		}
		return new idAddress(nearest, idListVal.get(nearest));
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

	int count() {
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
	idAddress(nodeID ID, hostPair pair) {
		IDval = ID;
		hostval = pair.getHost();
		portval = pair.getPort();
	}

	P2PClient getClient() throws MalformedURLException {
		return new P2PClient(hostval, portval);
	}

	@Override
	public idAddress clone() {
		return new idAddress(IDval, hostval, portval);
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
