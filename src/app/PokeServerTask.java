package app;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (Relatively)Simple TimerTask which on run, opens a connection, send a JSON message on it and then closes it.
 * @author prospere
 *
 */
public class PokeServerTask extends TimerTask {
	
	final Logger logger = LoggerFactory.getLogger(TimerTask.class);
	
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
        		//Try to open socket and poke.
        		logger.info("We try poking our server at {}",serverToPoke.getAddress().toString());
    			Socket clientSocket = new Socket();
    			clientSocket.connect(serverToPoke, 1000);
    			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    			
    			String address = (clientSocket.getLocalAddress().toString()).substring(1);
    			logger.info("Poke success ! We're at: {}",address);
    			
    			//The JSON message to send.
    			out.println("{\"type\":\"init\","
    					+ "\"infoInit\":\"Coucou, j'ouvre un server, cya\","
    					+ " \"clientName\": \" "+camName+"\","
    					+ " \"clientType\":\"Webcam\","
    					+ " \"ip\":\""+address+"\","
    					+ " \"port\":\""+port+"\"}");
    			
    			clientSocket.close();
    		} catch (UnknownHostException e) {
    			logger.error("Unknown Host. Retrying...");
    		} catch (IOException e) {
    			logger.error("Poke didn't reach our server ( IOException ). Retrying...");}
		}else{
			logger.info("We're connected, cancel Poke.");
			this.cancel();
		}
		
	}

}
