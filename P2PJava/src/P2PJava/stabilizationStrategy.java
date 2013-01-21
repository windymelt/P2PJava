package P2PJava;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;

interface stabilizationStrategy {
	public abstract void doStrategy();
}

class stabilizationSuccDeadStrategy implements stabilizationStrategy{
	public void doStrategy() {
		System.out.println("Successorに接続できません");
		System.out.println("Successorを破棄");
		if (chord.getInstance().succManager.count() > 1) {
			chord.getInstance().succManager.remove1st();
			try {
				chord.getInstance().join(chord.getInstance().succManager.first().getHostPair());
			} catch (MalformedURLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (XmlRpcException e) {
				// TODO 自動生成された catch ブロック
				return; // やりなおす
			}
		} else {
			try {
				chord.getInstance().join(chord.getInstance().pred.getHostPair());
			} catch (MalformedURLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (XmlRpcException e) {
				// ついに全接続が継続できなくなった
				System.out.println("全接続経路が破綻しました。停止します");
				chord.getInstance().stopStabilizer();
				chord.getInstance().succManager.clear();
				chord.getInstance().succManager.add(chord.getInstance().self);
				chord.getInstance().pred = null;
			}
		}
	}
}
class stabilizationPreSuccDeadStrategy implements stabilizationStrategy{
	public void doStrategy() {
		//amipredec.
		try {
			chord.getInstance().succManager.first().getClient().amIPredecesor();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (XmlRpcException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
class stabilizationRightStrategy implements stabilizationStrategy{
	public void doStrategy() {
		System.out.println("Right Strategy");
		try {
			chord.getInstance().succManager.first().getClient().amIPredecesor();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (XmlRpcException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
class stabilizationGaucheStrategy implements stabilizationStrategy{
	public void doStrategy() {
		idAddress preSucc = null;
		try {
			preSucc = chord.getInstance().succManager.first().getClient().yourPredecessor();
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		} catch (XmlRpcException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return;
		}
		System.out.println("Gauche Strategy: New Successor is: " + preSucc.getID().getBase64());
		//rewrite self.
		chord.getInstance().succManager.clear();
		chord.getInstance().succManager.add(preSucc);
		
		 try {
			chord.getInstance().succManager.first().getClient().amIPredecesor();
		} catch (MalformedURLException | XmlRpcException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
class stabilizationOKStrategy implements stabilizationStrategy{
	public void doStrategy() {
		System.out.println("It's all right");
	}
}
