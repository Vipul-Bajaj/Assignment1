import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author vipul
 *
 */
/**
 * @author vipul
 *
 */
public class Players {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> opponentsList = null;
		Socket clientSocket = null;
		Scanner scanner = new Scanner(System.in);
		try {
			clientSocket = new Socket("localhost", 4444);
			opponentsList = new ArrayList<>();
			ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
			DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			System.out.println("Enter your good name : ");
			String name = scanner.nextLine();
			dataOutputStream.writeUTF(name);
			System.out.println("The opponents online are");
			opponentsList = (ArrayList<String>) objectInputStream.readObject();
			for(String s : opponentsList){
				System.out.println(s);
			}
			while(true){
				System.out.println("Enter the message to be send");
				String str = scanner.nextLine();
				dataOutputStream.writeUTF(str);
				if(str.equalsIgnoreCase("Bye")){
					System.out.println("Terminatting");
					break;
				}
			}
			objectInputStream.close();
			dataOutputStream.close();
			scanner.close();
			clientSocket.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
