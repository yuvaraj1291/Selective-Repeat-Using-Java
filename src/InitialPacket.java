import java.io.Serializable;

public class InitialPacket implements Serializable{

	private static final long serialVersionUID = 9215586497573731864L;
	
	private int mss;
	private int windowSize;
	private int totalSeqNo;
	private boolean selectiveRepeat;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getMss() {
		return mss;
	}
	public void setMss(int mss) {
		this.mss = mss;
	}
	public int getWindowSize() {
		return windowSize;
	}
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
	public int getTotalSeqNo() {
		return totalSeqNo;
	}
	public void setTotalSeqNo(int totalSeqNo) {
		this.totalSeqNo = totalSeqNo;
	}
	public boolean isSelectiveRepeat() {
		return selectiveRepeat;
	}
	public void setSelectiveRepeat(boolean selectiveRepeat) {
		this.selectiveRepeat = selectiveRepeat;
	}
}
