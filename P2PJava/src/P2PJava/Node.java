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
		String idBase64 = Base64.encode(chord.getInstance().selfID.idVal);
		System.out.println("Pingを受信しました: ノード名は " + idBase64);
		return idBase64;
	}

	public idAddress findNode(String queryID) throws MalformedURLException,
			XmlRpcException, DecodingException {
		// あるChord空間上の位置から時計回りに巡り、最初に遭遇したノードが担当ノードである。
		// CCW <= |XaaaaaaaaAbbbbbbbbBccCddddddddDeeEF...xxx| => CW
		// 自己のノードlIDとクエリIDを比較し、等しければ自己のidAddressを返す。
		// 　等しくなければ、
		// 　Succussessorと自己のIDとの距離(Pとする)、クエリIDと自己のIDとの距離（Qとする）を比較し、
		// P>QならばSuccessorノードがクエリIDの担当者であるから、SuccessorにwhoAreYouを送信し、その返り値を自己の返り値とする。
		// P<Qならば自己の知り得る限りではクエリIDの担当者はいないから、Successorに処理を委譲し、回答を待つ。
		System.out.println("findNodeを受信:" + queryID);

		if (chord.getInstance().selfID.getBase64().equals(queryID)) { // 「あ、それ僕です」
			return new idAddress(chord.getInstance().selfID,
					chord.getInstance().selfHostName,
					chord.getInstance().selfPort);
		}

		BigInteger self_succ = nodeID.distance(chord.getInstance().selfID,
				chord.getInstance().succList.first().IDval);
		BigInteger self_query = nodeID.distance(
				new nodeID(Base64.decode(queryID)), chord.getInstance().selfID);

		String succHostname = chord.getInstance().succList.first()
				.getHostname();
		int succPort = chord.getInstance().succList.first().getPort();
		P2PClient successor = new P2PClient(succHostname, succPort);

		System.out.println("距離差モード: " + self_query.compareTo(self_succ));

		// 自分が孤独の場合は無条件に自分を返す
		if (self_query.compareTo(self_succ) <= 0
				|| chord.getInstance().selfID.idVal == chord.getInstance().succList
						.first().IDval.idVal) {
			System.out.println("次のノードが該当ノードです");
			nodeID[] nullarray = new nodeID[0];
			return (idAddress) successor.execute("Node.whoAreYou", nullarray);
		} else {
			System.out.println("メッセージを転送 => "
					+ chord.getInstance().succList.first().IDval.getBase64());
			String[] param = new String[1];
			param[0] = queryID;
			System.out.println("param: " + param[0]);
			return (idAddress) successor.execute("Node.findNode", param);
		}
	}

	// 自分のノード+アドレス情報を返す。
	public idAddress whoAreYou() {
		System.out.println("whoAreYouを受信");
		return new idAddress(chord.getInstance().selfID,
				chord.getInstance().selfHostName, chord.getInstance().selfPort);
	}

	// 自分のPredecessorを返す。
	public idAddress yourPredecessor(String dummy) throws MalformedURLException {
		System.out.println("yourPredecessorを受信");

		if (chord.getInstance().predID != null) {
			if (!chord.getInstance().stabilizerIsOn) {
				chord.getInstance().startStabilizer();
				chord.getInstance().stabilizerIsOn = true;
			}
			if (chord.getInstance().selfID.idVal == chord.getInstance().succList
					.first().IDval.idVal) {
				chord.getInstance().succList.clear();
				chord.getInstance().succList.add(chord.getInstance().predID,
						chord.getInstance().predAddress);
			}
			// predecessorが生きてるか確認する
			P2PClient cliPred = new P2PClient(
					chord.getInstance().predAddress.getHost(),
					chord.getInstance().predAddress.getPort());
			Object[] nullParam = new Object[0];
			// nullParam[0] = null;
			try {
				cliPred.execute("Node.ping", nullParam);
				// ping成功、相手は生きてる
				return new idAddress(chord.getInstance().predID,
						chord.getInstance().predAddress.getHost(),
						chord.getInstance().predAddress.getPort());
			} catch (XmlRpcException e) {
				// ping失敗、相手は死んでる
				// e.printStackTrace();
				return new idAddress(chord.getInstance().selfID,
						chord.getInstance().selfHostName,
						chord.getInstance().selfPort);
			}
		} else {
			return null; // neg
		}
	}

	public idAddress yourSuccessor(String dummy) {
		System.out.println("yourSuccessorを受信");
		return new idAddress(chord.getInstance().succList.first().getID(),
				chord.getInstance().succList.first().getHostname(),
				chord.getInstance().succList.first().getPort());
	}

	// 自分のPredecessorが正当か確認させる。
	public boolean amIPredecessor(idAddress id) {
		System.out.println("amIPredecessorを受信");
		return chord.getInstance().checkPredecessor(id); // chord
	}

	public byte[] getChunk(byte[] id) {
		if (chord.getInstance().dataHolderValue.containsKey(id)) {
			byte[] answer = chord.getInstance().dataHolderValue.get(id);
			return answer;
		} else {
			return null;
		}
	}
	
	public void putChunk(byte[] id, byte[] value) {
		chord.getInstance().dataHolderValue.put(id, value);
	}

}
