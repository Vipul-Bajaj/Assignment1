package Assignment1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*
 * @authors :
 * Lakshmi Priya Kolli - 14115048
 * Modugula Venkata Sai Geethika - 14115055
 * Vipul Bajaj - 14115904
 */

class GameClient implements Runnable
{
	private static PrintStream ps = null;
	private static DataInputStream dis = null;
	private static BufferedReader br = null;

	private static boolean closed = false;
	private static Socket clientSocket = null;
	
	public static void main(String args[])
	{
		try
		{
			clientSocket = new Socket( "localhost" , 4589);
			br = new BufferedReader(new InputStreamReader(System.in));
			dis = new DataInputStream(clientSocket.getInputStream());
			ps = new PrintStream(clientSocket.getOutputStream());
			
		}
		catch(Exception e)
		{
			System.out.println( "Cient side error: " + e.getMessage());
		}  
		
		if( dis != null && ps != null && clientSocket != null)
		{
			try {

				
				new Thread( new GameClient() ).start();
				while (!closed) 
				{
					ps.println(br.readLine());
				}
				
				ps.close();
				dis.close();
				clientSocket.close();
			} 
			
			catch (IOException e) 
			{
				System.err.println("Client side IOException:  " + e);
			}
		}
	}

	
	public void run() {
	
		String input;
		try 
		{
			while (( input = dis.readLine()) != null)
			{
				System.out.println( input );
				
				if ( input.equalsIgnoreCase("Disconnected"))
					break;
			}
			
			closed = true;
			
		} 
		
		catch (IOException e) {
			System.err.println("Client side IOException:  " + e);
		} 
	}
}