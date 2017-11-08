import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

public class SRSender{

	private static int SEGMENT_SIZE = 0;
	private static int WINDOW_SIZE=0;
	private static int TOTAL_SEQUENCE_NO=0;
	private static int TIMEOUT=0;
	private static int HEADER_SIZE=104;
	private static int MSS=0;
	private static double LOST_ACK_PROBABLITY=0.1;
	private static double BIT_ERROR_PROBABLITY=0.1;

	public static void main(String[] args) throws Exception {

		int socketNo=Integer.valueOf(args[1]);

		String paramFileName=args[0];
		String fileName = args[2];
		boolean selectiveRepeat=false;
		int m;
		int lineNo=1;
		try(BufferedReader br = new BufferedReader(new FileReader(paramFileName))) {
			for(String line; (line = br.readLine()) != null;) {
				// process the line.
				if(lineNo==1){
					if(line.trim().equalsIgnoreCase("SR")){
						selectiveRepeat=true;
					}
				}else if(lineNo==2){
					String[] lineArr=line.split(" ");
					m=Integer.parseInt(lineArr[0]);
					TOTAL_SEQUENCE_NO=(int)Math.pow(2, m);
					WINDOW_SIZE=Integer.parseInt(lineArr[1]);
				}else if(lineNo==3){
					TIMEOUT=Integer.parseInt(line.trim());
				}else if(lineNo==4){
					MSS=Integer.parseInt(line.trim());
				}
				lineNo++;
			}
			TIMEOUT=1000;
		}
		if(selectiveRepeat){
			selectiveRepeat(socketNo,fileName);
		}
	}

