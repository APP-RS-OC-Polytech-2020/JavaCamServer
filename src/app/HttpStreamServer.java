package app;
import javax.imageio.ImageIO;

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


    //private BufferedImage img = null;
    private ServerSocket serverSocket;
    private Socket socket;
    private final String boundary = "stream";
    private OutputStream outputStream;

    private int port;
    
    public boolean connected;

    public HttpStreamServer(int port) {
        
        this.port = port;
        connected = false;
    }

    /**
     * Demarre le serveur qui va distribuer le flux
     * @throws IOException
     */
    public void startStreamingServer() throws IOException {
        serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
        connected = true;
        System.out.println("Socket Accepted");
        writeHeader(socket.getOutputStream(), boundary);
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
    public void pushImage(BufferedImage frame) throws IOException {
        if (frame == null)
            return;
        try {
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
            
        } catch (Exception ex) {
        	//If exception, mean client disconnected. We wait for new client to come.
        	System.out.println("Client Disconnected, wait for socket");
        	connected = false;
        	
            socket = serverSocket.accept();
            connected = true;
            System.out.println("Client reconnected");
            writeHeader(socket.getOutputStream(), boundary);
        }
    }

    /**
     * Le run qui demarre le stream. Ouvre le service de flux, autorise la connexion, 
     * met tout ça dans un thread.
     */
    public void run() {
        try {
            System.out.println("go to  http://localhost:"+this.port+" with browser");
            startStreamingServer();
            
        } catch (IOException e) {
        	e.printStackTrace();
            return;
        }

    }
    
    /**
     * Quasiement jamais utilisé. Uniquement lorsque la socket meurt.
     * @throws IOException
     */
    public void stopStreamingServer() throws IOException {
        socket.close();
        serverSocket.close();
    }
//    /**
//     * Classe qui converti un Mat de OpenCV vers une bufferedimage de Java.
//     * @param image (org.opencv.core.Mat)
//     * @return image (java.awt.image.BufferedImage)
//     * @throws IOException
//     */
//    public static BufferedImage Mat2bufferedImage(Mat image) throws IOException {
//        MatOfByte bytemat = new MatOfByte();
//        Imgcodecs.imencode(".jpg", image, bytemat);
//        byte[] bytes = bytemat.toArray();
//        InputStream in = new ByteArrayInputStream(bytes);
//        BufferedImage img = null;
//        img = ImageIO.read(in);
//        return img;
//    }
    
    public boolean isConnected(){
    	if(socket!=null){
    		return connected;
    	}else{
    		return false;
    	}
    }
}