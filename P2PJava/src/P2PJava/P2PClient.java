package P2PJava;
import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

class P2PClient /*extends Thread*/ {
	private XmlRpcClient client;
	private XmlRpcClientConfigImpl config;
	
	P2PClient(String hostname, int port) throws MalformedURLException {
		java.net.URL serverURL  = new java.net.URL("http://" + hostname + ":" + port + "/xmlrpc");
	  System.out.println("Making client connection: " + serverURL.toString());
	  client = new XmlRpcClient();
	  config = new XmlRpcClientConfigImpl();
	  config.setServerURL(serverURL);
	  config.setEnabledForExtensions(true);
	  config.setConnectionTimeout(60 * 1000);
	  config.setReplyTimeout(60 * 1000);
	  // set configuration
	  client.setConfig(config);

	}
  //	  val host = "192.168.0.198";


  Object execute(String name, Object[] param) throws XmlRpcException {
   
		Object answer = client.execute(name, param);
		if (answer!=null) {
			return answer;
		} else {
			return null;
		}

}
}