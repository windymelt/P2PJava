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
	 */
	public static void main(String[] args) throws MalformedURLException, NoSuchAlgorithmException, UnknownHostException, XmlRpcException, DecodingException {
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
	    chord.getInstance().init(ID, java.net.InetAddress.getLocalHost().getHostAddress(), serverport);

	    // INITIALISATION END
	    P2PClient clientone = new P2PClient("localhost", serverport); // 自分にping飛ばすため
	    System.out.println("Hello,Scala!");
	    String param[] = null;

	    System.out.println("localhost ID:");
	    //System.out.println(clientone.execute("Node.ping", param));
	    System.out.println(clientone.execute("Node.ping", param));
	    if(args.length == 3){
	    	// パラメータが3あればjoinしてみる
	    	chord.getInstance().join(new hostPair(args[1], Integer.parseInt(args[2])));
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
	    	System.out.println(new Node().findNode(arguments[1]).IDval.getBase64());
	    	break;
	    case "save":
	    	System.out.println("あなたは "+arguments[1]+" を入力しました。");
		    System.out.println(Base64.encode(chord.getInstance().saveData("moko", arguments[1].getBytes())));
		    break;
	    case "load":
	    	System.out.println(arguments[1]+" をロードします。");
		    String responseStr = new String(chord.getInstance().loadData(Base64.decode(arguments[1])));
		    System.out.println(responseStr);
		    break;
	    }
	    
	    }
	}
}
