import java.io.Serializable;
import java.util.Arrays;

/**
 * POJO class for Packets
 *
 */
public class Packet implements Serializable{

	private static final long serialVersionUID = -4039374647942245132L;
	
	private int sequenceNo;
	private byte[] data;
	private boolean isLast;
	private int checkSum;
	
	public int getSequenceNo() {
		return sequenceNo;
	}
	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public boolean isLast() {
		return isLast;
	}
	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}
	public int getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(int checkSum) {
		this.checkSum = checkSum;
	}
	@Override
	public String toString() {
		return "Packet [sequenceNo=" + sequenceNo + ", data=" + Arrays.toString(data) + ", isLast=" + isLast
				+ ", checkSum=" + checkSum + "]";
	}
	
}
