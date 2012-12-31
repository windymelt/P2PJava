package P2PJava;

import java.io.IOException;

//import javax.xml.soap.Node;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;

class P2PServer extends Thread {
  int serverport = 8090;
  P2PServer(int port) {
	  serverport = port;
  }
  public void run() {
    
    try{
    System.out.println("Starting Server.");
    
	WebServer server = new WebServer(serverport);
    
    XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
	PropertyHandlerMapping handler = new PropertyHandlerMapping();
    try{
      //add handler class.
       handler.addHandler(Node.class.getSimpleName(), Node.class);
       // set handler to server.
       xmlRpcServer.setHandlerMapping(handler);
       // xml-rpc server config
       XmlRpcServerConfigImpl serverConfig =  (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
       serverConfig.setEnabledForExtensions(true);
       serverConfig.setContentLengthOptional(false);
      } catch(XmlRpcException e) {
      e.printStackTrace();
      }
      server.start();
      System.out.println("Server ready");
    } catch(java.net.BindException e) { // bind failed
    	System.out.println("アドレスをバインドできませんでした。\n" + e.toString());
    	System.exit(1);
    } catch(IOException e) {
    e.printStackTrace();
    } 
	  
  }
}