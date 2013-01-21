package P2PJava;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.apache.xmlrpc.XmlRpcException;

public class chord {
	private static final chord instance = new chord(); // インスタンス。static
														// getInstance()でアクセス可能。
	idAddress self = null;
	IdManager succManager = new IdManager(); // 複数のSuccessorを格納する。現在実装では[0]のみを使用。
	IdManager fingerManager = new IdManager(); // 複数のfingerを格納する。現在実装では使用しない。
	idAddress pred = null;
	Timer stabilizeTimer = null;
	boolean stabilizerIsOn = false;
	dataHolder dataHolderValue = new dataHolder();

	synchronized public static chord getInstance() { // Singleton
														// Patternの実装。instanceへの唯一のアクセス方法
		return instance;
	}

	private chord() {
	} // Singleton Pattern用のダミーのコンストラクタ

	// 初期化メソッド。起動時に必ず実行しなければならない。self関連の情報を代入し、初期のSuccessorを自己に設定する
	void init(nodeID ID, String host, int port) {
		System.out.println("Chordを初期化します");
		self = new idAddress(ID, host, port);
		succManager.add(self);
		pred = null;
	}

	private void sleepForRandom() {
		try {
			Thread.sleep((long) (Math.random() * 100));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void startStabilizer() {
		// 定期的にSuccessorを更新するスレッドを起動する
		if (stabilizerIsOn) {
			return;
		}
		System.out.println("スタビライザタイマを起動中");
		sleepForRandom();
		TimerTask stabilize = new stabilizeSuccessor();
		stabilizeTimer = new Timer("Stabilizer timer");
		stabilizeTimer.schedule(stabilize, 1000, 10 * 1000);
		stabilizerIsOn = true;
	}
	public void stopStabilizer() {
		if (!stabilizerIsOn) {
			return;
		}
		System.out.println("スタビライザタイマを停止中");
		stabilizeTimer.cancel();
		stabilizerIsOn = false;
	}
	// P2Pネットワークに参加する。参加の踏み台にする、任意の、このプログラムが稼動しているホストのhostPairを引数に取る。成功したらtrueを返す。
	// まず踏み台にアプローチし、自分のノードIDが担当すべきエリアを現在担当しているノード（前任者）の情報を入手する。
	// 前任者のノード情報を入手したら、SuccessorListに登録。この際、前のSuccessorListの情報は無意味になるので破棄しておく。
	// Successorスタビライザを起動し、処理を終了する。
	synchronized boolean join(hostPair connectTo) throws MalformedURLException, XmlRpcException {
		System.out.println("chord instance: join: " + connectTo.getHost() + ":"
				+ connectTo.getPort());

			idAddress addr_receivedSucc = connectTo.getClient().findNode(
					self.getID());
			succManager.clear();
			succManager.add(addr_receivedSucc);
			pred = null;


		chord.getInstance().startStabilizer();
		System.out.println("Joinに成功しました");
		return true;
	}

	// SuccessorListを動的に更新する。これにより新規ノードの参加処理は完了する。
	// まずSuccessorListのファーストノード（最近傍ノード）にアプローチし、ファーストのPredecessorを取得する。
	// ファーストのPredecessorが自分の場合は異常はない。
	// これが自分でない場合は、相手方が自分を認知していないか、自分が誤ったSuccessorに接続している可能性がある。
	// 相手方に問題がある（相手方のPredecessorのID<自分のID<相手方のID）場合、相手方に修正を要求する。
	// 自分に問題がある（自分のID<相手方のPredecessorのID<相手方のID）場合、自分のSuccessorListを破棄し、
	// 相手方のPredecessorを自分のSuccessorとして追加する。
	// CAUTION: 初期状態ではPred/SuccともにSelfを参照していることに留意されたし
	// TODO:　異常ノードの切断処理を追加されたし
	synchronized void stabilizeSuccessor2() {
		System.out.println("I am: " + self.getID().getBase64());
		System.out.println("successor is: "
				+ succManager.first().getID().getBase64());

		boolean succliving = checkSuccLiving();
		boolean presuccliving = checkPreSuccLiving();
		boolean rightness = checkRightness();
		boolean consistentness = checkConsistentness();

		System.out.println("Consistentness: " + consistentness);
		stabilizationStrategy strategy = null;
		strategy = stabilizationStrategyFactory.createStrategy(succliving,
				presuccliving, rightness, consistentness);
		strategy.doStrategy();
	}

	private boolean checkSuccLiving() {
		try {
			return succManager.first().getClient().checkLiving();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}
	}

	private boolean checkPreSuccLiving() {
		try {
			return succManager.second().getClient().checkLiving();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}
	}

	private boolean checkPredLiving() {
		try {
			return pred.getClient().checkLiving();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}
	}

	private boolean checkRightness() {
		nodeID id_Succ = succManager.first().getID();
		nodeID id_preSucc = null;
		if (self.getID().equals(id_Succ)) {
			return false; // 自分が孤独状態ならすぐに譲る
		}
		try {
			idAddress preSucc = succManager.first().getClient()
					.yourPredecessor();
			if (preSucc == null) {
				return true;
			}
			id_preSucc = preSucc.getID();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		} catch (XmlRpcException e) {
			// TODO 自動生成された catch ブロック
			return false;
		}
		return nodeID.belongs(self.getID(), id_preSucc, id_Succ);
	}

	private boolean checkConsistentness() {
		try {
			idAddress preSucc = succManager.first().getClient()
					.yourPredecessor();
			if(preSucc == null) {
				System.out.println("preSucc is null");
				return false;
				}
			System.out.println("presucc is: " + preSucc.getID().getBase64());
			return self.getID().getBase64().equals(preSucc.getID().getBase64());
		} catch (MalformedURLException | XmlRpcException e) {
			// TODO 自動生成された catch ブロック
			System.out.println("<<Consistent fail>>");
			return false;
		}
	}

	// }

	// 自分のPredecessorと告知されたIDのどちらが正当（ほとんどの場合告知されたIDが正当）か確認する。
	// 告知されたIDが距離的に近い場合、自分のPredecessorを破棄して告知されたIDに書き換える。
	// このメソッドはNodeのamIPredeessorによって呼ばれる。また、amIPredecessorはchordのスタビライザが呼び出す。
	boolean checkPredecessor(idAddress saidIDAddress) {
		// このへん
		if (saidIDAddress.getID().equals(self.getID())) {
			return false;
		}
		if (pred == null
				|| nodeID.belongs(saidIDAddress.getID(), pred.getID(),
						self.getID()) || !checkPredLiving())
			System.out.println("Predecessorに変更: "
					+ Base64.encode(saidIDAddress.getID().getArray()));
		pred = saidIDAddress;

		return true;
	}

	byte[] saveData(String title, byte[] value)
			throws NoSuchAlgorithmException, MalformedURLException,
			DecodingException, XmlRpcException {

		metaData meta = new metaData(title, value);
		System.out.println(meta.toString());
		/*
		 * for (int i = 0; i > value.length; i += 1024) { if (i + 1024 >
		 * value.length) { splitedData[i] = new String(value).substring(i, i +
		 * 1024); } else { splitedData[i] = new String(value).substring(i,
		 * value.length - 1); } }
		 */
		for (int i = 0; i < meta.chunks.length; i++) {
			Node nd = new Node();
			idAddress addr = nd.findNode(Base64
					.encode(meta.checksum_Chunk_SHA1[i]));
			P2PClient cliNode = new P2PClient(addr.getHostname(),
					addr.getPort());
			cliNode.execute("Node.setChunk", meta.checksum_Chunk_SHA1[i],
					meta.chunks[i].getBytes());
		}
		Node nd = new Node();
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(meta.toString().getBytes());
		idAddress addr = nd.findNode(Base64.encode(md.digest()));
		P2PClient cliNode = new P2PClient(addr.getHostname(), addr.getPort());
		cliNode.execute("Node.setChunk", md.digest(), meta.toString()
				.getBytes());
		return md.digest();
	}

	byte[] loadData(byte[] key) throws MalformedURLException,
			DecodingException, XmlRpcException {
		Node nd = new Node();
		idAddress addr = nd.findNode(Base64.encode(key));
		P2PClient cliNode = new P2PClient(addr.getHostname(), addr.getPort());
		String meta = ((byte[]) cliNode.execute("Node.getChunk", key))
				.toString();
		if (meta == null)
			return null;

		String[] splitedMeta = meta.split("\n");
		String filename = splitedMeta[0];
		String checksum = splitedMeta[1];
		int size = Integer.parseInt(splitedMeta[2]);
		int count = Integer.parseInt(splitedMeta[3]);
		String[] chunks = new String[count];
		for (int i = 0; i < count; i++) {
			chunks[i] = splitedMeta[i + 4];
		}

		byte[][] data = new byte[count][];
		for (int i = 0; i < count; i++) {
			;
			addr = nd.findNode(Base64.encode(key));
			cliNode = new P2PClient(addr.getHostname(), addr.getPort());

			data[i] = (byte[]) cliNode.execute("Node.getChunk",
					chunks[i].getBytes());
			if (data[i] == null)
				return null;
		}

		byte[] finalData = new byte[size];
		for (int i = 0; i < count; i++) {
			if (i == count) {
				System.arraycopy(data, 0, finalData, i * 1024, size
						- (count - 1) * 1024);
			} else {
				System.arraycopy(data, 0, finalData, i * 1024, 1024);
			}
		}

		return finalData;
	}

	idAddress getSelfIdAddress() {
		return self.clone();
	}
}
