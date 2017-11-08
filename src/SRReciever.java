import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class SRReciever {

	private static int MSS=0;
	private static int WINDOW_SIZE=0;
	private static int TOTAL_SEQUENCE_NO=0;
	private static double LOST_PACKET_PROBABLITY=0.1;
	private static double CHECKSUMERROR_PROBABLITY=0.05;

	private static String SUCCESS="SUCCESS";
	//private static  recieverSocket;

	public static void main(String args[]) throws Exception{

		int socketNo=Integer.valueOf(args[0]);
		//int socketNo=9987;
		DatagramSocket recieverSocket = new DatagramSocket(socketNo);

		DatagramPacket datagramPacket=null;
		byte[] buf = null;

		//Initiate transfer
		buf = new byte[128];
		datagramPacket = new DatagramPacket(buf, buf.length);
		recieverSocket.receive(datagramPacket);
		InitialPacket initialPacket=(InitialPacket)CommonUtils.byteArraytoObject(datagramPacket.getData());

		MSS=initialPacket.getMss();
		WINDOW_SIZE=initialPacket.getWindowSize();
		TOTAL_SEQUENCE_NO=initialPacket.getTotalSeqNo();
		byte[] message=SUCCESS.getBytes();
		datagramPacket=new DatagramPacket(message,message.length , datagramPacket.getAddress(),datagramPacket.getPort());

		recieverSocket.send(datagramPacket);

		System.out.println(" Recieved Initial Params Ready to recieve !!!!!!!!!!");


		if(initialPacket.isSelectiveRepeat()){
			selectiveRepeatReciever(recieverSocket);
		}else{
			//Go Back N

		}

		//closing the file
		recieverSocket.close();

	}

	public static void selectiveRepeatReciever(DatagramSocket recieverSocket) throws Exception{

		BufferedWriter bw = new BufferedWriter(new FileWriter("SelectiveRepeatOut.txt"));

		DatagramPacket datagramPacket=null;
		byte[] buf = null;

		Packet packet=null;
		Ack ack=null;
		byte[] ackData=null;

		Map<Integer, String> unOrderedMap=new HashMap<>();
		int start=0;
		int end=WINDOW_SIZE;
		int expectedSequenceNo=0;
		boolean lastFlag=false;
		boolean completedFlag=false;
		int lastSequenceNo=0;
		while(true){
			buf = new byte[MSS];
			datagramPacket = new DatagramPacket(buf, buf.length);
			recieverSocket.receive(datagramPacket);

			if(Math.random()<=LOST_PACKET_PROBABLITY){
				System.out.println("Packet Lost");
				continue;
			}

			packet=(Packet)CommonUtils.byteArraytoObject(datagramPacket.getData());
			System.out.println("Recieved Packet"+packet.getSequenceNo());


			if(packet.isLast()){
				lastFlag=true;
				lastSequenceNo=packet.getSequenceNo();
			}


			if(packet.getCheckSum()!=CommonUtils.generateCheckSum(packet.getData())){
				System.out.println("Bit error in Packet"+packet.getSequenceNo());
				continue;
			}

			//Forming the ACK for the sender
			ack=new Ack();
			ack.setAckNo(packet.getSequenceNo());
			ackData=CommonUtils.objectToByteArray(ack);

			datagramPacket=new DatagramPacket(ackData, ackData.length, datagramPacket.getAddress(),datagramPacket.getPort());

			recieverSocket.send(datagramPacket);
			System.out.println("Send ACK For Packet "+ack.getAckNo());



			if(packet.getSequenceNo()==expectedSequenceNo){
				String	data=new String(packet.getData());
				while(true){
					bw.write(data);
					if(lastFlag && expectedSequenceNo==lastSequenceNo){
						completedFlag=true;
						break;
					}
					if(expectedSequenceNo<TOTAL_SEQUENCE_NO-1){
						expectedSequenceNo++;
					}else{
						expectedSequenceNo=0;
					}
					//System.out.println("Expected seq NO"+expectedSequenceNo+"  "+lastFlag);
					if(start<TOTAL_SEQUENCE_NO-1){
						start++;
					}else{
						start=0;
					}

					if(end<TOTAL_SEQUENCE_NO-1){
						end++;
					}else{
						end=0;
					}
					if(unOrderedMap.containsKey(expectedSequenceNo)){
						data=unOrderedMap.get(expectedSequenceNo);
						unOrderedMap.remove(new Integer(expectedSequenceNo));
					}else{
						break;
					}
				}
			}else{
				if(start<end){
					if(packet.getSequenceNo()>=start && packet.getSequenceNo()<end){
						if(!unOrderedMap.containsKey(packet.getSequenceNo())){
							unOrderedMap.put(packet.getSequenceNo(), new String(packet.getData()));
						}
					}
				}else{
					if(packet.getSequenceNo()>=start || packet.getSequenceNo()<end){
						if(!unOrderedMap.containsKey(packet.getSequenceNo())){
							unOrderedMap.put(packet.getSequenceNo(), new String(packet.getData()));
						}
					}
				}
			}
			if(completedFlag){
				break;
			}
		}
		bw.close();
	}

}
