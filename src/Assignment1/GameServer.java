package Assignment1;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
/*
 * @authors :
 * Lakshmi Priya Kolli - 14115048
 * Modugula Venkata Sai Geethika - 14115055
 * Vipul Bajaj - 14115904
 */

public class GameServer {
		private static ServerSocket gameServerSocket = null;
		private static Socket clientSocket = null;
		private static final int maxClients = 15;
		private static final Clients[] client_threads = new Clients[maxClients];

		public static void main(String args[])
		{
			try
			{
				gameServerSocket = new ServerSocket(4589);
				System.out.println("Server is ready. Waiting for connections:");
				
				int i=0;
				
				while(true)
				{
					clientSocket = gameServerSocket.accept();
					
					for(i = 0; i < maxClients ; i++)
					{
						if( client_threads[i] == null)
						{
							(client_threads[i] = new Clients (clientSocket, client_threads)).start();
							break;
						}
					}
					
					if (i == maxClients)
					{
						PrintStream ps = new PrintStream(clientSocket.getOutputStream());
						ps.println ("Server is busy. Please try later");
						ps.close();
						clientSocket.close();
					}
				}
			}
			catch(Exception e)
			{
				System.out.println( "Error on server side "+ e );
			}
		}
}

class Clients extends Thread{
	DataInputStream dis = null;
	PrintStream ps = null;
	private String client_name = null;
	private Socket clientSocket = null;
	private final Clients[] client_threads;
	private int maxClients;
	private String opponent_name = null;
	private String requester = "list";
	private int occupied = 0;
	
	public Clients ( Socket s, Clients[] thread )
	{
		this.clientSocket = s;
		this.client_threads = thread;
		this.maxClients = thread.length;
	}
	
