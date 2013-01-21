package P2PJava;

import java.math.BigInteger;
import java.net.MalformedURLException;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.apache.xmlrpc.XmlRpcException;

public class Node {

	// var connectingNode: Array[Float] = null;
	// pingを受け付ける。Base64-encodedの自己IDを返す。
	public String ping() {
		return chord.getInstance().self.getID().getBase64();
	}
public idAddress findNode(String queryID) throws DecodingException, MalformedURLException, XmlRpcException {
	final nodeID id_query = new nodeID(queryID);
	final nodeID id_self = chord.getInstance().self.getID();
	final nodeID id_succ = chord.getInstance().succManager.first().getID();
	
	if (id_self.equals(id_query)) {
		return chord.getInstance().getSelfIdAddress();
	}
	if (nodeID.belongs(id_query, id_self, id_succ)) {
		P2PClient cli_succ = chord.getInstance().succManager.first().getClient(); 
		return cli_succ.whoAreYou();
	} else {
		return chord.getInstance().succManager.nearestNeighbor(id_query).getClient().findNode(id_query);
	}
}
	public idAddress findNode_old(String queryID) throws MalformedURLException,
			XmlRpcException, DecodingException {
		// あるChord空間上の位置から時計回りに巡り、最初に遭遇したノードが担当ノードである。
		// CCW <= |XaaaaaaaaAbbbbbbbbBccCddddddddDeeEF...xxx| => CW
		// 自己のノードlIDとクエリIDを比較し、等しければ自己のidAddressを返す。
		// 　等しくなければ、
		// 　Successorと自己のIDとの距離(Pとする)、クエリIDと自己のIDとの距離（Qとする）を比較し、
		// P>QならばSuccessorノードがクエリIDの担当者であるから、SuccessorにwhoAreYouを送信し、その返り値を自己の返り値とする。
		// P<Qならば自己の知り得る限りではクエリIDの担当者はいないから、Successorに処理を委譲し、回答を待つ。
		final nodeID id_queryID = new nodeID(queryID);
		final nodeID id_self = chord.getInstance().self.getID();

		System.out.println("findNodeを受信:" + queryID);

		if (id_self.equals(id_queryID)) { // 「あ、それ僕です」
			System.out.println("IT'S ME!");
			return chord.getInstance().getSelfIdAddress();
		}

		final idAddress addr_firstSucc = chord.getInstance().succManager.first();
		/*
		 * 距離測定
		 */
		final BigInteger distance_self_succ, distance_self_query;
		distance_self_succ = nodeID.distance(id_self, addr_firstSucc.IDval);
		distance_self_query = nodeID.distance(id_queryID, id_self);

		final P2PClient cli_successor = addr_firstSucc.getClient();

		System.out.println("self-succ間の距離: " + distance_self_succ.toString());
		System.out.println("self-query間の距離: " + distance_self_query.toString());
		System.out.println("差: "
				+ distance_self_query.subtract(distance_self_succ));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		if (distance_self_query.compareTo(distance_self_succ) <= 0
				|| distance_self_succ.compareTo(BigInteger.ZERO) == 0/*id_self.equals(addr_firstSucc.getID())*/) {
			System.out.println("次のノードが該当ノードです");
			return (idAddress) cli_successor.execute("Node.whoAreYou");
		} else {
			System.out.println("メッセージを転送 => "
					+ addr_firstSucc.IDval.getBase64());
			System.out.println("param: " + queryID);
			return (idAddress) cli_successor.execute("Node.findNode", queryID);
		}
	}


	// 自分のノード+アドレス情報を返す。
	public idAddress whoAreYou() {
		System.out.println("whoAreYouを受信");
		return chord.getInstance().self.clone();
	}

	// 自分のPredecessorを返す。
	public idAddress yourPredecessor() throws MalformedURLException {
		// System.out.println("yourPredecessorを受信");

		if (chord.getInstance().pred != null) {
			
				chord.getInstance().startStabilizer();

		
			// predecessorが生きてるか確認する
			try {
				chord.getInstance().pred.getClient().execute("Node.ping");
				// ping成功、相手は生きてる
				return chord.getInstance().pred.clone();
			} catch (XmlRpcException e) {
				// ping失敗、相手は死んでる
				// e.printStackTrace();
				return null;
			}
		} else {
			return null; // neg
		}
	}

	public idAddress yourSuccessor() {
		// System.out.println("yourSuccessorを受信");
		return new idAddress(chord.getInstance().succManager.first().getID(),
				chord.getInstance().succManager.first().getHostname(),
				chord.getInstance().succManager.first().getPort());
	}

	// 自分のPredecessorが正当か確認させる。
	public boolean amIPredecessor(idAddress id_obj) {
		// System.out.println("amIPredecessorを受信");
		idAddress id = (idAddress) id_obj;
		return chord.getInstance().checkPredecessor(id); // chord
	}

	public byte[] getChunk(byte[] id) {
		System.out.println("Data loading.. : " + Base64.encode(id));
		if (chord.getInstance().dataHolderValue.data.isEmpty()) {
			System.out.println("Empty");
		} else {
			System.out.println("not empty");
		}
		if (chord.getInstance().dataHolderValue.containsKey(id)) {
			System.out.println("Found.");
			byte[] answer = chord.getInstance().dataHolderValue.get(id);
			return answer;
		} else {
			System.out.println("Not Found.");
			return null;
		}
	}

	public byte[] setChunk(byte[] id, byte[] value) throws DecodingException {
		System.out.println("Data Accepted: " + Base64.encode(id));
		chord.getInstance().dataHolderValue.put(id, value);
		return id;
	}

}
