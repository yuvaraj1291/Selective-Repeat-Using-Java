import java.io.Serializable;

/**
 * POJO class for Ack
 *
 */
public class Ack implements Serializable{
	
	private static final long serialVersionUID = -247685297003174361L;
	
	private int ackNo;

	public int getAckNo() {
		return ackNo;
	}

	public void setAckNo(int ackNo) {
		this.ackNo = ackNo;
	}
	
}
