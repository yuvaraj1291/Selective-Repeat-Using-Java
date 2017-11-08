import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This utility class is used for 
 * serialization and de-serialization
 */
public class CommonUtils {

	public static byte[] objectToByteArray(final Object obj) throws IOException{

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(obj);
		return byteArrayOutputStream.toByteArray();
	}

	public static Object byteArraytoObject(final byte[] data) throws IOException, ClassNotFoundException{

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		return objectInputStream.readObject();
	}


	/*public static byte[] computeChecksum(byte[] data){

	}
	 */

	public static int generateCheckSum(byte[] message) {
		int checksum = 0;
		for (int i = 0; i < message.length; i++) {
			checksum += message[i];
		}
		return checksum;
	}

}
