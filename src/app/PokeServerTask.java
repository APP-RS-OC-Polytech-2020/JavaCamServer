package app;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

public class PokeServerTask extends TimerTask {
	
	private HttpStreamServer httpStreamServer;
	int port;
	InetSocketAddress serverToPoke;
	String camName;

	public PokeServerTask(HttpStreamServer httpStreamServer, int hostPort, InetSocketAddress serverToPoke,String camName) {
		this.httpStreamServer = httpStreamServer;
		this.port = hostPort;
		this.serverToPoke = serverToPoke;
		this.camName = camName;
	}
	
	@Override
	public void run() {
		if(httpStreamServer.isConnected()==false){
        	try {
        		System.out.println("We try poking our server at "+serverToPoke.getAddress().toString());
    			Socket clientSocket = new Socket();
    			clientSocket.connect(serverToPoke, 1000);
    			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    			
    			String address = (clientSocket.getLocalAddress().toString()).substring(1);
    			System.out.println("POKING ! :3 "+address);
    			
    			out.println("{\"type\":\"init\","
    					+ "\"infoInit\":\"Coucou, j'ouvre un server, cya\","
    					+ " \"clientName\": \"\","
    					+ " \"clientType\":\"Webcam\","
    					+ " \"ip\":\""+address+"\","
    					+ " \"port\":\""+port+"\","
    					+ " \"name\":\""+camName+"\"}");
    			clientSocket.close();
    		} catch (UnknownHostException e) {
    			System.err.println("Couldn't poke server :c");
    			//e.printStackTrace();
    		} catch (IOException e) {
    			System.err.println("IOException in PokeServerTask");
    			//e.printStackTrace();
    		}
		}else{
			System.out.println("We're already connected");
		}
		
	}

}
