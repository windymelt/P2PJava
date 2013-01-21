package P2PJava;

import java.net.MalformedURLException;

import org.apache.ws.commons.util.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

class P2PClient /* extends Thread */{
	private XmlRpcClient client;
	private XmlRpcClientConfigImpl config;

	P2PClient(String hostname, int port) throws MalformedURLException {
		java.net.URL serverURL = new java.net.URL("http://" + hostname + ":"
				+ port + "/xmlrpc");
		// System.out.println("Making client connection: " +
		// serverURL.toString());
		client = new XmlRpcClient();
		config = new XmlRpcClientConfigImpl();
		config.setServerURL(serverURL);
		config.setEnabledForExtensions(true);
		config.setConnectionTimeout(60 * 1000);
		config.setReplyTimeout(60 * 1000);
		// set configuration
		client.setConfig(config);

	}
	idAddress whoAreYou() throws XmlRpcException {
		return (idAddress)execute("Node.whoAreYou");
	}
	boolean checkLiving() {
		try {
			ping();
			return true;
		} catch(XmlRpcException e) {
			return false;
		}
	}

	String ping() throws XmlRpcException {
		return (String) execute("Node.ping");
	}

	idAddress findNode(nodeID id_query) throws XmlRpcException {
		return (idAddress) execute("Node.findNode",
				Base64.encode(id_query.getArray()));
	}

	void amIPredecesor() throws XmlRpcException {
		execute("Node.amIPredecessor", chord.getInstance().self);
	}

	idAddress yourPredecessor() throws XmlRpcException {
		return (idAddress) execute("Node.yourPredecessor");
	}

	<T> Object execute(String name, T... param) throws XmlRpcException { // 可変長param,generic
		return client.execute(name, param);
	}
}