	public static void selectiveRepeat(int socketNo, String fileName) throws IOException, ClassNotFoundException{

		InetAddress receiverAddress = InetAddress.getByName("127.0.0.1");

		DatagramSocket senderSocket = new DatagramSocket();

		SEGMENT_SIZE=MSS-HEADER_SIZE;

		//reading the file
		byte[] data = Files.readAllBytes(Paths.get(fileName));
		LinkedHashMap<Integer, Packet> dataMap=new LinkedHashMap<>();

		int dataSize = data.length;
		int start = 0;
		byte[] currentSegmentData=null;
		byte[] manipulatedData=null;

		Packet packet=null;
		boolean isLast=false;
		int sequenceNo=0;
		DatagramPacket ackPacket=null;
		byte[] ackData=null;
		Ack ack=null;
		//int packetNo=1;
		int Sf=0;
		int Sn=0;
		boolean timeOut=false;

		//initial transfer
		InitialPacket initialPacket=new InitialPacket();
		initialPacket.setMss(MSS);
		initialPacket.setTotalSeqNo(TOTAL_SEQUENCE_NO);
		initialPacket.setWindowSize(WINDOW_SIZE);
		initialPacket.setSelectiveRepeat(true);
		byte[] intialPacket=CommonUtils.objectToByteArray((Object)initialPacket);

		DatagramPacket datagramPacket = new DatagramPacket(intialPacket,intialPacket.length, receiverAddress, socketNo);

		senderSocket.send(datagramPacket);

		byte[] intialAckData=new byte[128];
		ackPacket = new DatagramPacket(intialAckData, intialAckData.length);
		senderSocket.receive(ackPacket);

		System.out.println("sent initial params & connection is established "+new String(ackPacket.getData()));
		int packetSent=0;
		//initial transfer ends here
		while(true){
			while (dataSize > 0 || !dataMap.isEmpty()) {
				//System.out.println(" Sf "+Sf+" Sn"+Sn);
				timeOut=false;
				try{
					//isLast=false;
					currentSegmentData=null;
					if (dataSize > SEGMENT_SIZE) {
						currentSegmentData=Arrays.copyOfRange(data, start, start + SEGMENT_SIZE);//splitting the data into segments
						start+=SEGMENT_SIZE;
					}else if(dataSize>0){
						currentSegmentData=Arrays.copyOfRange(data, start, data.length);//last packet data
						isLast=true;

					}
					dataSize-=SEGMENT_SIZE;//decrementing the size since this data is sent
					//System.out.println("****************"+isLast+" datasize"+dataSize);
					if(currentSegmentData!=null){
						//Forming the packets
						packet=new Packet();
						packet.setLast(isLast);
						packet.setData(currentSegmentData);
						packet.setCheckSum(CommonUtils.generateCheckSum(currentSegmentData));
						packet.setSequenceNo(sequenceNo);
						dataMap.put(sequenceNo, packet);
						manipulatedData=packet.getData();
						//System.out.println("The Intital  checksum here is:"+packet.getCheckSum());



						if(Math.random()<=BIT_ERROR_PROBABLITY){

							byte[] byteArray = new byte[SEGMENT_SIZE];
							new Random(System.currentTimeMillis()).nextBytes(byteArray);

							packet.setData(byteArray);

							if(CommonUtils.generateCheckSum(packet.getData())!=CommonUtils.generateCheckSum(manipulatedData))
							{
								System.out.println("Bit error in Packet at "+packet.getSequenceNo());
							}

						}
						
						sendPacket(packet,senderSocket,receiverAddress,socketNo);
						System.out.println("Sending "+packet.getSequenceNo()+" Timer Started");
						packetSent++;		

						senderSocket.setSoTimeout(TIMEOUT);	
						packet.setData(manipulatedData);

						//Setting the Sn value
						if(sequenceNo<TOTAL_SEQUENCE_NO-1){
							sequenceNo++;
							Sn++;
						}else{
							sequenceNo=0;
							Sn=0;
						}

						//finding the no of packets sent
						int pos=((Sn-Sf)>=0)?(Sn-Sf):((Sn-Sf)+TOTAL_SEQUENCE_NO);

						//sending all the packets till the window size
						if(pos<WINDOW_SIZE && !isLast){
							//packetNo++;
							continue;
						}
					}
					//ack array
					boolean[] isAck=new boolean[WINDOW_SIZE];
					int totalAck=0;
					while(totalAck<packetSent){
						ackData=new byte[128];
						ackPacket = new DatagramPacket(ackData, ackData.length);
						senderSocket.receive(ackPacket);

						if(Math.random()<=LOST_ACK_PROBABLITY && !isLast){
							System.out.println("Ack lost");
							continue;
						}

						ack=(Ack)CommonUtils.byteArraytoObject(ackData);
						System.out.println("Recived ACK for packet "+ack.getAckNo());
						dataMap.remove(new Integer(ack.getAckNo()));

						totalAck++;
						if(isLast  && dataMap.isEmpty()){
							break;
						}
					}
				}catch(SocketTimeoutException e){
					boolean isFirst=true;
					packetSent=0;
					for(Entry<Integer,Packet> entry:dataMap.entrySet()){
						if(isFirst){
							Sf=entry.getKey();
							isFirst=false;
						}
						packet=entry.getValue();
						sendPacket(packet,senderSocket,receiverAddress,socketNo);
						System.out.println("Resending Packet "+packet.getSequenceNo()+" Timer Started");
						packetSent++;
						timeOut=true;
					}
				}
				if(!timeOut){
					Sf=Sn;
					packetSent=0;
				}
			}
			if(isLast){
				break;
			}
		}
	}

	
	
	
	
	

	/**
	 * 
	 * This is used to send packets to the receiver 
	 * @param packet
	 * @param senderSocket
	 * @param receiverAddress
	 * @param socketNo
	 * @throws IOException
	 */
	public static void sendPacket(Packet packet,DatagramSocket senderSocket, InetAddress receiverAddress, int socketNo) throws IOException{

		byte[] currentSegment=CommonUtils.objectToByteArray((Object)packet);
		DatagramPacket datagramPacket = new DatagramPacket(currentSegment,currentSegment.length, receiverAddress, socketNo);

		senderSocket.send(datagramPacket);

	}
}
