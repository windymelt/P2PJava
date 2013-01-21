package P2PJava;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.apache.xmlrpc.XmlRpcException;

public class P2PJava {
	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnknownHostException 
	 * @throws XmlRpcException 
	 * @throws DecodingException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws MalformedURLException, NoSuchAlgorithmException, UnknownHostException, DecodingException, InterruptedException {
		// TODO Auto-generated method stub
		int serverport;
		
		if(args.length > 0){
		      serverport = Integer.parseInt(args[0]); // テスト機能　サーバのポートを変更
		    } else {
		      serverport = 8090;
		    }
		System.out.println("サーバポートは: " + serverport);
		P2PServer server = new P2PServer(serverport);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
	    //md.update(java.net.InetAddress.getLocalHost().toString().getBytes());
		md.update(Long.toHexString(System.currentTimeMillis()).getBytes());
		nodeID ID = new nodeID(md.digest());

	    server.start();
	    Thread.sleep(1000);
	    chord.getInstance().init(ID, java.net.InetAddress.getLocalHost().getHostAddress(), serverport);

	    // INITIALISATION END
	    P2PClient clientone = new P2PClient("localhost", serverport); // 自分にping飛ばすため
	    System.out.println("Hello, Java!");
	    System.out.println("localhost ID:");

	    try {
			System.out.println(clientone.execute("Node.ping"));
		} catch (XmlRpcException e) {
			System.err.println("ping failed: " + e.getMessage());
		}
	    if(args.length == 3){
	    	// パラメータが3あればjoinしてみる
	    	try {
				chord.getInstance().join(new hostPair(args[1], Integer.parseInt(args[2])));
			} catch (NumberFormatException | XmlRpcException e) {
				// TODO 自動生成された catch ブロック
				System.err.println("接続に失敗しました");
			}
	    }
	    
	    Scanner in = new Scanner(System.in);
	    String str;
	    String[] arguments;
	    while(true) {
	    System.out.println("何か入力してEnterキーを押してください。：");
	    str = in.nextLine();
	    arguments = str.split(" ");
	    System.out.println(str);
	    switch (arguments[0]) {
	    case "find":
	    	System.out.println("finding " + arguments[1]);
	    	try {
				System.out.println(new Node().findNode(arguments[1]).IDval.getBase64());
			} catch (XmlRpcException e) {
				System.err.println("Find failed: " + e.getMessage());
			}
	    	break;
	    case "save":
	    	System.out.println("あなたは "+arguments[1]+" を入力しました。");
		    try {
				System.out.println(Base64.encode(chord.getInstance().saveData("moko", arguments[1].getBytes())));
			} catch (XmlRpcException e) {
				System.err.println("Save failed: " + e.getMessage());
			}
		    break;
	    case "load":
	    	System.out.println(arguments[1]+" をロードします。");
		    String responseStr;
			try {
				responseStr = new String(chord.getInstance().loadData(Base64.decode(arguments[1])));
				System.out.println(responseStr);
			} catch (XmlRpcException e) {
				System.err.println("Load faield: " + e.getMessage());
			}
		    
		    break;
	    }
	    
	    }
	}
}
