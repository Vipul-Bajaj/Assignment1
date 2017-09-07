package Assignment1;
/**
 * @author vipul
 *
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class TCPGameServer extends Thread {

	private Socket clientSocket = null;
	private ArrayList<String> playersList = null;

	public TCPGameServer(Socket s) {
		super("TCPGameServer");
		this.clientSocket = s;
	}

	public void run(){
		try {		
			playersList = Helper.getList();
			System.out.println("Client Connected");
			//Created Streams for input and output.
			DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			String name = dataInputStream.readUTF();
			playersList.add(name);
			objectOutputStream.writeObject(playersList);
			while(true){
				String str = (String) dataInputStream.readUTF();
				System.out.println("Message from Player : " + str);
				if(str.equalsIgnoreCase("bye")){
					playersList.remove(name);
					System.out.println("Ok Byeeeeee");
					break;
				}
			}
			Helper.setList(playersList);
			dataInputStream.close();
			objectOutputStream.close();
			clientSocket.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
	}
}

public class GameServer {

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket echoServer = null;

		try {
			echoServer = new ServerSocket(4444);
			System.out.println("Waiting for the Player to Connect...");
			while (true) {
				new TCPGameServer(echoServer.accept()).start();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				echoServer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

