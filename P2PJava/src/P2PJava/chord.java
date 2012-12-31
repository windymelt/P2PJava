package P2PJava;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.ws.commons.util.Base64;
import org.apache.xmlrpc.XmlRpcException;

public class chord {
	private static final chord instance = new chord(); // インスタンス。static
														// getInstance()でアクセス可能。
	nodeID selfID = null; // 自己のnodeID
	String selfHostName = null; // 自己のホスト名
	int selfPort = 0; // 自己のサーバのポート番号
	idList succList = new idList(); // 複数のSuccessorを格納する。現在実装では[0]のみを使用。
	idList fingerList = new idList(); // 複数のfingerを格納する。現在実装では使用しない。
	nodeID predID = null; // 単一のpredecessorのIDを格納する。
	hostPair predAddress = null; // 単一のpredecessorのアドレス情報を格納する。
									// TODO:上の変数とともにidAddressにすればいいのではないか
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
		selfID = ID;
		selfHostName = host;
		selfPort = port;
		succList.add(selfID, selfHostName, selfPort);
		predID = selfID;
		predAddress = new hostPair(selfHostName, selfPort);

	}

	public void startStabilizer() {
		// 定期的にSuccessorを更新するスレッドを起動する
		System.out.println("スタビライザタイマを起動中");
		TimerTask stabilize = new stabilizeSuccessor();
		Timer stabilizeTimer = new Timer("Stabilizer timer");
		stabilizeTimer.schedule(stabilize, 1000, 30 * 1000);
	}

	// P2Pネットワークに参加する。参加の踏み台にする、任意の、このプログラムが稼動しているホストのhostPairを引数に取る。成功したらtrueを返す。
	// まず踏み台にアプローチし、自分のノードIDが担当すべきエリアを現在担当しているノード（前任者）の情報を入手する。
	// 前任者のノード情報を入手したら、SuccessorListに登録。この際、前のSuccessorListの情報は無意味になるので破棄しておく。
	// Successorスタビライザを起動し、処理を終了する。
	boolean join(hostPair connectTo) throws MalformedURLException {
		System.out.println("chord instance: join: " + connectTo.getHost() + ":"
				+ connectTo.getPort());

		P2PClient client = new P2PClient(connectTo.getHost(),
				connectTo.getPort());
		String[] param = { Base64.encode(selfID.getArray()) };
		// System.out.println(param);
		idAddress receivedSuccessor;
		try {
			receivedSuccessor = (idAddress) client.execute("Node.findNode",
					param);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} // =>
			// (Array[Byte],
			// String)
		succList.clear();
		succList.add(receivedSuccessor.getID(),
				receivedSuccessor.getHostname(), receivedSuccessor.getPort());
		try {
			client = new P2PClient(succList.first().getHostname(), succList
					.first().getPort());
			idAddress[] param2 = { new idAddress(selfID, selfHostName, selfPort) };
			client.execute("Node.amIPredecessor", param2);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if (!stabilizerIsOn)
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
	synchronized void stabilizeSuccessor() throws MalformedURLException {
		System.out.println("スタビライザ稼動");
		System.out.println("My Succ: "
				+ chord.getInstance().succList.first().IDval.getBase64());
		System.out
				.println("My Pred: " + chord.getInstance().predID.getBase64());
		/*
		 * もしSuccessorがSelfの場合は処理を中断する
		 */
		if (chord.getInstance().succList.first().IDval.idVal == selfID.idVal)
			return;
		/*
		 * Successorが参照しているPredecessorを取得する
		 */
		// 1: 接続オブジェクトの作成
		P2PClient cliSucc = new P2PClient(succList.first().getHostname(),
				succList.first().getPort());
		String[] param = new String[1];
		param[0] = "";
		// 2: Successorと接続し、データを取得する
		idAddress hisPred; // maybe idAddress
		try {
			hisPred = (idAddress) cliSucc
					.execute("Node.yourPredecessor", param);

			System.out.println("SuccessorによるProdecessorを受信: "
					+ hisPred.getID().getBase64());
		} catch (XmlRpcException e) {
			// 受信不可能の場合、代替ノードに接続を変更する。
			// まずはsuccListを走査し、空ならばpredに接続し自然に回復させる。
			// e.printStackTrace();
			System.out.println("Successorに接続できません");
			// succListの状態により処理を変更
			System.out.println("Successorを破棄");
			succList.remove1st();
			if (succList.isEmpty()) {
				System.out.println("SuccessorをPredecessorに移行");
				succList.add(predID, predAddress);
			}
			// ここで処理を中断する
			return;
		}

		nodeID hisPredID = hisPred.getID();
		nodeID mySuccID = succList.first().getID();

		System.out.println("HisPredのID: " + hisPredID.getBase64());
		System.out.println("SelfのID: " + selfID.getBase64());

		if (hisPredID.getBase64().equals(selfID.getBase64())) {
			// Successorの参照するPredecessorがSelfのIDと等しいとき（異常なし）
			System.out.println("ルーティングテーブルに変更なし");
			// return;
		} else {
			/*
			 * Successorの参照するPredecessorとSelfのIDが違うとき
			 * Successorが誤ったPredecessorを参照しているか、 Selfが誤ったSuccessorを参照している。
			 */
			// Successorから両ノードへの距離を測定する
			BigInteger self_succ = nodeID.distance(mySuccID, selfID);
			BigInteger succPred_succ = nodeID.distance(mySuccID, hisPredID);
			if (self_succ.compareTo(succPred_succ) < 0
					|| hisPredID.getBase64().equals(mySuccID.getBase64())) {
				// SuccのPredよりも自分が近い,もしくは相手のPredは初期状態でPred自身を参照しているので更新させる
				Object[] params = { new idAddress(selfID, selfHostName,
						selfPort) };// new Object[2];
				try {
					cliSucc.execute("Node.amIPredecessor", params);
				} catch (XmlRpcException e) {
					e.printStackTrace();
					return;
				}
			} else {
				// 自分のSuccを換えるべき
				System.out.println("Successorに変更:"
						+ Base64.encode(hisPredID.getArray()));
				succList.clear();
				succList.add(hisPredID, hisPred.getHostname(),
						hisPred.getPort());
			}
		}
		/*
		 * succListは常に3ノード確保する
		 */
		if (succList.count() < 3) {
			P2PClient cliSuccSucc = new P2PClient(succList.second()
					.getHostname(), succList.second().getPort());
			Object[] nullParam = { null };

			switch (succList.count()) {
			case 1:
				try {
					succList.add((idAddress) cliSucc.execute(
							"Node.yourPredecessor", nullParam));
				} catch (XmlRpcException e) {
					// TODO 自動生成された catch ブロック
					System.out.println(e.getMessage());
				}
				try {
					succList.add((idAddress) cliSuccSucc.execute(
							"Node.yourPredecessor", nullParam));
				} catch (XmlRpcException e) {
					// TODO 自動生成された catch ブロック
					System.out.println(e.getMessage());
				}
				
				break;
			case 2:
				try {
					succList.add((idAddress) cliSuccSucc.execute(
							"Node.yourPredecessor", nullParam));
				} catch (XmlRpcException e) {
					// TODO 自動生成された catch ブロック
					System.out.println(e.getMessage());
				}
			}
		}
	}

	// 自分のPredecessorと告知されたIDのどちらが正当（ほとんどの場合告知されたIDが正当）か確認する。
	// 告知されたIDが距離的に近い場合、自分のPredecessorを破棄して告知されたIDに書き換える。
	// このメソッドはNodeのamIPredeessorによって呼ばれる。また、amIPredecessorはchordのスタビライザが呼び出す。
	/* synchronized */boolean checkPredecessor(idAddress saidIDAddress) {
		// このへん
		System.out.println("Predecessorに変更: "
				+ Base64.encode(saidIDAddress.getID().getArray()));
		predID = saidIDAddress.getID();
		predAddress = saidIDAddress.getHostPair();

		return true;
	}
}
