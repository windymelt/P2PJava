package P2PJava;
import java.net.MalformedURLException;
import java.util.TimerTask;

class stabilizeSuccessor extends TimerTask {
	public void run() {
		try {
			chord.getInstance().stabilizeSuccessor(); // chordのスタビライザを起動させる
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}