package app;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe qui va exposer un stream mpeg venant de la camera. 
 * @author prospere
 *
 */
public class HttpStreamServer implements Runnable {


	final Logger logger = LoggerFactory.getLogger(HttpStreamServer.class);
	
    private ServerSocket serverSocket;
    private Socket socket;
    private final String boundary = "stream";
    private OutputStream outputStream;
    private int port;
    public boolean connected;
	private WebcamAPICameraStream server;

    private HttpStreamServer(int port) {
        
        this.port = port;
        connected = false;
    }
    public HttpStreamServer(int port, WebcamAPICameraStream server) {
    	this(port);
    	this.server = server;
    }

    /**
     * Demarre le serveur qui va distribuer le flux
     * @throws IOException
     */
    public void startStreamingServer() throws IOException {
        serverSocket = new ServerSocket(port);
        
        /* Trying cleanup
        socket = serverSocket.accept();
        connected = true;
        System.out.println("Socket Accepted");
        writeHeader(socket.getOutputStream(), boundary);
        */
    }
    /**
     * Classe qui ecris le header HTTP pour exposer le flux. 
     * A noter que le header est ajusté pour un HTTP recent.
     * @param stream
     * @param boundary
     * @throws IOException
     */
    private void writeHeader(OutputStream stream, String boundary) throws IOException {
        stream.write(("HTTP/1.0 200 OK\r\n" +
                "Connection: close\r\n" +
                "Max-Age: 0\r\n" +
                "Expires: 0\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n" +
                //"Pragma: no-cache\r\n" + //Only useful for backwd compatibility
                "Content-Type: multipart/x-mixed-replace; " +
                "boundary=" + boundary + "\r\n" +
                "\r\n" +
                "--" + boundary + "\r\n").getBytes());
    }
    
    /**
     * Balance une image dans le stream. Elle pousse un bout de contenu (une frame).
     * @param frame
     * @throws IOException
     */
    public void pushImage(BufferedImage frame) {
        if (frame == null){return;}
        try{
            outputStream = socket.getOutputStream();
            //BufferedImage img = Mat2bufferedImage(frame);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(frame, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            outputStream.write(("Content-type: image/jpeg\r\n" +
                    "Content-Length: " + imageBytes.length + "\r\n" +
                    "\r\n").getBytes());
            outputStream.write(imageBytes);
            outputStream.write(("\r\n--" + boundary + "\r\n").getBytes());
        }catch(IOException e){
        	

        	//If exception, mean client disconnected. We wait for new client to come.
        	
        	logger.warn("Cannot push image to client. Disconnecting.");
        	connected = false;
        	try{socket.close();}catch(IOException el){el.printStackTrace();}
        }
    }

    /**
     * Le run qui demarre le stream. Ouvre le service de flux, autorise la connexion, 
     * met tout ça dans un thread.
     */
    public void run() {
        try {
        	logger.info("go to  http://localhost:"+this.port+" with browser");
            startStreamingServer();
            
        } catch (IOException e) {
        	e.printStackTrace();
            return;
        }
        while(true){
	        if(connected==false){
	        	logger.info("Now we wait for a client");
	            server.startPokeTask(5000);
	            try {
	            	socket = serverSocket.accept();
		            connected = true;
		            logger.info("Client connected.");
		            try {
						writeHeader(socket.getOutputStream(), boundary);
					} catch (IOException e) {
						connected = false;
						try {socket.close();} catch (IOException e1) {e1.printStackTrace();} //Something went terribly wrong
					}
	            } catch (IOException e) {
	            	logger.warn("We tried connecting, but it didn't work. Retrying...");
	            } 
	        }//End if
	        else{
	        	//System.out.println("kek");
	        	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
        }//End while

    }//End run
    
    /**
     * Methode qui arrête le serveur.
     * Quasiement jamais utilisé. Uniquement lorsque la socket meurt.
     * @throws IOException
     */
    public void stopStreamingServer() throws IOException {
        socket.close();
        serverSocket.close();
    }
    
    public boolean isConnected(){
    	if(socket!=null){
    		return connected;
    	}else{
    		return false;
    	}
    }
}