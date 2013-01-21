package P2PJava;
import java.util.TimerTask;

class stabilizeSuccessor extends TimerTask {
	public void run() {
		chord.getInstance().stabilizeSuccessor2(); // chordのスタビライザ"2"を起動させる 
	}
}