	public void run()
	{
		int maxClients_count = this.maxClients;
		Clients[] threads = this.client_threads;
		
		try
		{
			dis = new DataInputStream ( clientSocket.getInputStream());
			ps = new PrintStream ( clientSocket.getOutputStream(),true);
			String clientName;
			
			ps.println("Choose any of the following \n"
					+ "a. To get players online type getplayers\n"
					+ "b. To request opponent type name of opponent\n"
					+ "c. To accept request - type accept,player_name / To reject request - type reject,player_name\n"
					+ "d. To quit the game type quit\n"
					+ "e. To go offline type bye\n");
			while(true)
			{
				int flag = 0;
				ps.println("Enter your good name:");
				clientName = dis.readLine();
				
				synchronized(this)
				{
					for(int i = 0; i < maxClients; i++)
					{
						if(threads[i] != null && threads[i].client_name != null && threads[i].client_name.equalsIgnoreCase(clientName))
						{
							flag = 1;
							this.ps.println("User with the name " + clientName + "already exists. Please choose some other name ");
						}
					}
					if(flag == 0)
					{
						break;
					}
				}
			}
			System.out.println("Connected to " + clientName);
			
			synchronized(this)
			{
				for(int i = 0; i < maxClients; i++)
				{
					if( threads[i] != null && threads[i] == this)
					{
						client_name = clientName;
						break;
					}
				}
				for(int i = 0; i < maxClients; i++)
				{
					if(threads[i] != null && threads[i].client_name != null && threads[i].client_name != this.client_name)
					{
						threads[i].ps.println(clientName + " is online");
					}
				}
			}
			int temp = 0;
			
			synchronized(this)
			{
				for(int i = 0; i < maxClients; i++)
				{
					if(threads[i] != null && threads[i].client_name != null && threads[i].client_name != this.client_name)
					{
						temp = 1;
						break;
					}
				}
			}
			if(temp == 0)
			{
				ps.println("No player is online");
			}
			else
			{
				synchronized(this)
				{
					ps.println("List of all the players who are online ");
					for(int i = 0; i < maxClients; i++)
					{
						if(threads[i] != null && threads[i].client_name != null && threads[i].client_name != this.client_name)
						{
							ps.println(threads[i].client_name);
						}
					}
					ps.println("List of players who are available to play the game:");
					for (int i = 0; i < maxClients; i++) 
					{
						if (threads[i] != null && threads[i].client_name != null && threads[i].occupied == 0 && threads[i].client_name != this.client_name)
						{
							this.ps.println(threads[i].client_name);
						}
					}
				}
			}
			
			while(true)
			{
				String choice = dis.readLine();
				if (choice.equalsIgnoreCase("getplayers"))
				{ 
					synchronized (this) 
					{ 
						this.ps.println("List of all the players who are online are:");
						for (int i = 0; i < maxClients; i++) 
						{
							if (threads[i] != null && threads[i].client_name != this.client_name)
							{
								this.ps.println(threads[i].client_name);
							}
						}
						this.ps.println("List of players who are available to play the game:");
						for (int i = 0; i < maxClients; i++) 
						{
							if (threads[i] != null && threads[i].occupied == 0 && threads[i].client_name != this.client_name)
							{
								this.ps.println(threads[i].client_name);
							}
						}
					}

				}

				
				else if(choice.indexOf("accept")>=0 || choice.indexOf("reject")>=0 )
				{
					StringTokenizer strtoken = new StringTokenizer(choice);
					String check = strtoken.nextToken(",");
					String name = strtoken.nextToken();
					
					synchronized(this)
					{
						for(int i = 0; i < maxClients; i++)
						{
							if(threads[i] != null && threads[i].client_name.equals(name))
							{
								if(check.equalsIgnoreCase("accept"))
								{
									if(this.occupied == 1)
									{
										this.ps.println("You are already playing with " + this.opponent_name);
									}
									else if(threads[i].occupied == 1 && this.requester != null && this.requester.indexOf(threads[i].client_name)>=0)
									{
										this.ps.println( name + " is now playing with " + threads[i].opponent_name);
										this.requester = this.requester.replace(name, "");
									}
									else if(this.requester != null && this.requester.indexOf(threads[i].client_name)>=0)
									{
										threads[i].ps.println("You are now connected with " + this.client_name + " to play.");
										this.ps.println("You are now connected with " + name + " to play.");
										threads[i].occupied = 1;
										threads[i].opponent_name = this.client_name;
										this.occupied = 1;
										this.opponent_name = name;
										this.requester = this.requester.replace(name, "");
									}
								}
								
								else if(check.equalsIgnoreCase("reject") && this.requester!=null && this.requester.indexOf(name)>=0)
								{
									threads[i].ps.println(this.client_name+" rejected your request.");
									this.requester=this.requester.replace(name,"");
								}	  
								
							}
						}
					}
				}
				
				else if(choice.equalsIgnoreCase("bye"))
				{
					this.ps.println("You are going offline");
					
					synchronized(this)
					{
						if(this.occupied == 1)
						{
							
							for(int i = 0; i < maxClients; i++)
							{
								if(threads[i] != null && threads[i].client_name != this.opponent_name && threads[i].client_name != this.client_name)
								{
									threads[i].ps.println(this.opponent_name + "is now available to play game.");
								}
							}
							
							for(int i = 0; i < maxClients; i++)
							{
								if(threads[i] != null && threads[i].client_name.equals(this.opponent_name) && threads[i].occupied == 1)
								{
										threads[i].ps.println("Your opponent " + this.client_name + " left the game." );
										threads[i].occupied = 0;
										threads[i].opponent_name = null;
										this.occupied = 0;
										this.opponent_name = null;
								}
							}
						}
					}
					break;
				}

				
				else if (choice.equalsIgnoreCase("quit"))
				{ 
					synchronized (this)
					{ 
						for (int i = 0; i < maxClients; i++) 
						{
							if (threads[i] != null && threads[i].client_name!=this.opponent_name && threads[i].client_name!=this.client_name)
							{
								threads[i].ps.println(this.client_name +" & "+this.opponent_name+"  are now available to play game.");
							}
						}

						for (int i = 0; i < maxClients; i++)
						{
							if (threads[i] != null && threads[i].client_name.equals(this.opponent_name) && threads[i].occupied == 1) 
							{
								threads[i].ps.println("Your opponent "+this.client_name+" quit the game.");
								this.ps.println("You have quit the game.");
								
								this.ps.println("List of players who are available to play the game:");
								for (int j = 0; j < maxClients; j++) 
								{
									if (threads[j] != null && threads[j].occupied == 0 && threads[j].client_name != this.client_name)
									{
										this.ps.println(threads[j].client_name);
									}
								}		
								
								threads[i].occupied = 0;
								threads[i].opponent_name = null;
								this.occupied = 0;
								this.opponent_name = null;
							}
						}
					}
				}
				
				else
				{
					synchronized (this) 
					{ 
						int tempx = 0;
						for (int i = 0; i < maxClients; i++)
						{
							if(this.occupied == 1)
							{
								this.ps.println("You are already playing with " + this.opponent_name);
							}
							else if (threads[i] != null && threads[i].client_name.equals(choice) && threads[i].occupied == 0)
							{
								threads[i].requester = threads[i].requester.concat(clientName);
								threads[i].ps.println( clientName + " wants to play with you.");
								this.ps.println("Request sent to "+threads[i].client_name+".");
								tempx = 1;
							}
							else if (threads[i] != null && threads[i].client_name.equals(choice) && threads[i].occupied == 1) 
							{
								this.ps.println( choice + " is busy playing with other opponent.");
								tempx = 1;
							}

						}
						if( tempx == 0)
						{
							this.ps.println( choice + " is not a valid player name.");
						}
					}    
				}

			}
				
				
				ps.println("Disconnected");
				System.out.println( clientName + " Disconnected");
				
				synchronized(this)
				{
					for (int i = 0; i < maxClients; i++) 
					{
						if (threads[i] != null && threads[i].client_name!=null)
						{
							threads[i].ps.println(clientName + " has left");
						}
					}
				}	

				synchronized (this) 
				{
					for (int i = 0; i < maxClients; i++) 
					{
						if (threads[i] == this)
						{
							threads[i] = null;
						}
					}
				}
				dis.close();
				ps.close();
				clientSocket.close();			
		}
		catch(Exception e)
		{
				System.out.println("Server side client threads error " + e);
		}
	}
}
